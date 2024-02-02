package com.rick.admin.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author Rick.Xu
 * @date 2023/8/25 02:00
 */
@Getter
@AllArgsConstructor
public class IdQuantity {

    private Long id;

    private String description;

    private BigDecimal quantity;

}
