package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:36
 */
@UtilityClass
public class BigDecimalUtils {

    public boolean eq(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) == 0;
    }

    public boolean neq(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) != 0;
    }

    public boolean lt(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) == -1;
    }

    public boolean le(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) <= 0 ;
    }

    public boolean gt(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) == 1;
    }

    public boolean ge(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) >= 0;
    }


    /**
     * 单位小数，四舍五入
     * @param value
     * @return
     */
    public String formatBigDecimalValue(BigDecimal value) {
        return formatBigDecimalValue(value, 2, RoundingMode.HALF_UP);
    }

    public String formatBigDecimalValue(BigDecimal value, int newScale, RoundingMode roundingMode) {
        if (value == null) {
            return "";
        }

        return String.format("%."+newScale+"f", value.setScale(newScale, roundingMode).stripTrailingZeros());
    }
}
