package com.rick.meta.props.service;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/8/3 10:10
 */
final public class PropertyUtils {

     private PropertyUtils(){}

     static Map<String, String> map = new HashMap<>();

     public static String getProperty(String name) {
          if (StringUtils.isBlank(name)) {
               return null;
          }

          return map.get(name);
     }
}
