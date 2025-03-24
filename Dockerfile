FROM openjdk:17-alpine
RUN echo ${PWD} && ls -lR 
WORKDIR /app
COPY ./target/spring-gateway-0.0.1-SNAPSHOT.jar /app
CMD ["java", "-jar","spring-gateway-0.0.1-SNAPSHOT.jar"]
#CMD ["java", "-jar", "-Djavax.net.debug=all" ,"-Djava.security.debug=all","spring-gateway-0.0.1-SNAPSHOT.jar"]