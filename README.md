# Construrrenta App Mobile

Esta es la aplicación móvil para el ecosistema **Construrrenta**, diseñada para interactuar con nuestro robusto backend desarrollado en Spring Boot.

## 🚀 Repositorio del Backend

El código fuente del backend se encuentra disponible en el siguiente repositorio de GitHub:
👉 **[proyecto-spring-boot](https://github.com/victormoreno-2007/proyecto-spring-boot.git)**

## 🛠️ Requisitos Previos

Para que esta aplicación móvil funcione correctamente, es necesario tener el backend en ejecución. El backend utiliza una base de datos **MySQL**, la cual puede ser configurada fácilmente mediante Docker.

### Software necesario:
- **Java JDK 17** (o superior) para compilar y ejecutar el proyecto Spring Boot.
- **Docker y Docker Compose** (Recomendado) o una instalación local de MySQL.
- **Git** para clonar el repositorio.

## ⚙️ Instrucciones de Ejecución del Backend

Sigue estos pasos para inicializar el backend en tu entorno local:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/victormoreno-2007/proyecto-spring-boot.git
   cd proyecto-spring-boot
   ```

2. **Inicializar la base de datos MySQL (con Docker):**
   Puedes levantar la base de datos rápidamente usando Docker. Ejecuta el siguiente comando (si el repositorio cuenta con un archivo `docker-compose.yml`):
   ```bash
   docker-compose up -d
   ```
   O también puedes levantar un contenedor de MySQL directamente:
   ```bash
   docker run --name mysql-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=construrrenta_db -p 3306:3306 -d mysql:8.0
   ```
   *Nota: Asegúrate de configurar las credenciales correctas en el archivo `application.properties` o `application.yml` del backend para que coincidan con la base de datos.*

3. **Ejecutar la aplicación Spring Boot:**
   Dependiendo de cómo esté configurado el proyecto, puedes ejecutarlo usando Maven o Gradle.
   
   Usando Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Usando Gradle:
   ```bash
   ./gradlew bootRun
   ```

4. **Verificación:**
   Una vez que el backend esté corriendo sin errores en tu máquina, asegúrate de que la aplicación móvil apunte a la IP y puerto correctos. Si estás probando la app en un emulador de Android y el backend se ejecuta en tu misma PC, la URL base debería ser `http://10.0.2.2:8080`.

## 📱 Configuración de la App Móvil

1. Abre este proyecto en **Android Studio**.
2. Sincroniza los archivos de Gradle.
3. Asegúrate de configurar la URL base de tu API en las configuraciones de red del proyecto (por ejemplo, en tu cliente Retrofit) para apuntar a la instancia del backend.
4. Ejecuta la app en tu emulador o dispositivo físico.

---
*Desarrollado para la plataforma Construrrenta.*
