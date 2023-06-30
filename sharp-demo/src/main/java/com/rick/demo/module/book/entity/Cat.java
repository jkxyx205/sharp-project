package com.rick.demo.module.book.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/30 11:51
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "cat", comment = "猫")
public class Cat extends BaseEntity {

    String name;

    int age;

    @OneToMany(subTable = "cat_reward", reversePropertyName = "catId", cascadeInsertOrUpdate = true, joinValue = "cat_id")
    List<Reward> rewardList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "cat_reward", comment = "猫的证书")
    public static class Reward extends BaseEntity {
        String title;

        Long catId;
    }

}