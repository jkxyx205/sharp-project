package com.rick.demo.module.form.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Rick
 * @createdAt 2023-03-16 23:19:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_user", comment = "人员信息表")
public class User extends BaseEntity {

    @Column(comment = "姓名", nullable = false)
    private String name;

    @Column(comment = "性别", nullable = false)
    private GenderEnum gender;

    @Column(comment = "年龄", nullable = false)
    private Integer age;

    @Column(comment = "出生时间")
    private LocalDate birthday;

    @Column(comment = "手机号码", nullable = false)
    private String mobile;

    @Column(comment = "邮箱")
    private String email;

    @Column(comment = "籍贯", nullable = false)
    private String nativePlace;

    @Column(comment = "兴趣爱好", value = "hobby", nullable = false)
    private List<HobbyEnum> hobbyList;

    @Column(comment = "婚否")
    private Boolean marriage;

    @Column(comment = "自我介绍", columnDefinition = "text", nullable = false)
    private String introduce;

    @Column(comment = "附件", columnDefinition = "text", value = "attachment", nullable = false)
    private List<Map<String, Object>> attachmentList;

    @Column(comment = "学习经历", columnDefinition = "text", value = "school_experience")
    private List<List<String>> schoolExperienceList;

    @Column(comment = "用户状态")
    private StatusEnum status;

    @AllArgsConstructor
    @Getter
    public enum GenderEnum {
        UNKNOWN(0, "Unknown"),
        MALE(1, "Male"),
        FEMALE(2, "Female");
        private static final Map<Integer, GenderEnum> codeMap = new HashMap<>();
        static {
            for (GenderEnum e : values()) {
                codeMap.put(e.code, e);
            }
        }
        private final int code;
        private final String label;
        @JsonValue
        public int getCode() {
            return this.code;
        }

        /**
         * code枚举 必须重写toString()
         * @return
         */
        @Override
        public String toString() {
            return String.valueOf(code);
        }

        public static GenderEnum valueOfCode(int code) {
            return codeMap.get(code);
        }
    }

    @Getter
    public enum StatusEnum {
        LOCKED, NORMAL
    }

    @AllArgsConstructor
    @Getter
    public enum HobbyEnum {
        BASKETBALL("篮球"),
        FOOTBALL("足球"),
        BADMINTON("羽毛球"),
        VOLLEYBALL("排球");

        @JsonValue
        public String getCode() {
            return this.name();
        }
        private final String label;
        public static HobbyEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

}
