package com.rick.db.repository.support;

import com.rick.db.repository.model.BaseCodeEntity;
import com.rick.db.repository.model.BaseEntity;
import com.rick.db.repository.model.BaseEntityInfo;
import com.rick.db.repository.model.BaseEntityInfoGetter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2025/11/17 13:18
 */
public class ExtendInsertUpdateCallback implements InsertUpdateCallback<BaseEntityInfoGetter> {

    @Override
    public void handler(boolean insert, BaseEntityInfoGetter baseEntityInfoGetter, Map<String, Object> args) {
        BaseEntityInfo baseEntityInfo = new BaseEntityInfo();
        if (insert) {
            baseEntityInfo.setCreateBy((Long) args.get("create_by"));
            baseEntityInfo.setCreateTime((LocalDateTime) args.get("create_time"));
            baseEntityInfo.setUpdateBy((Long) args.get("update_by"));
            baseEntityInfo.setUpdateTime((LocalDateTime) args.get("update_time"));
            baseEntityInfo.setDeleted((Boolean) args.get("is_deleted"));
        } else {
            baseEntityInfo.setCreateBy((Long) args.get("baseEntityInfo.createBy"));
            baseEntityInfo.setCreateTime((LocalDateTime) args.get("baseEntityInfo.createTime"));
            baseEntityInfo.setUpdateBy((Long) args.get("baseEntityInfo.updateBy"));
            baseEntityInfo.setUpdateTime((LocalDateTime) args.get("baseEntityInfo.updateTime"));
            baseEntityInfo.setDeleted((Boolean) args.get("baseEntityInfo.deleted"));
        }

        if (baseEntityInfoGetter instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) baseEntityInfoGetter;
            baseEntity.setBaseEntityInfo(baseEntityInfo);
        } else if (baseEntityInfoGetter instanceof BaseCodeEntity) {
            BaseCodeEntity baseCodeEntity = (BaseCodeEntity) baseEntityInfoGetter;
            baseCodeEntity.setBaseEntityInfo(baseEntityInfo);
        }

    }
}
