FROM registry.cn-hangzhou.aliyuncs.com/bawei_efk/java:8 as builder
LABEL MAINTAINER="zhongchao.cheng"
ARG JAR_FILE=target/*.jar
WORKDIR /opt
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS=''
ENTRYPOINT ["sh", "-c", "java \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseCGroupMemoryLimitForHeap \
    -XshowSettings:vm \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/opt/logs/HeapDumpOnOutOfMemoryError.log \
    $JAVA_OPTS \
    -jar app.jar ${0} ${@}"]
# Docker构建命令：docker build -t registry.cn-hangzhou.aliyuncs.com/cspace/sentinel-dashboard:1.8.0 .
# Maven构建命令（依赖Spotify Maven Plugin）：mvn clean package dockerfile:build
