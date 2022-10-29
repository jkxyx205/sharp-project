package com.rick.demo.module.project.domain.entity;

import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2022-10-29 21:31:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_film", comment = "电影")
public class Film {

    @Id(strategy = Id.GenerationType.ASSIGN)
    private String seqId;

    private String title;
}
