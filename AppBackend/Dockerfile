# ---------- Etapa de compilacion ----------
# Se utilizan las versiones exigidas por el documento: JDK 11 y Maven 3.6+
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /proyecto
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa de ejecucion ----------
FROM eclipse-temurin:11-jre
WORKDIR /aplicacion
COPY --from=build /proyecto/target/appbackend.jar aplicacion.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "aplicacion.jar"]
