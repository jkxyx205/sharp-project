package com.rick.fileupload.client.support;

import com.google.common.collect.Lists;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.util.DateConvertUtils;
import com.rick.common.util.IdGenerator;
import com.rick.common.util.ZipUtils;
import com.rick.fileupload.core.FileStore;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileUploadProperties;
import com.rick.fileupload.plugin.image.ImageParam;
import com.rick.fileupload.plugin.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.rick.common.util.StringUtils.isImageType;

/**
 * @author Rick
 * @createdAt 2021-09-29 18:15:00
 */
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentDAO documentDAO;

    private final FileStore fileStore;

    private final FileUploadProperties fileUploadProperties;

    private final ImageService imageService;

    @Override
    public Document store(FileMeta fileMeta, String groupName) throws IOException {
        fileStore.storeFileMeta(Arrays.asList(fileMeta), groupName);
        Document document = parse(fileMeta);
        documentDAO.insert(parseParams(document));
        return document;
    }

    @Override
    public List<Document> store(List<FileMeta> fileMetaList, String groupName) throws IOException {
        fileStore.storeFileMeta(fileMetaList, groupName);

        List<Document> documentList = Lists.newArrayListWithExpectedSize(fileMetaList.size());
        for (FileMeta fileMeta : fileMetaList) {
            documentList.add(parse(fileMeta));
        }

        documentDAO.insert(documentList.stream().map(document -> parseParams(document)).collect(Collectors.toList()));
        return documentList;
    }

    @Override
    public void rename(long id, String name) {
        documentDAO.updateById("name", new Object[] {name}, id);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, long... ids) throws IOException {
        if (ArrayUtils.isEmpty(ids)) {
            throw new RuntimeException("下载id不能为空");
        }
        Document document = ids.length > 1 ? new Document() : findById(ids[0]);
        OutputStream os;

        // 压缩下载
        if (ids.length > 1) {
            //1.下载文件
            List<Document> subDocuments;

            subDocuments = new ArrayList<>(ids.length);
            for (Long id : ids) {
                subDocuments.add(findById(id));
            }
            document.setName("【批量下载】"+ subDocuments.get(0).getFullName() + "等" + ids.length + "个文件");

            Path path = Files.createTempDirectory(Paths.get(fileUploadProperties.getTmp()),null);

            File home = path.toFile();

            File root = new File(home, document.getFullName());
            root.mkdir();

            //4.创建
            downloadDocument2Folder(root, subDocuments);

            String zipName = document.getFullName() + ".zip";
            File zipFile = new File(home, zipName);

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

            ZipUtils.zipDirectoryToZipFile(home.getAbsolutePath(), root, zipOut);

            os = HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, zipName);

            zipOut.close();

            FileCopyUtils.copy(new FileInputStream(zipFile), os);
            FileUtils.deleteQuietly(home);
        } else { //单文件下载
            os = HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, document.getFullName());
            FileCopyUtils.copy(fileStore.getInputStream(document.getGroupName(), document.getPath()),  os);
        }

        os.close();
    }

    @Override
    public void delete(Long... ids) {
        for (long id : ids) {
            Document document = findById(id);
            try {
                fileStore.delete(document.getGroupName(), document.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //删除数据库记录
        documentDAO.deleteByIds(Arrays.asList(ids));
    }

    @Override
    public Document findById(long id) {
        Document document = documentDAO.selectById(id).get();
        document.setUrl(getURL(id));
        return document;
    }

    @Override
    public void preview(long id, ImageParam imageParam, OutputStream os) throws IOException {
        Document document = findById(id);
        if (isImageType(document.getExtension(), document.getContentType())) {
            // 预览图片
            imageService.write(document, imageParam, os);
        } else {
            // pdf等其他文件
            os.write(IOUtils.toByteArray(fileStore.getInputStream(document.getGroupName(), document.getPath())));
            os.close();
        }
    }

    @Override
    public String getURL(long id) {
        Document document = documentDAO.selectById(id).get();
        return fileStore.getURL(document.getGroupName(), document.getPath());
    }

    private void downloadDocument2Folder(File folder, List<Document> subDocuments) throws IOException {
        for (Document subDocument :  subDocuments) {
            FileCopyUtils.copy(fileStore.getInputStream(subDocument.getGroupName(), subDocument.getPath()), new FileOutputStream(new File(folder, subDocument.getFullName())));
        }
    }

    private Object[] parseParams(Document document) {
        // id,created_at,name,extension,content_type,size,group_name,path
        long sequenceId = IdGenerator.getSequenceId();
        document.setId(sequenceId);
        document.setCreatedAt(Instant.now());
        return new Object[] {
                document.getId()
                , DateConvertUtils.unixTimeToLocalDateTime(document.getCreatedAt().toEpochMilli()), document.getName(), document.getExtension(), document.getContentType(), document.getSize(),document.getGroupName(),
                document.getPath(),
        };
    }

    private Document parse(FileMeta fileMeta) {
        Document document = new Document();
        BeanUtils.copyProperties(fileMeta, document);
        return document;
    }
}