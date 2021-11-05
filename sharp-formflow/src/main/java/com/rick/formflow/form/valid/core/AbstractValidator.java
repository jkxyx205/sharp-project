package com.rick.formflow.form.valid.core;

/**
 * @author Rick
 * @createdAt 2021-11-05 12:00:00
 */
public abstract class AbstractValidator<T> implements Validator<T> {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Validator && obj.getClass() == getClass()) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
