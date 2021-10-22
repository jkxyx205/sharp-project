package com.rick.common.validate;

import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-10-08 17:59:00
 */
@RequiredArgsConstructor
public class ValidatorHelper {

    private final Validator validator;

//    public <T> void validate(T t) throws BindException {
//        Map<String, Object> map = new HashMap<>(16);
//        BindingResult errors =  new MapBindingResult(map, t.getClass().getName());
//        Set<ConstraintViolation<T>> validate = validator.validate(t);
////        validator.validate(t, errors);
//        if (errors.hasErrors()) {
////            String errorMessage = errors.getAllErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(","));
////            throw new IllegalArgumentException(errorMessage);
//            if (errors.hasErrors()) {
//                throw new BindException(errors);
//            }
//        }
//    }

    public <T> void validate(T target){
        Set<ConstraintViolation<T>> result = validator.validate(target);
        handResult(result);
    }

    public <T> void validate(T target, Method methodToValidate, Object[] parameterValues){
        ExecutableValidator execVal = validator.forExecutables();
        Set<ConstraintViolation<T>> result = execVal.validateParameters(target, methodToValidate, parameterValues, new Class[]{});
        handResult(result);
    }

    private <T> void handResult(Set<ConstraintViolation<T>> result) {
        if (!result.isEmpty()) {
            throw new ConstraintViolationException(result);
        }
    }

}
