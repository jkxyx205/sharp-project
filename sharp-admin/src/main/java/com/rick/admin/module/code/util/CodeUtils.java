package com.rick.admin.module.code.util;

import com.rick.common.util.Time2StringUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.util.function.Function;

/**
 * @author Rick.Xu
 * @date 2024/9/16 07:17
 */
@UtilityClass
public class CodeUtils {

    /**
     *  prefix + yyyymmddHHMMss
     * @param prefix
     * @return
     */
    public String generateCode(String prefix) {
        return prefix + getTimestamp(time -> time.replaceAll("\\s+|-|:", ""), 4);
    }

    /**
     * prefix + yyyymmdd
     * @param prefix
     * @return
     */
    public String generateCodeWithDay(String prefix) {
        return prefix + getTimestamp(time -> time.replaceAll("\\s+|-|:", "").substring(0, 8), 6);
    }

    public String getTimestamp(Function<String, String> formatTimestamp, int randomNumericLen) {
        String timestamp = formatTimestamp.apply(Time2StringUtils.format(Instant.now()));
        if (randomNumericLen > 0) {
            timestamp = String.valueOf(Long.parseLong(timestamp) + Long.parseLong(RandomStringUtils.randomNumeric(randomNumericLen)));
        }
        return timestamp;
    }
}
