package com.rick.formflow.form.cpn.core;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-03 17:07:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "sys_form")
public class Form extends BaseCodeEntity {

    @NotBlank
    private String name;

    @Column(comment = "formAdviceName服务的名称")
    private String formAdviceName;

    private String tableName;

    private String repositoryName;

    private StorageStrategyEnum storageStrategy;

    private String tplName;

    private Map<String, Object> additionalInfo;

    @AllArgsConstructor
    @Getter
    public enum StorageStrategyEnum {
        NONE("无"),
        INNER_TABLE("内部表"),
        CREATE_TABLE("外部表");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static Form.StorageStrategyEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

}
