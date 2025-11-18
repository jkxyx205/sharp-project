package com.rick.db.repository.support.category;

/**
 * @author Rick.Xu
 * @date 2025/11/18 12:05
 */
public interface RowCategory<T extends Enum<T>> {
    T getCategory();

    void setCategory(T t);

}
