# Etapa 1: construir el WAR con Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: desplegar en Tomcat 10
FROM tomcat:10.1-jdk17

# Configurar zona horaria de Mexico (Veracruz/CDMX)
ENV TZ=America/Mexico_City
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Limpiar webapps por defecto
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar nuestro WAR como ROOT.war para que sea la app principal
COPY --from=builder /app/target/comedor.war /usr/local/tomcat/webapps/ROOT.war

# Configurar JVM con zona horaria de Mexico
ENV JAVA_OPTS="-Duser.timezone=America/Mexico_City"
ENV CATALINA_OPTS="-Duser.timezone=America/Mexico_City"

# Puerto que Railway asigna
EXPOSE 8080

CMD ["catalina.sh", "run"]