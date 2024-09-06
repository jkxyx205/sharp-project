package com.rick.demo.module.school.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2022-05-01 13:52:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_school_student", comment = "学生")
public class Student extends BaseEntityWithLongId {

    @Column(comment = "姓名")
    private String name;

    @Column(comment = "年级")
    private Integer grade;

    @Column(comment = "性别")
    private SexEnum sex;

    @ManyToOne(value = "school_id", parentTable = "t_school")
    private School school;

    @AllArgsConstructor
    @Getter
    public enum SexEnum {
        MALE(0, "男"),
        FEMALE(1, "女");

        private static final Map<Integer, SexEnum> codeMap = new HashMap<>();

        static {
            for (SexEnum e : values()) {
                codeMap.put(e.code, e);
            }
        }

        private final int code;
        private final String label;

        @JsonValue
        public int getCode() {
            return code;
        }

        public static SexEnum valueOfCode(int code) {
            return codeMap.get(code);
        }
    }
}