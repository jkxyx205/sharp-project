package com.rick.demo;

import com.rick.demo.test.IServiceProvider;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

/**
 * @author Rick.Xu
 * @date 2023/9/15 21:22
 */
public class SimpleTest {

    @Test
    public void testSpi() {
        ServiceLoader<IServiceProvider> loader = ServiceLoader.load(IServiceProvider.class);
        for (IServiceProvider iServiceProvider : loader) {
           iServiceProvider.sayHello("Rick");
        }
    }


}
