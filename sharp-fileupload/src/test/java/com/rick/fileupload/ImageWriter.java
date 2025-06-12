package com.rick.fileupload;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Caption;
import net.coobird.thumbnailator.geometry.Coordinate;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Rick.Xu
 * @date 2025/6/3 18:45
 */

public class ImageWriter {

    @Test
    public void testWrite() {
        try {
            // Input and output file paths
            File inputFile = new File("/Users/rick/Space/tmp/f3.png");
            File outputFile = new File("/Users/rick/Space/tmp/f3-"+System.currentTimeMillis()+".png");

            // Define the caption properties
            String captionText = "Rick.Xu";
            Font font = new Font("Arial", Font.BOLD, 48);
            Color color = Color.RED;
            float opacity = 1.0f; // 0.0f (transparent) to 1.0f (opaque)
            int xPosition = 20; // Pixels from left
            int yPosition = 50; // Pixels from top

            // Create the Caption filter
            Caption caption = new Caption(captionText, font, color, opacity, new Coordinate(xPosition, yPosition), 0);


            // Apply the caption and save the image
            Thumbnails.of(inputFile)
                    .size(1833, 1269) // Optional: Resize image
                    .addFilter(caption) // Add text
                    .toFile(outputFile);

            System.out.println("Text added to image successfully!");

        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    @Test
    public void testWrite2() throws IOException, FontFormatException {
        // 加载原始图片
        BufferedImage originalImage = ImageIO.read(new File("/Users/rick/Space/tmp/f3.png"));

        // 创建一个与原图大小相同的图像
        BufferedImage imageWithText = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // 获取图形上下文
        Graphics2D g2d = imageWithText.createGraphics();

        // 绘制原始图像
        g2d.drawImage(originalImage, 0, 0, null);

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 加载字体文件（确保路径正确）
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("/Users/rick/Space/tmp/Norican/ttf/Norican-Regular.ttf"))
                .deriveFont(Font.PLAIN, 200);  // 设置大小

        // 注册到环境中（可选）
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(customFont);

        // 设置字体和颜色
//        g2d.setFont(new Font("Bradley Hand", Font.BOLD, 200));

//        g2d.setFont(new Font("Brush Script MT", Font.BOLD, 200));
//        g2d.setColor(Color.RED);
        g2d.setFont(customFont);
        g2d.setColor(new Color(84, 44, 133));

        // 要绘制的文字
        String text = "Dingjin Li";

        // 获取文字的宽度和高度
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();

        // 计算居中位置
        int x = (originalImage.getWidth() - textWidth) / 2;
        int y = 590; //(originalImage.getHeight() / 2) + (textHeight / 2);

        // 绘制文字
        g2d.drawString(text, x, y);

        // 清理资源
        g2d.dispose();

        // 使用 Thumbnailator 保存图片（可选择压缩）
        Thumbnails.of(imageWithText)
                .scale(1.0)
                .outputQuality(1.0)
                .toFile("/Users/rick/Space/tmp/f3-"+System.currentTimeMillis()+".png");

        System.out.println("处理完成，保存为 output.jpg");
    }
}
