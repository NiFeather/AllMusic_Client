package com.coloryr.allmusic.client.utils;

import net.minecraft.util.Identifier;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Utils
{
    public static Identifier nameIdentifierFrom(String string)
    {
        var hash = Integer.toHexString(string.hashCode());

        return Identifier.of("allmusic", hash);
    }

    public static BufferedImage scalePicture(BufferedImage image, int targetWidth, int targetHeight)
    {
        Image resultingImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics()
                .drawImage(resultingImage, 0, 0, null);

        return outputImage;
    }

    public static BufferedImage makePictureRounded(int width, int height, BufferedImage image)
    {
        int[] pixels = new int[width * height];

        // 透明底的图片
        BufferedImage formatAvatarImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = formatAvatarImage.createGraphics();
        // 把图片切成一个园
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 留一个像素的空白区域，这个很重要，画圆的时候把这个覆盖
        int border = (int) (width * 0.11);
        // 图片是一个圆型
        Ellipse2D.Double shape = new Ellipse2D.Double(border, border, width - border * 2, width - border * 2);
        // 需要保留的区域
        graphics.setClip(shape);
        graphics.drawImage(image, border, border, width - border * 2, width - border * 2, null);
        graphics.dispose();
        // 在圆图外面再画一个圆
        // 新创建一个graphics，这样画的圆不会有锯齿
        graphics = formatAvatarImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画笔是4.5个像素，BasicStroke的使用可以查看下面的参考文档
        // 使画笔时基本会像外延伸一定像素，具体可以自己使用的时候测试
        int border1;

        border1 = (int) (width * 0.08);
        BasicStroke s = new BasicStroke(border1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        graphics.setStroke(s);
        graphics.setColor(Color.decode("#121212"));
        graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);

        border1 = (int) (width * 0.05);
        float si = (float) (border1 / 6);
        s = new BasicStroke(si, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        graphics.setStroke(s);
        graphics.setColor(Color.decode("#181818"));
        graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);

        border1 = (int) (width * 0.065);
        graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);

        border1 = (int) (width * 0.08);
        graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);

        border1 = (int) (width * 0.095);
        graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);

        graphics.dispose();
/*
        formatAvatarImage.getRGB(
                0,
                0,
                formatAvatarImage.getWidth(),
                formatAvatarImage.getHeight(),
                pixels,
                0,
                formatAvatarImage.getWidth());
*/
        return formatAvatarImage;
    }
}
