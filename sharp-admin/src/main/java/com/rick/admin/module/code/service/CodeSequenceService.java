package com.rick.admin.module.code.service;

import com.rick.common.util.Time2StringUtils;
import com.rick.db.repository.TableDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CodeSequenceService {

    TableDAO tableDAO;

    /**
     * 默认
     * name: yyyymmdd
     * sequenceLen: 2
     * @param category
     * @param prefix
     * @return
     */
    public String getCodeSequence(String category, String prefix) {
        return getCodeSequence(category, prefix, Time2StringUtils.format(Instant.now()).replaceAll("\\s+|-|:", "").substring(0, 8), 2);
    }

    public String getCodeSequence(String category, String prefix, String name, int sequenceLen) {
        return getCodeSequences(category, prefix, name, sequenceLen, 1)[0];
    }

    /**
     *
     * @param category 分类
     * @param prefix 前缀
     * @param name
     * @param sequenceLen 补齐的长度
     * @param size 一次性获取 code 的数量
     * @return
     */
    public String[] getCodeSequences(String category, String prefix, String name, int sequenceLen, int size) {
        int[] sequences = getNextSequences(category, prefix, name, size);

        String[] codeSequences = new String[size];

        for (int i = 0; i < size; i++) {
            codeSequences[i] = StringUtils.defaultString(prefix, "") + name + StringUtils.leftPad("" + sequences[i], sequenceLen, "0");
        }

        return codeSequences;
    }

    public int getNextSequence(String category, String prefix, String name) {
        return getNextSequences(category, prefix, name, 1)[0];
    }

    /**
     * 在表 core_code_sequence 中先维护 String category, String prefix 再调用方法
     * category: CUSTOMER; prefix = C; 表示客户使用前缀C
     * getNextSequences("CUSTOMER", "C", "20241012", 2)
     * @param category 分类
     * @param prefix 前缀
     * @param name 变量
     * @param size 获取序列的总数量
     * @return
     */
    public int[] getNextSequences(String category, String prefix, String name, int size) {
        int[] sequences = new int[size];

        // 单连接事务内 SELECT ... FOR UPDATE 取行锁 + UPDATE，由 DB 行锁保证并发安全（集群也安全），
        // 无需 JVM 锁，也不区分数据库方言。WHERE 必须含 name，避免覆盖同名分类下的其它行。
        int newSeq = tableDAO.execute(con -> {
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                int sequence;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT sequence FROM core_code_sequence WHERE category = ? AND prefix = ? AND name = ? FOR UPDATE")) {
                    ps.setString(1, category);
                    ps.setString(2, prefix);
                    ps.setString(3, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("core_code_sequence row not found: category=" + category + ", prefix=" + prefix + ", name=" + name);
                        }
                        sequence = rs.getInt(1);
                    }
                }

                int newSequence = sequence + size;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE core_code_sequence SET sequence = ? WHERE category = ? AND prefix = ? AND name = ?")) {
                    ps.setInt(1, newSequence);
                    ps.setString(2, category);
                    ps.setString(3, prefix);
                    ps.setString(4, name);
                    ps.executeUpdate();
                }

                con.commit();
                return newSequence;
            } catch (SQLException | RuntimeException e) {
                try { con.rollback(); } catch (SQLException ignore) {}
                throw e;
            } finally {
                try { con.setAutoCommit(originalAutoCommit); } catch (SQLException ignore) {}
            }
        });

        for (int i = 1; i <= size; i++) {
            sequences[i - 1] = newSeq - size + i;
        }

        return sequences;
    }
}