FROM openjdk:17

WORKDIR /usrapp/bin

ENV PORT=8080
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/properties?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["java","-cp","./classes:./dependency/*","co.edu.escuelaing.propertiesapi.PropertiesapiApplication"]