package com.rick.demo.module.book.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.common.http.json.deserializer.NamePropertyDeserializer;
import com.rick.common.http.web.param.ParamName;
import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.*;
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
public class Book extends BaseEntityWithLongId {

    private String title;

    @ParamName("user_id")
    @Transient
    private String userId;

    /**
     * 书的拥有者
     */
    @ParamName({"person_id", "personId"})
    @JsonAlias("personId")
//    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @JsonDeserialize(using = NamePropertyDeserializer.class)
    @ManyToOne(value = "person_id", parentTable = "t_person")
    private Person person;

    /**
     * 标签
     */
    @ParamName("tag_ids")
    @JsonAlias({"tagIds", "tagIdList"})
//    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @JsonDeserialize(using = NamePropertyDeserializer.class)
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
