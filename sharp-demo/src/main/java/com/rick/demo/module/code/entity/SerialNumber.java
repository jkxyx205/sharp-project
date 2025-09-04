package com.rick.demo.module.code.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseCodeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2023-03-08 22:19:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "mm_serial_number", comment = "序列号")
public class SerialNumber extends BaseCodeEntity<Long> {

    private SerialNumber.SerialNumberEnum status;

    @AllArgsConstructor
    @Getter
    public enum SerialNumberEnum {
        CREATED("创建"),
        AVAILABLE("在库"),
        SOLD("离库");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static SerialNumberEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

}
