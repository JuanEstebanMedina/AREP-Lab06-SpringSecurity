# AREP-Lab06-SpringSecurity
Proyecto: Sistema CRUD para gestión de propiedades

## Resumen del proyecto
Este repositorio contiene una aplicación backend en Java (Spring Boot) que implementa un sistema CRUD para gestionar propiedades (inmuebles). La API permite crear, leer, actualizar y eliminar propiedades, además de búsquedas avanzadas por dirección, texto libre, rango de precio y rango de tamaño.

Principales responsabilidades:
- Almacenar y consultar propiedades en una base de datos MySQL.
- Exponer endpoints REST para operaciones CRUD y búsqueda.
- Validar entradas y manejar errores de forma centralizada.

## Video de despliegue
https://youtu.be/JL5P3XX3E4g

<video src="https://youtu.be/JL5P3XX3E4g" controls width="720"></video>

## Arquitectura del sistema

Componentes principales:
- Frontend: (se encuentra en `resources/static`) Puede ser cualquier cliente HTTP/SPA que consuma la API REST.
- Backend: Aplicación Spring Boot ubicada en `src/main/java` que expone endpoints REST en el puerto 8080.
- Base de datos: MySQL. En el repositorio se incluye un `docker-compose.yml` que levanta un servicio `mysql` y el servicio `app` (la API) para desarrollo local.

Interacción:
1. El cliente (frontend o curl/Postman) realiza peticiones HTTP al backend.
2. El backend usa JPA/Hibernate para persistir y consultar datos en MySQL.
3. Las respuestas se devuelven en formato JSON y los errores se manejan por un `@ControllerAdvice` global.

Diagrama simplificado:

Client <--HTTP--> Backend (Spring Boot) <--JPA/Hibernate--> MySQL

En local se puede ejecutar con Docker Compose: el contenedor `mysql` y el contenedor `app` se comunican usando la red definida en `docker-compose.yml`.

## Diseño de clases (overview)

Componentes principales del backend:

- `co.edu.escuelaing.propertiesapi.model.entity.Property`
	- Entidad JPA que representa una propiedad.
	- Campos: `id: Long`, `address: String`, `price: BigDecimal`, `size: Double`, `description: String`.

- `co.edu.escuelaing.propertiesapi.model.dto.PropertyDto`
	- DTO usado para recibir/validar datos en las APIs.
	- Contiene validaciones (`@NotBlank`, `@NotNull`, `@Positive`, etc.).

- `co.edu.escuelaing.propertiesapi.repository.PropertyRepository`
	- Extiende `JpaRepository<Property, Long>` y `JpaSpecificationExecutor<Property>` para consultas paginadas y filtradas.

- `co.edu.escuelaing.propertiesapi.service.PropertyService` (interface)
	- Define operaciones: `create`, `list(Pageable)`, `get`, `update`, `delete`, `search(...)`.

- `co.edu.escuelaing.propertiesapi.service.impl.PropertyServiceImpl`
	- Implementación de `PropertyService`.
	- Maneja lógica de negocio y lanza `NoSuchElementException` cuando no encuentra recursos.

- `co.edu.escuelaing.propertiesapi.controller.PropertyController`
	- Expones los endpoints REST (GET/POST/PUT/DELETE) y delega al servicio.
	- Usa `@Valid` para validar `PropertyDto` y devuelve `201 Created` en creación.

- `co.edu.escuelaing.propertiesapi.controller.GlobalExceptionHandler`
	- Manejador global (`@ControllerAdvice`) que captura `MethodArgumentNotValidException`, `NoSuchElementException`, `DataIntegrityViolationException`, `ConstraintViolationException` y excepciones generales para mapearlas a respuestas HTTP adecuadas (400/404/409/500).

## Endpoints principales

Ejemplos (asumimos base `/api/properties`):
- GET `/api/properties` -> listar (paginado) / búsqueda con parámetros opcionales (address, q, minPrice, maxPrice, minSize, maxSize, page, size)
- GET `/api/properties/{id}` -> obtener por id
- POST `/api/properties` -> crear (body: `PropertyDto`)
- PUT `/api/properties/{id}` -> actualizar
- DELETE `/api/properties/{id}` -> eliminar

## Instrucciones de despliegue

Requisitos previos:
- Java 17+ y Maven para generar el `jar` localmente.
- Docker y Docker Compose para pruebas locales.
- Cuenta AWS con permisos para ECS/EC2/RDS según la opción de despliegue.

1) Construir artefacto Java (local)

```bash
mvn clean package
```

2) Construir imagen Docker localmente

```bash
docker build -t rivitas13/arep-lab05-properties:latest .
```

3) Ejecutar en local con Docker Compose (levanta MySQL y la app)

```bash
docker-compose up --build
```

Ajustes importantes en `docker-compose.yml`:
- El servicio `mysql` expone el puerto 3306 y tiene variables de entorno para la contraseña/usuario.
- El servicio `app` usa variables `DB_URL`, `DB_USER`, `DB_PASS` y el `SPRING_PROFILES_ACTIVE`.

4) Publicar la imagen en Docker hub

- Crear un repositorio en dockerhub (o usar uno existente).

```bash
docker push rivitas13/arep-lab05-properties:latest
```

5) Despliegue en AWS (2 EC2)

	1.  **db-ec2**  
	```bash
	sudo yum update -y && sudo yum install -y docker && sudo systemctl enable --now docker
	mkdir -p $HOME/mysql-data
	sudo docker run -d --name mysql_props -p 3306:3306 \
		-e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=properties \
		-v $HOME/mysql-data:/var/lib/mysql --restart unless-stopped mysql:8.0
	```

	2. **app-ec2**
	```bash
	sudo yum update -y && sudo yum install -y docker && sudo systemctl enable --now docker
	sudo docker pull rivitas13/arep-lab05-properties:latest
	sudo docker run -d --name properties-api -p 8080:8080 \
	-e SPRING_PROFILES_ACTIVE=prod \
	-e DB_URL="jdbc:mysql://172.31.34.200:3306/properties?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC" \
	-e DB_USER=root -e DB_PASS=root -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
	--restart unless-stopped rivitas13/arep-lab05-properties:1.0
	```

	3. Accede: http://<IP_PRIVADA_APP>:8080/ (Se utilizada la IP privada pues esta no cambia al reiniciarse y no falla la conexión de la aplicación con la base de datos)

## Manejo de errores y buenas prácticas
- Validar DTOs con anotaciones `jakarta.validation` para asegurar entradas correctas.
- Manejar excepciones de BD (`DataIntegrityViolationException`, `ConstraintViolationException`) en el `@ControllerAdvice` y mapear a 400/409 cuando proceda.

## Screenshots (placeholders)

Pruebas de los endpoints:

1. Crear una propiedad (POST):

<img src = "img/postTest.png">

2. Listar (GET):

<img src = "img/getTest.png">

3. Actualizar (PUT) y Eliminar (DELETE) para completar flujo CRUD.

**Actualizar una propiedad**
<img src = "img/putTest.png">

**Eliminar una propiedad no existente**
<img src = "img/deleteNotFoundTest.png">

**Eliminar una propiedad**
<img src = "img/deleteTest.png">


## Autor

Juan Esteban Medina Rivas - Universidad Escuela Colombiana de Ingeniería Julio Garavito

---
Versión: 1.0 — documentación inicial generada.


