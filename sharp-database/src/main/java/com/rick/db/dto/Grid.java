package com.rick.db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by Rick.Xu on 2016/03/22.
 */
@Value
@Builder
public class Grid<T> implements Serializable {

    /**
     * 当前页面索引
     */
    private int page;

    /**
     * 一页显示记录条数
     */
    @JsonProperty("pageSize")
    private int pageSize;

    /***
     * 总纪录数
     */
    private long records;

    /***
     * 总页数
     */
    @JsonProperty("totalPages")
    private long totalPages;

    /***
     * 数据项
     */
    private List<T> rows;

    public static <T> Grid<T> emptyInstance(int pageSize) {
        Grid<T> grid = Grid.builder().totalPages(0)
                .page(1)
                .rows(Collections.EMPTY_LIST)
                .pageSize(pageSize)
                .records(0)
                .build();
        return grid;
    }
}