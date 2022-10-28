package com.rick.demo.module.project.model;

import com.rick.demo.module.project.domain.entity.Person;
import lombok.Setter;

import java.util.List;

/**
 * 请求参数包装
 * @author Rick
 * @createdAt 2022-10-27 17:27:00
 */
@Setter
public class PersonDTO extends Person {

    private Long idCardId;

    private List<Long> roleIds;


}
