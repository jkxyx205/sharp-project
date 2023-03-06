package com.rick.demo.module.embedded.service;

import com.rick.demo.module.embedded.dao.CompanyDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Rick
 * @createdAt 2023-03-06 14:17:00
 */
@Service
@RequiredArgsConstructor
@Validated
public class CompanyService {

    private final CompanyDAO companyDAO;

}