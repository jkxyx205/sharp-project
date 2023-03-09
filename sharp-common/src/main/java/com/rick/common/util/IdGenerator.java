package com.rick.common.util;

import com.rick.common.util.sequence.Sequence;
import lombok.experimental.UtilityClass;

import java.util.Random;

/**
 * @author Rick
 * @createdAt 2021-09-23 21:31:00
 */
@UtilityClass
public class IdGenerator {

    private static final Sequence sequence =   new Sequence(0);

    /**
     * 生成主键(19位数字)
     * 主键生成方式,年月日时分秒毫秒的时间戳+三位随机数保证不重复
     */
    public static Long getSimpleId() {
        //当前系统时间戳精确到毫秒
        Long simple = System.currentTimeMillis();
        //随机数
        int random = new Random().nextInt(900) + 100;//为变量赋随机值100-999;
        return Long.parseLong( simple.toString() + random);
    }

    public static Long getSequenceId() {
        return sequence.nextId();
    }

}
