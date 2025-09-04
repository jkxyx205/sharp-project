package com.rick.demo.module.book.entity;

import com.rick.db.repository.OneToMany;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

;

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
public class Cat extends BaseEntity<Long> {

    String name;

    int age;

    @OneToMany(joinColumnId = "cat_id", mappedBy = "catId")
    List<Reward> rewardList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "cat_reward", comment = "猫的证书")
    public static class Reward extends BaseEntity<Long> {
        String title;

        Long catId;
    }

}