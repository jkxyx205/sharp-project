package com.rick.demo.module.project.controller;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.demo.module.project.dao.ProjectDAO;
import com.rick.demo.module.project.domain.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-13 19:25:00
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectDAO projectDAO;


    /**
     * http://127.0.0.1:8080/get?id=1&title=title&description=description&coverUrl=http://&ownerId=1&sex=0&status=LOCKED
     * @param project
     * @return
     */
    @GetMapping("get")
    public Result<List<Project>> get(Project project) {
        return ResultUtils.success(projectDAO.selectByParams(project));
    }

    /**
     * {
     * 	"id": "1",
     * 	"title": "title",
     * 	"description": "description",
     * 	"coverUrl": "http://",
     * 	"ownerId": 1,
     * 	"sex": 0,
     * 	"address": {
     * 		"code": "002",
     * 		"detail": "苏州"
     *        },
     * 	"status": "LOCKED",
     * 	"list": [{
     * 			"code": "001",
     * 			"detail": "南京"
     *        },
     *        {
     * 			"code": "002",
     * 			"detail": "苏州"
     *        }
     * 	]
     * }
     * @param project
     * @return
     */
    @PostMapping("post")
    public Result<Project> post(@RequestBody @Valid Project project) {
        projectDAO.insert(project);
        log.info("id: {}", project.getId());

        Project p = projectDAO.selectById(project.getId()).get();
        projectDAO.deleteById(p.getId());

        return ResultUtils.success(p);
    }
}
