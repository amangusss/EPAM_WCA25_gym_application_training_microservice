FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests -q dependency:copy-dependencies -DincludeScope=runtime
RUN mvn -DskipTests -q package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./dependency
EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp 'classes:dependency/*' com.github.amangusss.TrainerWorkloadMicroservice"]