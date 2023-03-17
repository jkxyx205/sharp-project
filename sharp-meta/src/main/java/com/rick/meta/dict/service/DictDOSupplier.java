package com.rick.meta.dict.service;

import com.rick.meta.dict.dao.dataobject.DictDO;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2023-03-17 12:09:00
 */
public interface DictDOSupplier {

    List<DictDO> get();
}
