package com.rick.demo.module.project.domain.entity;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rick
 * @createdAt 2021-10-12 20:19:00
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address implements JsonStringToObjectConverterFactory.JsonValue {

    private String code;

    private String detail;
}
