package com.rick.meta.dict.service;



import com.rick.meta.dict.entity.Dict;

import java.util.List;

/**
 * 由应用程序提供字典值
 * @author Rick
 * @createdAt 2023-03-17 12:09:00
 */
public interface DictDOSupplier {

    List<Dict> get();
}
