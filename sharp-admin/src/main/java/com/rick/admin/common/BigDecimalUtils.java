package com.rick.admin.common;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:36
 */
@UtilityClass
public class BigDecimalUtils {

    public boolean eq(BigDecimal b1, BigDecimal b2) {
        return b1.compareTo(b2) == 0;
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
}
