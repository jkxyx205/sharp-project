package com.rick.demo;

import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import com.rick.meta.props.service.PropertyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class MetaTest {

    @Autowired
    private DictService dictService;
    @Autowired
    private PropertyService propertyService;

    @Test
    public void testList() {
        List<Dict> sexList = dictService.getDictByType("sex");
        assertThat(sexList.size()).isEqualTo(2);
    }

    @Test
    public void testGetOne() {
        Dict dictDO = dictService.getDictByTypeAndName("sex", "F").get();
        assertThat(dictDO.getLabel()).isEqualTo("女");
    }

    @Test
    public void testDictYml() {
        assertThat(dictService.getDictByType("grade").size()).isEqualTo(2);
        assertThat(dictService.getDictByTypeAndName("grade", "g1").get().getLabel()).isEqualTo("一年级");
        assertThat(dictService.getDictByTypeAndName("user", "jkxyx205").get().getLabel()).isEqualTo("Rick");
        assertThat(dictService.getDictByTypeAndName("sex", "M").get().getLabel()).isEqualTo("男");
    }

    @Test
    public void testGetProperty() {
        String property = propertyService.getProperty("hello");
        assertThat(property).isEqualTo("world");
    }
    @Test
    public void testSetProperty() {
        String property = propertyService.getProperty("name");
        propertyService.setProperty("name", "Ashley");

        assertThat(property).isEqualTo("Ashley");
    }
}
