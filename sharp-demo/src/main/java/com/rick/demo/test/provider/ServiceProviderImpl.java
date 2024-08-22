package com.rick.demo.test.provider;

import com.rick.demo.test.IServiceProvider;

/**
 * @author Rick.Xu
 * @date 2023/9/15 21:20
 */
public class ServiceProviderImpl implements IServiceProvider {

    @Override
    public void sayHello(String name) {
        System.out.println("hello " + name);
    }
}
