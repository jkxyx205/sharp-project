package com.rick.formflow.form.service;

/**
 * @author Rick.Xu
 * @date 2023/6/3 23:04
 */
public interface CpnValueConverter<K, V> {
    V convert(K k);
}
