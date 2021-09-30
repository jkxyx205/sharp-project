package com.rick.fileupload.plugin.image;

import lombok.Data;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Position;

import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 参数的作用可以参照：http://zimg.buaa.us/documents/guidebook/
 * IP + port / id (? + width + height + resize_type + grayscale + x + y + rotate + quality + format)
 * @author: Rick.Xu
 * @date: 10/19/18 14:16
 * @Copyright: 2018 www.yodean.com. All rights reserved.
 */
@Data
public class ImageParam {


    private Integer p;

    /**
     * 宽度
     */
    private Integer w;

    /**
     * 高度
     */
    private Integer h;

    /**
     * 旋转
     */
    private Integer r;

    /**
     * 坐标x
     */
    private Integer x;

    /**
     * 坐标x
     */
    private Integer y;

    /**
     * 格式
     */
    private String f;

    /**
     * 清晰度
     */
    private Integer q;

    /**
     * 宽的比例
     */
    private Integer rw;

    /**
     * 高的比例
     */
    private Integer rh;

    private Position position;

    public Position getPosition() {
        if (Objects.isNull(position) && Objects.nonNull(x) && Objects.nonNull(y)) {
            position = new Coordinate(x, y);
        }
        return position;
    }

    public boolean isEmpty() {
        return p == null && w == null && h == null && r == null && x == null && y == null && f == null
                && q == null && rw == null && rh == null && position == null;
    }

    public boolean isSource() {
        return p != null && w == null && h == null && r == null && x == null && y == null && f == null
                && q == null && rw == null && rh == null && position == null;
    }

}
