package com.rick.fileupload.upload;

import com.google.common.collect.Lists;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.util.ZipUtils;
import com.rick.fileupload.core.Constants;
import com.rick.fileupload.core.FileUploader;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.persist.Document;
import com.rick.fileupload.persist.DocumentService;
import com.rick.fileupload.persist.support.DocumentUtils;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * @author Rick
 * @createdAt 2021-09-29 19:18:00
 */
@RequiredArgsConstructor
public class FileUploaderImpl implements FileUploader {

    private final InputStreamStore inputStreamStore;

    private final DocumentService documentService;

    @Override
    public List<Document> upload(List<MultipartFile> multipartFileList, String groupName, String path) {
        if (CollectionUtils.isEmpty(multipartFileList)) {
            return Collections.emptyList();
        }

        List<Document> documentList = Lists.newArrayListWithExpectedSize(multipartFileList.size());

        multipartFileList.forEach(file -> {
            try {
                Document document = DocumentUtils.parseToDocument(file);
                String[] result = inputStreamStore.store(file.getInputStream(), document.getExtension());
                document.setGroupName(result[0]);
                document.setPath(result[1]);
                documentList.add(document);
            } catch (IOException e) {
//                "读取文件失败:" + file.getName()
            }

        });

        documentService.saveAll(documentList);
        return documentList;
    }

    @Override
    public List<Document> upload2(List<Document> documents, String groupName, String path) {
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }
        documents.forEach(document -> {
            try {
                String[] result  = inputStreamStore.store(new ByteArrayInputStream(document.getData()), document.getExtension());

                document.setGroupName(result[0]);
                document.setPath(result[1]);
            } catch (IOException e) {
//                "读取文件失败:" + file.getName()
            }

        });
        documentService.saveAll(documents);
        return documents;
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, long... ids) throws IOException {
        if (ArrayUtils.isEmpty(ids)) {
            throw new RuntimeException("下载id不能为空");
        }

        Document document;

        if (ids.length > 1) { //批量下载
            document = new Document();

        } else {
            document = documentService.findById(ids[0]).get();
        }

        OutputStream os;

        if (ids.length > 1) { //压缩下载
            //1.下载文件
            List<Document> subDocuments;

            subDocuments = new ArrayList<>(ids.length);
            for (Long id : ids) {
                subDocuments.add(findById(id));
            }
            document.setName("【批量下载】"+ subDocuments.get(0).getFullName() + "等" + ids.length + "个文件");

            Path path = Files.createTempDirectory(Paths.get(Constants.TMP),null);

            File _home = path.toFile();

            File root = new File(_home, document.getFullName());
            root.mkdir();

            //4.创建
            downloadDocument2Folder(root, subDocuments);

            String zipName = document.getFullName() + ".zip";
            File zipFile = new File(_home, zipName);

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

            ZipUtils.zipDirectoryToZipFile(_home.getAbsolutePath(), root, zipOut);

            os = HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, zipName);

            zipOut.close();

            FileCopyUtils.copy(new FileInputStream(zipFile), os);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(_home)));
//            FileUtils.forceDeleteOnExit(_home);
        } else { //单文件下载
            os = HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, document.getFullName());
//            FileCopyUtils.copy(getFileInputStream(document.getGroupName(), document.getPath()), os);
            FileCopyUtils.copy(getFileInputStream(document.getGroupName(), document.getPath()),  os);
//            FileCopyUtils.copy(getFileByteArray(document), os);
        }

        os.close();
    }

    @Override
    public void delete(Long... ids) {
        for (long id : ids) {
            Document document = findById(id);
            try {
                inputStreamStore.delete(document.getGroupName(), document.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //删除数据库记录
        documentService.deleteByIds(Arrays.asList(ids));
    }

    @Override
    public void rename(long id, String name) {
        documentService.rename(id, name);
    }

    @Override
    public Document findById(long id) {
        return documentService.findById(id).get();
    }

    @Override
    public void view(HttpServletRequest request, HttpServletResponse response, long id) throws IOException {
        Document document = findById(id);

        OutputStream os = HttpServletResponseUtils.getOutputStreamAsView(request, response, document.getFullName());
        FileCopyUtils.copy(getFileInputStream(document.getGroupName(), document.getPath()),  os);
//        FileCopyUtils.copy(getFileByteArray(document), os);
        os.close();
    }

    @Override
    public String getURL(long id) {
        Document document = findById(id);
        return inputStreamStore.getURL(document.getGroupName(), document.getPath());
    }

    private InputStream getFileInputStream(String groupName, String path) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url(inputStreamStore.getURL(groupName, path))
                .get()  //默认为GET请求，可以不写
                .build();

        final Call call = client.newCall(req);
        return call.execute().body().byteStream();
    }

    private void downloadDocument2Folder(File folder, List<Document> subDocuments) throws IOException {
        for (Document subDocument :  subDocuments) {
            FileCopyUtils.copy(getFileInputStream(subDocument.getGroupName(), subDocument.getPath()), new FileOutputStream(new File(folder, subDocument.getFullName())));
        }
    }
}
