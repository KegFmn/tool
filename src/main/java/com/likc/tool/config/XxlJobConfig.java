package com.likc.tool.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */
@Configuration
//@Profile("prod")
public class XxlJobConfig {

    @Value("${xxl-job.url}")
    private String url;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        // "https://xxl-job.xiaofeilun.cn/xxl-job-admin"
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(url);
        xxlJobSpringExecutor.setAppname("recharge-api");
        xxlJobSpringExecutor.setAccessToken("default_token");
        xxlJobSpringExecutor.setLogPath("./logs/xxl-job");
        xxlJobSpringExecutor.setLogRetentionDays(10);

        return xxlJobSpringExecutor;
    }

}