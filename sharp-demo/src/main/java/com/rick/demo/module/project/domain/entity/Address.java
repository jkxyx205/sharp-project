package com.rick.demo.module.project.domain.entity;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

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

    @Override
    public String toString() {
        try {
            return JsonUtils.toJson(this);
        } catch (IOException e) {
            return null;
        }
    }

}
