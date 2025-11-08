FROM amazoncorretto:25-alpine

ARG JAR_FILE=target/power-component-*.jar
COPY ${JAR_FILE} /opt/app/power-component.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/opt/app/power-component.jar"]

