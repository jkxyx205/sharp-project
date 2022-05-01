package com.rick.demo.module.school.controller;

import com.rick.demo.module.school.dao.SchoolDAO;
import com.rick.demo.module.school.entity.School;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rick
 * @createdAt 2022-05-01 17:08:00
 */
@RestController
@RequestMapping("schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolDAO schoolDAO;

    @PostMapping
    public School save(@RequestBody School school) {
        schoolDAO.insert(school);
        return schoolDAO.selectById(school.getId()).get();
    }
}
