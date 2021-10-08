package com.rick.common.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-10-08 17:59:00
 */
@RequiredArgsConstructor
public class ValidatorHelper {

    private final Validator validator;

    public <T> void validate(T t) {
        Map<String, Object> map = new HashMap<>(16);
        BindingResult errors =  new MapBindingResult(map, t.getClass().getName());
        validator.validate(t, errors);
        if (errors.hasErrors()) {
            String errorMessage = errors.getAllErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(","));
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
