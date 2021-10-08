package com.rick.fileupload.plugin.image;

import com.rick.common.util.IdGenerator;
import com.rick.fileupload.core.Constants;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.exception.NotImageTypeException;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.model.StoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

import static com.rick.common.util.StringUtils.isImageType;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 图片处理类
 * @author: Rick.Xu
 * @date: 10/19/18 14:33
 * @Copyright: 2018 www.yodean.com. All rights reserved.
 */
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final InputStreamStore inputStreamStore;

    /**
     * 图片查看
     * @param imageParam
     * @param fileMeta
     * @param imageParam
     */
    public void write(FileMeta fileMeta, ImageParam imageParam, OutputStream os) throws IOException {
        if (Objects.isNull(fileMeta.getData())) {
            InputStream is = inputStreamStore.getInputStream(fileMeta.getGroupName(), fileMeta.getPath());
            fileMeta.setData(IOUtils.toByteArray(is));
//            String url = inputStreamStore.getURL(fileMeta.getGroupName(), fileMeta.getPath());
        }

        if (Objects.isNull(imageParam) || imageParam.isEmpty()) {
            // 没有任何参数 自动压缩图片
            long size = fileMeta.getSize();
            if (size <= Constants.COMPRESS_THRESHOLD) {
                os.write(fileMeta.getData());
                os.close();
                return;
            }
        } else if (imageParam.isSource() && EqualsBuilder.reflectionEquals(imageParam.getP(), 0)) {
            // 只有一个参数p=0，获取原始图片
//            os.write(IOUtils.toByteArray(new URL(url)));
            os.write(fileMeta.getData());
            os.close();
            return;
        }

        // 添加缓存 fileMeta.getFullPath() + imageParam参数降序排列做key
        if (Objects.nonNull(imageParam.getF())) {
            fileMeta.setExtension(imageParam.getF());
        }

        cropPic(fileMeta, imageParam, os);
    }

    /**
     * 自动裁剪(从中心裁剪)
     * @param fileMeta
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    public FileMeta cropPic(FileMeta fileMeta, int aspectRatioW, int aspectRatioH) throws IOException {
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(fileMeta.getData()));
        return cropPic(fileMeta, Positions.CENTER, bi.getWidth(), bi.getHeight(), aspectRatioW, aspectRatioH);
    }

    /**
     * 自定义裁剪
     * @param fileMeta
     * @param x
     * @param y
     * @param w
     * @param h
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    public FileMeta cropPic(FileMeta fileMeta, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        Position position = new Coordinate(x, y);
        return cropPic(fileMeta, position, x, y, w, h, aspectRatioW, aspectRatioH);
    }

    public FileMeta cropPic(FileMeta fileMeta, Position position, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        return cropPic(fileMeta, position, 0, 0, w, h, aspectRatioW, aspectRatioH);
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
    private FileMeta cropPic(FileMeta fileMeta, Position position, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        ImageParam imageParam = new ImageParam();
        imageParam.setPosition(position);
        imageParam.setX(x);
        imageParam.setY(y);
        imageParam.setW(w);
        imageParam.setH(h);
        imageParam.setRw(aspectRatioW);
        imageParam.setRh(aspectRatioH);
        return cropPic(fileMeta, imageParam);
    }

    public FileMeta cropPic(FileMeta fileMeta, ImageParam imageParam) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        cropPic(fileMeta, imageParam, bos);
        // 裁剪字节数组
        fileMeta.setData(bos.toByteArray());
        return fileMeta;
    }

    public FileMeta cropPic(FileMeta fileMeta, ImageParam imageParam, OutputStream os) throws IOException {
        // 不是图片类型
        if (!isImageType(fileMeta.getExtension(), fileMeta.getContentType())) {
            throw new NotImageTypeException();
        }

        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(fileMeta.getData()));

        Thumbnails.Builder<BufferedImage> builder = handleImage(bi, imageParam, fileMeta.getSize(), fileMeta.getExtension());
        builder.toOutputStream(os);
        os.close();
        return fileMeta;
    }

    public String createImage(String text, String groupName) throws IOException {
        return createImage(text, groupName, String.valueOf(IdGenerator.getSequenceId()));
    }

    /**
     * 获取名字头像
     * @param text 张三
     * @param groupName 存储
     * @param storeName 存储的文件名，可能是用户的id
     * @return
     * @throws IOException
     */
    public String createImage(String text, String groupName, String storeName) throws IOException {
        byte[] content = NameImageCreator.generateImg(text);

        StoreResponse store = inputStreamStore.store(groupName, storeName, "png", new ByteArrayInputStream(content));
        return inputStreamStore.getURL(groupName, store.getPath());
    }

    private Thumbnails.Builder<BufferedImage> handleImage(BufferedImage bi, ImageParam imageParam, long size, String ext) {
        log.info("image size {}x{}", bi.getWidth(), bi.getHeight());
        Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(bi);

        // 从比例中获取长宽
        if (Objects.nonNull(imageParam.getRw()) && Objects.nonNull(imageParam.getRh())) {
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

        if (Objects.nonNull(imageParam.getW()) && Objects.nonNull(imageParam.getH())) { // 长和宽都有值
            Position position = Positions.CENTER;

            if (Objects.nonNull(imageParam.getPosition())) {
                //TODO 跟zimg表现不一致
                position = imageParam.getPosition();
            }

            if (Objects.isNull(imageParam.getP())) {
                if (size > Constants.COMPRESS_THRESHOLD) {
                    double quality = Constants.COMPRESS_THRESHOLD / size ;
                    builder.outputQuality(quality);
                }

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

        } else { // 哪个有值，设置哪个
            if (Objects.nonNull(imageParam.getW())) {
                builder.width(imageParam.getW());
            }

            if (Objects.nonNull(imageParam.getH())) {
                builder.height(imageParam.getH());
            }
            builder.scale(1.0f);
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