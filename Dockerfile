# Etapa 1: construir el WAR con Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: desplegar en Tomcat 10
FROM tomcat:10.1-jdk17
# Limpiar webapps por defecto
RUN rm -rf /usr/local/tomcat/webapps/*
# Copiar nuestro WAR como ROOT.war para que sea la app principal
COPY --from=builder /app/target/comedor.war /usr/local/tomcat/webapps/ROOT.war
# Puerto que Railway asigna
EXPOSE 8080
# Configurar Tomcat para que escuche el puerto de Railway
ENV CATALINA_OPTS="-Dport.http=${PORT:-8080}"
# Tomcat por defecto usa 8080 que coincide con Railway
CMD ["catalina.sh", "run"]
