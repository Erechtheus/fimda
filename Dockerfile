FROM openjdk:8-jre-alpine

#ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/fimda/fimda.jar"]
ENTRYPOINT ["java", "-cp", "/usr/share/fimda/fimda.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
#ADD target/lib           /usr/share/fimda/lib
# Add the service itself
ARG JAR_FILE
RUN echo $JAR_FILE
ADD target/${JAR_FILE} /usr/share/fimda/fimda.jar

# hack
COPY src/main/resources/desc/SethTypeSystem.xml /SethTypeSystem.xml

EXPOSE 8080