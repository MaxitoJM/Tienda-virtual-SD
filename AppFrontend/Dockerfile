# ---------- Etapa de compilacion del archivo WAR ----------
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /proyecto
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package

# ---------- Servidor de aplicaciones Apache Tomcat 9 ----------
FROM tomcat:9.0-jdk11-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /proyecto/target/ciclo3demo.war /usr/local/tomcat/webapps/ciclo3demo.war
EXPOSE 8080
