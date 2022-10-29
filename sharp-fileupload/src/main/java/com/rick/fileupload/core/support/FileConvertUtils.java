package com.rick.fileupload.core.support;

import com.google.common.collect.Lists;
import com.lowagie.text.pdf.PdfReader;
import lombok.experimental.UtilityClass;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-08 14:11:00
 */
@UtilityClass
public class FileConvertUtils {

    private static final String IMAGE_TYPE_PNG = "png";

    public static List<byte[]> pdf2Image(byte[] data, int dpi) throws IOException {
        PDDocument pdDocument;
        pdDocument = PDDocument.load(data);
        PDFRenderer renderer = new PDFRenderer(pdDocument);
        /* dpi越大转换后越清晰，相对转换速度越慢 */
        PdfReader reader = new PdfReader(data);
        int pages = reader.getNumberOfPages();

        List<byte[]> dataList = Lists.newArrayListWithExpectedSize(pages);

        for (int i = 0; i < pages; i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, dpi);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, IMAGE_TYPE_PNG, bos);
            dataList.add(bos.toByteArray());
            bos.close();
        }

        return dataList;
    }

    public static void pdf2Image(byte[] data, OutputStream os, int dpi) {
        try (PDDocument pdf = PDDocument.load(data)) {
            int actSize = pdf.getNumberOfPages();
            List<BufferedImage> picList = Lists.newArrayList();
            for (int i = 0; i < actSize; i++) {
                BufferedImage image = new PDFRenderer(pdf).renderImageWithDPI(i, dpi, ImageType.RGB);
                picList.add(image);
            }
            // 组合图片
            mergeImages(picList, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mergeImages(List<BufferedImage> picList, OutputStream os) throws IOException {
        // 纵向处理图片
        if (picList == null || picList.size() <= 0) {
            return;
        }
        // 总高度
        int height = 0,
                // 总宽度
                width = 0,
                // 临时的高度 , 或保存偏移高度
                offsetHeight = 0,
                // 临时的高度，主要保存每个高度
                tmpHeight = 0,
                // 图片的数量
                picNum = picList.size();
        // 保存每个文件的高度
        int[] heightArray = new int[picNum];
        // 保存图片流
        BufferedImage buffer = null;
        // 保存所有的图片的RGB
        List<int[]> imgRgb = new ArrayList<int[]>();
        // 保存一张图片中的RGB数据
        int[] tmpImgRgb;
        for (int i = 0; i < picNum; i++) {
            buffer = picList.get(i);
            // 图片高度
            heightArray[i] = offsetHeight = buffer.getHeight();
            if (i == 0) {
                // 图片宽度
                width = buffer.getWidth();
            }
            // 获取总高度
            height += offsetHeight;
            // 从图片中读取RGB
            tmpImgRgb = new int[width * offsetHeight];
            tmpImgRgb = buffer.getRGB(0, 0, width, offsetHeight, tmpImgRgb, 0, width);
            imgRgb.add(tmpImgRgb);
        }
        // 设置偏移高度为0
        offsetHeight = 0;
        // 生成新图片
        BufferedImage imageResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < picNum; i++) {
            tmpHeight = heightArray[i];
            if (i != 0) {
                // 计算偏移高度
                offsetHeight += tmpHeight;
            }
            // 写入流中
            imageResult.setRGB(0, offsetHeight, width, tmpHeight, imgRgb.get(i), 0, width);
        }

        // 写图片
        ImageIO.write(imageResult, IMAGE_TYPE_PNG, os);
    }

}
