package com.rick.demo.module.book.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.common.http.json.deserializer.EntityWithLongIdPropertyDeserializer;
import com.rick.common.http.web.param.ParamName;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Sql;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.demo.module.project.domain.entity.Person;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-10-27 17:49:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_book", comment = "书")
public class Book extends BaseEntity {

    private String title;

    /**
     * 书的拥有者
     */
    @ParamName({"person_id", "personId"})
    @JsonAlias("personId")
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @ManyToOne(value = "person_id", parentTable = "t_person")
    private Person person;

    /**
     * 标签
     */
    @ParamName("tag_ids")
    @JsonAlias({"tagIds", "tagList"})
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @ManyToMany(thirdPartyTable = "t_book_tag",
            referenceTable = "t_tag", referenceColumnName = "tag_id", columnDefinition="book_id")
    private List<Tag> tagList;

    @Sql("select * from t_person")
    private List<Person> allPerson;

    @Sql("select * from t_person where id = 552098712424472576")
    private Person p1;

    @Sql(value = "select * from t_person where id = :id", params = "id@person.id")
    private Person p2;

    @Sql(value = "select * from t_person where id = :id AND name like :title", params = "id@person.id, title@title")
    private Person p3;

}
