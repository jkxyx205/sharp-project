package com.rick.fileupload.image;

import com.google.common.collect.Lists;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.fileupload.core.Constants;
import com.rick.fileupload.core.FileUploader;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.model.ImageParam;
import com.rick.fileupload.persist.Document;
import com.rick.fileupload.persist.support.DocumentUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 图片处理类
 * @author: Rick.Xu
 * @date: 10/19/18 14:33
 * @Copyright: 2018 www.yodean.com. All rights reserved.
 */
@Service
@Slf4j
public class ImageService {


    @Autowired
    private FileUploader fileUploader;

    @Autowired
    private InputStreamStore inputStreamStore;

    /**
     * 应用内存
     */
    private Map<String, String> cache = new HashMap<>();

    /**
     * 图片查看
     * @param request
     * @param response
     * @param id
     * @param imageParamVO
     */
    public void view(HttpServletRequest request, HttpServletResponse response, Long id, ImageParam imageParamVO) {
        // 1. 查找缓存
//        String urlPath = request.getRequestURI() + "?" + request.getQueryString();
//        String cacheName = cache.get(urlPath);
//
//        if (cacheName != null) {
//            response.getOutputStream()
//            .write(IOUtils.toByteArray(new URL(Constants.CACHE_URL + cacheName)));
//            return;
//        }


        Document document = fileUploader.findById(id);

        //不是图片类型
        if (!(document.getContentType().startsWith("image") && isImageExt(document.getExtension()))
                ||Objects.nonNull(imageParamVO.getF()) && !isImageExt(imageParamVO.getF())) {
            // throw new FastDFSException(ErrorCode.FILE_EXT_EXCEPTION);
            // TODO
        }

        if (Objects.nonNull(imageParamVO.getF())) {
            document.setExtension(imageParamVO.getF());
        }

        try(OutputStream os = HttpServletResponseUtils.getOutputStreamAsView(request, response, document.getFullName());

            InputStream is = inputStreamStore.getInputStream(document.getGroupName(), document.getPath())) {

            BufferedImage bi = ImageIO.read(is);

            String url = inputStreamStore.getURL(document.getGroupName(), document.getPath());

            if (EqualsBuilder.reflectionEquals(imageParamVO.getP(), 0) && request.getParameterMap().size() == 1) {
                //只有一个参数p=0，获取原始图片
                os.write(IOUtils.toByteArray(new URL(url)));
                return;
            } else if(StringUtils.isBlank(request.getQueryString())) {
                imageParamVO.setEmpty(true);
                //没有任何参数
                long size = document.getSize();
                //自动压缩图片，图片大小保持不变
                if(size <= Constants.COMPRESS_THRESHOLD){
                    os.write(IOUtils.toByteArray(new URL(url)));
                    return;
                }
            }

            Thumbnails.Builder<BufferedImage> builder = handleImage(bi, imageParamVO, document.getSize(), document.getExtension());
            //指定图片格式
            if (Objects.nonNull(imageParamVO.getF())) {
                response.setContentType("image/" + imageParamVO.getF());
                document.setExtension(imageParamVO.getF());
            } else {
                //图片原始格式
                response.setContentType(document.getContentType());
            }


//            makeFileCache(builder, urlPath, document.getExt());
            builder.toOutputStream(os);


        } catch (IOException e) {
            log.error("图片处理失败", e.getMessage());
        }

    }

