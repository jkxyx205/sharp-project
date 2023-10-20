package com.rick.demo.db;

import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.demo.module.book.entity.StorageLocation;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlantStorageLocationTest {

    @Resource
    private EntityCodeDAO<StorageLocation, Long> storageLocationDAO;

    @Test
    public void testInitData() {
//        storageLocationDAO.insertOrUpdate(StorageLocation.builder().plantId(1L).code("A").description("a").build());
//        storageLocationDAO.insertOrUpdate(StorageLocation.builder().plantId(1L).code("B").description("b").build());
//        storageLocationDAO.insertOrUpdate(StorageLocation.builder().plantId(1L).code("C").description("c").build());
//        storageLocationDAO.insertOrUpdate(StorageLocation.builder().plantId(1L).code("D").description("d").build());
//        storageLocationDAO.insertOrUpdate(StorageLocation.builder().plantId(1L).code("E").description("e").build());
    }

    /**
     * 745648278938329088
     * 745648279030603776
     * 745648279080935424
     * 745648279106101248
     * 745648279139655680
     */

    @Test
    public void testSync() {
        storageLocationDAO.insertOrUpdate("plant_id", 1L, Arrays.asList(
                StorageLocation.builder().plantId(1L).id(745648278938329088L).code("A").description("aaaaA1").build(), // 修改
                StorageLocation.builder().plantId(1L).code("B").description("bbbbB1").build(), // 修改
                // C -> E 删除
                StorageLocation.builder().plantId(1L).code("F").description("ffffF1").build() // 新增
        ));
    }

    @Test
    public void testDeleteTable() {
        SQLUtils.delete("plant_storage_location");
    }

    @Test
    public void testInsertOrUpdateTable() {
        storageLocationDAO.insertOrUpdateTable(Arrays.asList(
                StorageLocation.builder().plantId(1L).id(745648278938329088L).code("A").description("aaaaA222").build(), // 修改
                StorageLocation.builder().plantId(1L).code("B").description("bbbbB222").build(), // 修改
                // C -> E 删除
                StorageLocation.builder().plantId(1L).code("F").description("ffffF2222").build() // 新增
        ));
    }

    @Test
    public void testSelectSubTable() {
        List<StorageLocation> locationList = storageLocationDAO.selectSubTable("plant_id", 2L);
        System.out.println(locationList.size());
    }

}
