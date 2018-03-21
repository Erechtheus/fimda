FROM openjdk:8-jre-alpine

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/fimda/fimda.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
#ADD target/lib           /usr/share/fimda/lib
# Add the service itself
ARG JAR_FILE
RUN echo $JAR_FILE
ADD target/${JAR_FILE} /usr/share/fimda/fimda.jar
EXPOSE 8080