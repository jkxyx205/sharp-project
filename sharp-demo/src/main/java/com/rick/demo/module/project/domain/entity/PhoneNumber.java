package com.rick.demo.module.project.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rick
 * @createdAt 2021-10-14 09:46:00
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumber {

    private String code;

    private String number;

    /**
     * 序列化格式
     * @return
     */
    @Override
    public String toString() {
        return code + "-" + number;
    }
}
