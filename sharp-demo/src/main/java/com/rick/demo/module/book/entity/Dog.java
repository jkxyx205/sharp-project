package com.rick.demo.module.book.entity;

import com.rick.db.plugin.dao.annotation.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author Rick.Xu
 * @date 2023/6/30 11:51
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table // 必须的
public class Dog {

    String id;

    String name;

    Integer age;
}