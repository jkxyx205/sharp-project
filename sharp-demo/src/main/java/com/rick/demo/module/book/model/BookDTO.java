package com.rick.demo.module.book.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.rick.demo.module.book.entity.Book;
import com.rick.demo.module.book.entity.Tag;
import com.rick.demo.module.project.domain.entity.Person;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.TestCodeEnum;
import com.rick.demo.module.school.entity.School;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-10-27 18:09:00
 */
@Setter
public class BookDTO extends Book {

    private Long personId;

    private List<Long> tagIds;

    /**
     * 扩展字段
     */
    private SexEnum sex;

    /**
     * 扩展字段
     */
    private School.TypeEnum type;

    @JsonAlias("testCode")
    private TestCodeEnum testCode;

    public void setPersonId(Long personId) {
        this.setPerson(Person.builder().id(personId).build());
    }

    public void setTagIds(List<Long> tagIds) {
        if (CollectionUtils.isNotEmpty(tagIds)) {
            List<Tag> tagList = new ArrayList<>();

            for (Long tagId : tagIds) {
                tagList.add(Tag.builder().id(tagId).build());
            }
            setTagList(tagList);
        }
    }

//    @Override
//    public Person getPerson() {
//        if (super.getPerson() != null) {
//            return super.getPerson();
//        }
//
//        if (personId != null) {
//            return Person.builder().id(personId).build();
//        }
//
//        return null;
//    }
//
//    @Override
//    public List<Tag> getTagList() {
//        if (getTagList() != null) {
//            return getTagList();
//        }
//
//        if (CollectionUtils.isNotEmpty(tagIds)) {
//            List<Tag> tagList = new ArrayList<>();
//
//            for (Long tagId : tagIds) {
//                tagList.add(Tag.builder().id(tagId).build());
//            }
//            return tagList;
//        }
//
//        return null;
//    }

}
