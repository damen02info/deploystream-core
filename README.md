# 🚀 DeployStream - Backend API

Backend principal de **DeployStream**, un proyecto personal pensado como un pequeño **laboratorio de despliegues, CI/CD e infraestructura**.

La idea es bastante sencilla: conectar un frontend Angular con **Spring Boot, PostgreSQL y Jenkins** para poder lanzar despliegues reales, ver lo que está pasando en tiempo real y gestionar cambios temporales en la infraestructura.

En otras palabras: un proyecto para experimentar con todo ese mundo que normalmente está detrás de un simple botón de *"Deploy"*. 😄

## 🛠️ Tech Stack

* **Java + Spring Boot**

  * Spring Web
  * Spring Data JPA
  * Spring Async
* **PostgreSQL**
* **Jenkins REST API**
* **REST / HTTP**
* **Server-Sent Events (SSE)**
* **Angular** como frontend

## 🏗️ ¿Qué hace el backend?

El backend funciona como el punto intermedio entre el frontend, Jenkins y PostgreSQL.

Desde Angular se pueden lanzar acciones que terminan ejecutando **pipelines reales en Jenkins**, mientras que el backend se encarga de coordinar todo el proceso.

### ⚡ Ejecuciones asíncronas

Los procesos que pueden tardar más de lo normal no bloquean la petición HTTP.

Spring `@Async` permite delegar determinadas tareas a hilos secundarios, de forma que el frontend recibe una respuesta rápidamente mientras el backend continúa trabajando por detrás.

Esto resulta especialmente útil cuando hay que iniciar un pipeline, esperar a Jenkins o procesar información durante el despliegue.

### 📡 Logs en tiempo real con SSE

Una de las partes que más me interesaba probar era poder ver el pipeline **como si estuviera conectado directamente a la terminal de Jenkins**.

El backend obtiene los logs de ejecución y los va enviando al frontend mediante **Server-Sent Events (SSE)**.

Así, en lugar de hacer polling constantemente preguntando:

> "¿Hay nuevos logs?"

el frontend mantiene una conexión abierta y recibe los eventos a medida que van llegando.

Resultado: una terminal de despliegue prácticamente en tiempo real. 🖥️

### 🔄 Estado temporal y rollback

DeployStream también permite modificar determinados valores de configuración almacenados en PostgreSQL para representar cambios temporales en el entorno.

Por ejemplo, se puede aplicar un cambio visual durante un tiempo determinado.

Cuando termina ese periodo, un proceso en segundo plano se encarga de ejecutar automáticamente el **rollback**, restaurando los valores originales en `public.app_config`.

La idea es poder experimentar con estados temporales sin dejar el sistema en un estado modificado permanentemente.

## 🧩 Arquitectura

A grandes rasgos, el flujo es:

```text
Angular
   │
   │ REST / SSE
   ▼
Spring Boot API
   │
   ├──────────────► PostgreSQL
   │
   └──────────────► Jenkins
                         │
                         ▼
                    CI/CD Pipeline
                         │
                         ▼
                       Logs
                         │
                         └──────► SSE ──────► Angular
```

El backend se encarga de coordinar las diferentes piezas y mantener al frontend informado de lo que está ocurriendo.

## 🔧 Requisitos

Para ejecutar el backend necesitas:

* **JDK 17+**
* **PostgreSQL**
* **Jenkins**
* Jenkins accesible desde el servidor donde se ejecuta la API
* Un **Jenkins API Token**

## ⚙️ Configuración

Las credenciales y endpoints se configuran mediante variables del entorno de ejecución.

Ejemplo de `application.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

# Jenkins
jenkins.server.url=http://<JENKINS_HOST>:<PORT>
jenkins.api.user=<JENKINS_USER>
jenkins.api.token=<JENKINS_TOKEN>
jenkins.job.name=<DEPLOY_JOB_NAME>
```

> 🔐 Las credenciales reales no forman parte del repositorio. Utiliza variables de entorno o un sistema seguro de gestión de secretos en tu entorno de despliegue.

## 🎯 ¿Por qué este proyecto?

DeployStream nació principalmente como un **laboratorio personal** para juntar varias cosas que me interesan:

* Backend con Spring Boot
* APIs REST
* Programación asíncrona
* Streaming de información en tiempo real
* PostgreSQL
* Jenkins y CI/CD
* Automatización
* Infraestructura
* Monitorización de procesos

Más que intentar crear otra aplicación CRUD, la idea es tener un entorno donde pueda **experimentar con despliegues y automatización de verdad** y seguir ampliándolo poco a poco.