    /**
     * 加入缓存
     * @param builder
     * @param urlPath
     * @param ext
     */
    private void makeFileCache(Thumbnails.Builder<BufferedImage> builder, String urlPath, String ext) {
        // TODO 添加Redis缓存
        //生产缓存文件
        Executors.newSingleThreadExecutor().submit(() -> {
            String cacheName = System.currentTimeMillis() + UUID.randomUUID().toString() + "." + ext;
            try {
                builder.toFile(new File(Constants.CACHE, cacheName));
                cache.put(urlPath, cacheName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 判断是否是合法的文件类型
     * @param ext
     * @return
     */
    private boolean isImageExt(String ext) {
        for (String s : Constants.ALLOWED_FORMAT_TYPE) {
            if (s.equalsIgnoreCase(ext)) {
                return true;
            }
        }

        return false;
    }

    private Thumbnails.Builder<BufferedImage> handleImage(BufferedImage bi, ImageParam imageParam, long size, String ext) {
        log.info("image size {}x{}", bi.getWidth(), bi.getHeight());

        Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(bi);

            //从比例中获取长宽
            if (Objects.nonNull(imageParam.getRw())
                    && Objects.nonNull(imageParam.getRh())
                    ) {

                int x = imageParam.getX() == null ? 0 : imageParam.getX();
                int y = imageParam.getY() == null ? 0 : imageParam.getY();


                if (Objects.isNull(imageParam.getW())) {
                    imageParam.setW(bi.getWidth());
                }

                if (Objects.isNull(imageParam.getH())) {
                    imageParam.setH(bi.getHeight());
                }

                //TODO 无法获取Postion中x y参数
                Integer[] wh = cropSize(imageParam.getW() < bi.getWidth() - x ? imageParam.getW() : bi.getWidth() - x ,
                                        imageParam.getH() < bi.getHeight() - y ? imageParam.getH() : bi.getHeight() - y,
                                        imageParam.getRw(),
                                        imageParam.getRh());

                imageParam.setW(wh[0]);
                imageParam.setH(wh[1]);

                imageParam.setX(x);
                imageParam.setY(y);
            }

            //没有任何参数,压缩然后输出
            if (imageParam.isEmpty()) {
                double quality = Constants.COMPRESS_THRESHOLD / size ;
                builder.outputQuality(quality);
                builder.scale(1.0f);
            } else if (Objects.nonNull(imageParam.getW()) && Objects.nonNull(imageParam.getH())) { //长和宽都有值
                Position position = Positions.CENTER;

                if (Objects.nonNull(imageParam.getPosition())) {
                    //TODO 跟zimg表现不一致
                    position = imageParam.getPosition();
                }

                if (Objects.isNull(imageParam.getP())) {
                    builder.sourceRegion(position, imageParam.getW(), imageParam.getH());
                    builder.scale(1.0f);
                } else if (EqualsBuilder.reflectionEquals(imageParam.getP(), 0)) {
                    // 图片拉伸
                    builder.forceSize(imageParam.getW(), imageParam.getH());
                } else if (EqualsBuilder.reflectionEquals(imageParam.getP(), 2)) {
                    //图片放大2倍
                    builder.sourceRegion(position, imageParam.getW() / 2, imageParam.getH() / 2);
                    builder.scale(2.0f);
                } else if (EqualsBuilder.reflectionEquals(imageParam.getP(), 3)) {
                    //按比例裁剪
                    builder.sourceRegion(position, bi.getWidth() * imageParam.getW() / 100 ,  bi.getHeight() * imageParam.getH() / 100);
                    builder.scale(2.0f);
                }

            } else { //哪个有值，设置哪个
                if (Objects.nonNull(imageParam.getW())) {
                    builder.width(imageParam.getW());
                }

                if (Objects.nonNull(imageParam.getH())) {
                    builder.height(imageParam.getH());
                }
            }

            //指定图片格式
            if (Objects.nonNull(imageParam.getF())) {
                builder.outputFormat(imageParam.getF());
            } else {
                //图片原始格式
                builder.outputFormat(ext);
            }

            //图片质量
            if (Objects.nonNull(imageParam.getQ())) {
                builder.outputQuality(imageParam.getQ()/100f);
            }

            //图片旋转
            if (Objects.nonNull(imageParam.getR())) {
                builder.rotate(imageParam.getR());
            }

            return builder;
    }

    /**
     * 图片裁剪
     * @param position
     * @param w
     * @param h
     * @param aspectRatioW
     * @param aspectRatioH
     * @return
     */
    @Transactional
    public Document cropPic(MultipartFile file, Position position, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        byte[] content = IOUtils.toByteArray(file.getInputStream());

        Document document = DocumentUtils.parseToDocument(file);

        ImageParam imageParam = new ImageParam();
        imageParam.setPosition(position);
        imageParam.setX(x);
        imageParam.setY(y);
        imageParam.setW(w);
        imageParam.setH(h);
        imageParam.setRw(aspectRatioW);
        imageParam.setRh(aspectRatioH);
        imageParam.setEmpty(false);

        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(content));

        Thumbnails.Builder<BufferedImage> builder = handleImage(bi, imageParam, document.getSize(), document.getExtension());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        builder.toOutputStream(bos);

        bos.close();

        // 裁剪字节数组
        document.setData(bos.toByteArray());
        return fileUploader.upload2(Lists.newArrayList(document), null, null).get(0);

    }

    /**
     * 自动裁剪(从中心裁剪)
     * @param file
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    public Document cropPic(MultipartFile file, int aspectRatioW, int aspectRatioH) throws IOException {
        BufferedImage bi = ImageIO.read(file.getInputStream());
        return cropPic(file, Positions.CENTER, bi.getWidth(), bi.getHeight(), aspectRatioW, aspectRatioH);
    }

    /**
     * 自定义裁剪
     * @param file
     * @param x
     * @param y
     * @param w
     * @param h
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    public Document cropPic(MultipartFile file, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        Position position = new Coordinate(x, y);
        return cropPic(file, position, x, y, w, h, aspectRatioW, aspectRatioH);
    }

    private Document cropPic(MultipartFile file, Position position, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        return cropPic(file, position, 0, 0, w, h, aspectRatioW, aspectRatioH);
    }

    public String createImage(String text) throws IOException {
        byte[] content = NameImageCreator.generateImg(text);

        String[] result = inputStreamStore.store(new ByteArrayInputStream(content),   "png");
        return inputStreamStore.getURL(result[0], result[1]);
    }


    private static Integer[] cropSize(Integer w, Integer h, int aspectRatioW, int aspectRatioH) {
        if(aspectRatioW == 0 || aspectRatioH == 0) {
            return new Integer[]{w, h};
        }

        if (w * aspectRatioH != h * aspectRatioW) {

            int r;

            if (w * aspectRatioH > h * aspectRatioW) {
                r = h / aspectRatioH;
            } else {
                r = w / aspectRatioW;
            }

            w = aspectRatioW * r;
            h = aspectRatioH * r;
        }

        Integer[] arr = new Integer[2];
        arr[0] = w;
        arr[1] = h;
        return arr;

    }
}