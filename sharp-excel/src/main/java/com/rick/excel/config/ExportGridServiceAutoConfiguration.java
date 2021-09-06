package com.rick.excel.config;

import com.rick.db.config.GridServiceAutoConfiguration;
import com.rick.db.service.GridService;
import com.rick.excel.plugin.ExportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 自动配置类
 * @author: Rick.Xu
 * @date: 9/12/19 7:42 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Configuration
@ConditionalOnSingleCandidate(GridService.class)
@AutoConfigureAfter({GridServiceAutoConfiguration.class})
public class ExportGridServiceAutoConfiguration {

    public ExportGridServiceAutoConfiguration() {
    }

    @Configuration
    static class ExportGridServiceConfiguration {

        @Autowired
        public void getSQLUtils(GridService gridService) {
            ExportUtils exportUtils = new ExportUtils();
            exportUtils.setGridService(gridService);
        }
    }
}
