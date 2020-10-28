package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengzhongchao
 * @date 2020/10/24
 */
@Service
public class SentinelNacosService {

    private static final Logger logger = LoggerFactory.getLogger(SentinelNacosService.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private NacosProperties nacosProperties;

    public <T> List<T> getRules(String appName, String flowDataIdSuffix, Class<T> ruleClass) throws Exception {
        String rules = configService.getConfig(appName + flowDataIdSuffix, nacosProperties.getGroupId(), 3000);
        logger.info("pull flow rule from Nacos Config: {}", rules);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        List<T> flow = JSON.parseArray(rules, ruleClass);
        return flow;
    }

    public <T> void publishRules(String appName, String flowDataIdSuffix, List<T> rules) throws Exception {
        AssertUtil.notEmpty(appName, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        String rulesStr = JSON.toJSONString(rules);
        configService.publishConfig(appName + flowDataIdSuffix, nacosProperties.getGroupId(), rulesStr);
    }

}
