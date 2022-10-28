package com.rick.demo.module.book.model;

import com.rick.demo.module.book.entity.Book;
import com.rick.demo.module.book.entity.Tag;
import com.rick.demo.module.project.domain.entity.Person;
import com.rick.demo.module.project.domain.enums.SexEnum;
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

    private SexEnum sex;

    private School.TypeEnum type;

    @Override
    public Person getPerson() {
        if (personId != null) {
            return Person.builder().id(personId).build();
        }
        return super.getPerson();
    }

    @Override
    public List<Tag> getTagList() {
        if (CollectionUtils.isNotEmpty(tagIds)) {
            List<Tag> tagList = new ArrayList<>();

            for (Long tagId : tagIds) {
                tagList.add(Tag.builder().id(tagId).build());
            }
            return tagList;
        }
        return super.getTagList();
    }

}
