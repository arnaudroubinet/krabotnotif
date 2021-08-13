FROM openjdk:15
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV SPRING_APPLICATION_JSON='{'discord.hook.url':'url','kraland.user':'user','kraland.password':'password'}'
