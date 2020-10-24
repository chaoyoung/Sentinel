package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ApolloCommonService {

    private static final Logger logger = LoggerFactory.getLogger(ApolloCommonService.class);
    /**
     * 没有找到配置项，apollo 返回的错误码
     */
    private static final int NO_FOUND_ERROR_CODE = 404;

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Value("${env:DEV}")
    private String env;
    @Value("${apollo.app.id:sentinel-apollo}")
    private String appId;
    @Value("${apollo.cluster.name:default}")
    private String clusterName;
    @Value("${apollo.namespace.name:application}")
    private String namespaceName;
    @Value("${apollo.modify.user:sentinel}")
    private String modifyUser;
    @Value("${apollo.modify.comment:modify by sentinel-dashboard}")
    private String modifyComment;
    @Value("${apollo.release.comment:release by sentinel-dashboard}")
    private String releaseComment;
    @Value("${apollo.release.user:sentinel}")
    private String releaseUser;

    /**
     * 获得规则类型
     *
     * @param appName app 名称
     * @return
     * @throws Exception
     */
    public <T> List<T> getRules(String appName, String flowDataIdSuffix, Class<T> ruleClass) throws Exception {
        // flowDataId
        String flowDataId = appName + "-" + flowDataIdSuffix;
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }

        List<T> flow = JSON.parseArray(rules,ruleClass);
        if (Objects.isNull(flow)){
            return new ArrayList<>();
        }

        return flow;
    }

    /**
     * 设置规则类型
     *
     * @param rules
     * @throws Exception
     */
    public void publishRules(String appName, String flowDataIdSuffix, String rules) throws Exception {
        // flowDataId
        String flowDataId = appName + "-" + flowDataIdSuffix;
        AssertUtil.notEmpty(appName, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(rules);
        openItemDTO.setComment(modifyComment);
        openItemDTO.setDataChangeCreatedBy(modifyUser);
        try {
            apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);
        } catch (Exception e) {
            if (e.getCause() instanceof ApolloOpenApiException) {
                ApolloOpenApiException apolloOpenApiException = (ApolloOpenApiException) e.getCause();
                if (Integer.valueOf(NO_FOUND_ERROR_CODE).equals(apolloOpenApiException.getStatus())) {
                    apolloOpenApiClient.createItem(appId, env, clusterName, namespaceName, openItemDTO);
                    logger.info("初始化应用配置 -> {}", flowDataId);
                }
            } else {
                logger.error("", e);
            }
        }
        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment(releaseComment);
        namespaceReleaseDTO.setReleasedBy(releaseUser);
        namespaceReleaseDTO.setReleaseTitle(releaseComment);
        apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);
    }

} 