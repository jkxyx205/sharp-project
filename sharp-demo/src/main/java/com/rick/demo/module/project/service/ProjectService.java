package com.rick.demo.module.project.service;

import com.rick.demo.module.project.domain.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * @author Rick
 * @createdAt 2021-10-22 17:14:00
 */
@Service
public class ProjectService {

    @Validated
    public void save(@Valid Project project) {

    }
}
