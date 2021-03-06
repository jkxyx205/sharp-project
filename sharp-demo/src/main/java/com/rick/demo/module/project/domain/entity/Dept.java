package com.rick.demo.module.project.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rick
 * @createdAt 2021-09-12 09:45:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dept {
    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 父部门ID。
     */
    private Long parentId;

    @Override
    public String toString() {
        return "id="+id+"&name="+name+"&parentId="+parentId+"";
    }
}
