# syntax=docker/dockerfile:experimental
# This is a multi-stage dockerfile. The builder stage extracts the directories that are needed later. Each of the COPY commands relates to the layers extracted by the jarmode.
FROM registry.cn-hangzhou.aliyuncs.com/bawei_efk/java:8 as builder
WORKDIR /opt
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /opt/app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# This is a multi-stage dockerfile. The builder stage extracts the directories that are needed later. Each of the COPY commands relates to the layers extracted by the jarmode.
FROM registry.cn-hangzhou.aliyuncs.com/bawei_efk/java:8
LABEL MAINTAINER="DevOps"
WORKDIR /opt
COPY --from=builder opt/dependencies .
COPY --from=builder opt/spring-boot-loader .
COPY --from=builder opt/snapshot-dependencies .
COPY --from=builder opt/application .
ENV JAVA_OPTS='' \
    ENV='DEV' \
    NACOS_SERVER_ADDR='127.0.0.1:8848' \
    NACOS_GROUP='SENTINEL_GROUP' \
    NACOS_USERNAME='' \
    NACOS_PASSWORD='' \
    NACOS_NAMESPACE=''
ENTRYPOINT ["sh", "-c", "java \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseCGroupMemoryLimitForHeap \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=logs \
    -XX:ErrorFile=logs/hs_err_pid%p.log \
    $JAVA_OPTS \
    -XshowSettings:vm \
    -Dnacos.server-addr=$NACOS_SERVER_ADDR \
    -Dnacos.group-id=$NACOS_GROUP \
    -Dnacos.username=$NACOS_USERNAME \
    -Dnacos.password=$NACOS_PASSWORD \
    -Dnacos.namespace=$NACOS_NAMESPACE \
    -Dspring.profiles.active=$ENV \
    org.springframework.boot.loader.JarLauncher \
    ${0} ${@} "]