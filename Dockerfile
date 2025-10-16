FROM openjdk:17

WORKDIR /usrapp/bin

ENV PORT=8080
ENV DB_URL=jdbc:mysql://172.31.34.200:3306/properties?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
ENV DB_USER=root
ENV DB_PASS=root

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["java","-cp","./classes:./dependency/*","co.edu.escuelaing.propertiesapi.PropertiesapiApplication"]