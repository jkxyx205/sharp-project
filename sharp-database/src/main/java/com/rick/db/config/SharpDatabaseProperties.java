package com.rick.db.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/12/19 5:41 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */

@ConfigurationProperties(prefix = "sharp.database")
@Data
public class SharpDatabaseProperties {

    private String type = "mysql";

}
