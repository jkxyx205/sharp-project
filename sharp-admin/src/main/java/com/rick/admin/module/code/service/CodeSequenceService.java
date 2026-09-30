package com.rick.admin.module.code.service;

import com.rick.common.util.Time2StringUtils;
import com.rick.db.plugin.SQLUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CodeSequenceService {

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
     * 预览下一个编号，但不持久化递增（未保存前不占用序号，下次进入仍可使用）。
     * 仅用于新增表单渲染时的默认编号展示，真正占用序号应在保存时调用 {@link #getCodeSequence}。
     */
    public String peekCodeSequence(String category, String prefix) {
        return peekCodeSequence(category, prefix, Time2StringUtils.format(Instant.now()).replaceAll("\\s+|-|:", "").substring(0, 8), 2);
    }

    /**
     * 预览下一个编号，但不持久化递增（未保存前不占用序号，下次进入仍可使用）。
     * 仅用于新增表单渲染时的默认编号展示，真正占用序号应在保存时调用 {@link #getCodeSequence}。
     */
    public String peekCodeSequence(String category, String prefix, String name, int sequenceLen) {
        int sequence = peekNextSequence(category, prefix, name);
        return StringUtils.defaultString(prefix, "") + name + StringUtils.leftPad("" + (sequence + 1), sequenceLen, "0");
    }

    /**
     * 只读查询当前已使用的序号，不递增、不落库。
     */
    private int peekNextSequence(String category, String prefix, String name) {
        return SQLUtils.execute(con -> {
            int sequence = 0;
            try (PreparedStatement queryPreparedStatement = con.prepareStatement("SELECT sequence FROM core_code_sequence WHERE category = ? AND prefix = ? AND name = ?")) {
                queryPreparedStatement.setString(1, category);
                queryPreparedStatement.setString(2, prefix);
                queryPreparedStatement.setString(3, name);

                try (ResultSet resultSet = queryPreparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        sequence = resultSet.getInt(1);
                    }
                }
            }

            return sequence;
        });
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

        synchronized (category) {
            int seq = SQLUtils.execute(con -> {
                int sequence = 0;
                try (PreparedStatement queryPreparedStatement = con.prepareStatement("SELECT sequence FROM core_code_sequence WHERE category = ? AND prefix = ? AND name = ?")){
                    queryPreparedStatement.setString(1, category);
                    queryPreparedStatement.setString(2, prefix);
                    queryPreparedStatement.setString(3, name);

                    try (ResultSet resultSet = queryPreparedStatement.executeQuery()){
                        if (resultSet.next()) {
                            sequence = resultSet.getInt(1);
                        }
                    }
                }

                try (PreparedStatement preparedStatement = con.prepareStatement("UPDATE core_code_sequence SET sequence = ?, name = ?  WHERE category = ? AND prefix = ? ")){
                    preparedStatement.setInt(1, sequence + size);
                    preparedStatement.setString(2, name);
                    preparedStatement.setString(3, category);
                    preparedStatement.setString(4, prefix);
                    preparedStatement.executeUpdate();
                }

                return sequence;
            });

            for (int i = 1; i <= size; i++) {
                sequences[i - 1] = seq + i;
            }
        }

        return sequences;
    }
}