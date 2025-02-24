FROM gradle:jdk23

WORKDIR /etc/opt/stock-market-collector

COPY ./src ./src
COPY ./build.gradle ./flyway.conf ./settings.gradle ./

RUN gradle jar

CMD java -jar stock-market-collector-1.0-SNAPSHOT.jar