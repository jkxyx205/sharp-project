package com.rick.meta.props.service;

/**
 * @author Rick
 * @createdAt 2021-09-07 04:56:00
 */
public interface PropertyService {

    String getProperty(String name);

    void setProperty(String name, String value);
}
