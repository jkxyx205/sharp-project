package com.rick.formflow.form.valid.core;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:19:00
 */
public interface Validator<T> {

   void valid(T t);

   ValidatorTypeEnum getValidatorType();

   String getMessage();

}
