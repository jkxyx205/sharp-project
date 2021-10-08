package com.rick.common.validate;


import com.rick.common.validate.annotation.PhoneValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rick
 * @version 1.0.0
 * @Description
 * @createTime 2021-01-05 19:38:00
 */
public class PhoneValidator implements ConstraintValidator<PhoneValid, String> {

    private static final String MOBILE_REGEX = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";

    private static final int MOBILE_LENGTH = 11;

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (Objects.isNull(phone)) {
            return true;
        }

        if (phone.length() != MOBILE_LENGTH) {
            return false;
        } else {
            Pattern p = Pattern.compile(MOBILE_REGEX);
            Matcher m = p.matcher(phone);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }
}
