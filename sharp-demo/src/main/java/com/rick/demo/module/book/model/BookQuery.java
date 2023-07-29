package com.rick.demo.module.book.model;

import com.rick.db.plugin.dao.annotation.Sql;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.demo.module.book.entity.Tag;
import com.rick.demo.module.project.domain.entity.Person;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/7/29 18:23
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class BookQuery {
    
    Long id;

    String title;

    String personId;

    @Sql(value = "select * from t_person where id = :personId", params = "personId@personId")
    Person person;

    /**
     * 标签
     */
    @Sql(value = "select t_tag.* from t_book, t_tag, t_book_tag where t_book_tag.book_id = t_book.id AND t_book_tag.tag_id = t_tag.id AND t_book.id = :id", params = "id@id")
    List<Tag> tagList;

    @Sql("select * from t_person")
    List<Person> allPerson;

    @Sql("select * from t_person where id = 552098712424472576")
    Person p1;

    @Sql(value = "select * from t_person where id = :id", params = "id@person.id")
    Person p2;

    @Sql(value = "select * from t_person where id = :id AND name like :title", params = "id@person.id, title@title")
    Person p3;
}