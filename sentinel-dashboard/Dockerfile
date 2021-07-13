# This is a multi-stage dockerfile. The builder stage extracts the directories that are needed later. Each of the COPY commands relates to the layers extracted by the jarmode.
FROM registry.cn-hangzhou.aliyuncs.com/bawei_efk/java:8
LABEL MAINTAINER="zhongchao.cheng"
WORKDIR /opt
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS='' \
    ENV='dev' \
    NACOS_SERVER_ADDR='127.0.0.1:8848'
ENTRYPOINT ["sh", "-c", "java \
    -Djava.security.egd=file:/dev/./urandom \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseCGroupMemoryLimitForHeap \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/opt/logs/HeapDumpOnOutOfMemoryError.log \
    $JAVA_OPTS \
    -XshowSettings:vm \
    -Dnacos.server-addr=$NACOS_SERVER_ADDR \
    -Dspring.profiles.active=$ENV \
    -jar /opt/app.jar \
    ${0} ${@} "]