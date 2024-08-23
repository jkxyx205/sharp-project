package com.rick.admin.module.demo.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.fileupload.client.support.Document;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/8/23 16:28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "t_student", comment = "学生表")
public class Student extends BaseEntity {

    String name;

    List<Document> files;

    Document avatar;
}