package com.rick.demo.module.info.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2023-03-09 16:12:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_info", comment = "用户信息")
public class Info extends BaseEntity {

    private String name;

    private Integer age;

    private String nativePlace;

    private List<String> hobby;

    private List<String> agree;

    private String remark;

    private String sex;

    private String mobile;

    private List<Map<String, Object>> file;

    private String email;

    private LocalDate date;

    private List<List<String>> info;

}
