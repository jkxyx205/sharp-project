package com.rick.demo;

import com.rick.common.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick.Xu
 * @date 2024/8/23 08:41
 */
public class FileUtilsTest {

    @Test
    public void isImageType() {
        assertThat(FileUtils.isImageType("java.java")).isFalse();
        assertThat(FileUtils.isImageType(".java")).isFalse();
        assertThat(FileUtils.isImageType("java")).isFalse();

        assertThat(FileUtils.isImageType("a.png")).isTrue();
        assertThat(FileUtils.isImageType(".png")).isTrue();
        assertThat(FileUtils.isImageType("png")).isTrue();
        assertThat(FileUtils.isImageType("中文.png")).isTrue();
    }
}
