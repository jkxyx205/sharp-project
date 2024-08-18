package com.rick.admin.module.common.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.BaseCodeDescriptionEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2024/2/20 15:46
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_code_description", comment = "编号-描述 表")
public class CodeDescription extends BaseCodeDescriptionEntity {

    CategoryEnum category;

    Integer sort;

    @AllArgsConstructor
    @Getter
    public enum CategoryEnum {
        /**
         * 物料组
         */
        MATERIAL("物料组"),
        /**
         * 采购组织
         */
        PURCHASING_ORG("采购组织"),
        /**
         * 物料包装组
         */
        PACKAGING("包装组"),
        /**
         * 销售组织
         */
        SALES_ORG("销售组织"),;

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static CategoryEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}