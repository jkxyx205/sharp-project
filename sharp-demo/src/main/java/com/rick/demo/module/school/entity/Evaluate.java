package com.rick.demo.module.school.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick
 * @createdAt 2022-05-01 13:52:00
 */
@Getter
@AllArgsConstructor
public class Evaluate {

    /**
     * 评价等级
     */
    private Integer grade;

    /**
     * 评价内容
     */
    private String description;

    @Override
    public String toString() {
        return "Evaluate{" +
                "grade=" + grade +
                ", description='" + description + '\'' +
                '}';
    }
}