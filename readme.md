# 📱 Android Event Manager con Firebase & FCM

Una aplicación Android nativa construida con **Kotlin y Jetpack Compose** que demuestra una arquitectura "Serverless" completa utilizando **Firebase**.

El proyecto implementa un sistema de gestión de eventos con roles diferenciados (Administrador y Usuario), sincronización en tiempo real mediante Firestore y un sistema de notificaciones push bidireccional automatizado con Cloud Functions.

---

## ✨ Características Principales

### 🔐 Autenticación y Roles
* **Login/Registro:** Autenticación mediante Firebase Auth.
* **Seguridad:** Registro de Administradores protegido por "Contraseña Maestra".
* **Verificación:** Bloqueo de funcionalidades hasta verificar el correo electrónico.

### 👥 Perfil de Usuario (Normal)
* **Creación de Eventos:** Envío de solicitudes de eventos para revisión.
* **Historial de Notificaciones:** Persistencia de mensajes recibidos (Aprobaciones/Rechazos) en Firestore.
* **Estado en Tiempo Real:** Visualización del estado de verificación.

### 🛡️ Perfil de Administrador
* **Dashboard de Control:** Acceso exclusivo a herramientas de gestión.
* **Revisión de Eventos:** Lista de eventos pendientes con opciones para Aprobar o Rechazar y dejar feedback.
* **Notificaciones Masivas:** Capacidad de enviar Push Notifications a todos los usuarios o a una selección específica desde la App.

### ☁️ Backend & Notificaciones (Serverless)
* **Triggers Automáticos:**
    * Nuevo Evento (Usuario) → Notificación Push a todos los Admins (Tópico).
    * Resolución de Evento (Admin) → Notificación Push al Usuario específico + Guardado en Historial.
* **Cloud Functions Callable:** Función segura para que el Admin envíe mensajes masivos sin exponer las claves de FCM en el cliente.

---

## 🛠️ Stack Tecnológico

### Android (Cliente)
* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture (Capas de Dominio/Data/UI)
* **Inyección de Dependencias:** Koin
* **Asincronía:** Coroutines & StateFlow
* **Navegación:** Navigation Compose

### Firebase (Backend)
* **Authentication:** Gestión de identidades.
* **Firestore Database:** Base de datos NoSQL en tiempo real.
* **Cloud Messaging (FCM):** Envío de notificaciones push.
* **Cloud Functions (Gen 2):** Lógica de negocio en Node.js 20.

---

## 🚀 Configuración del Proyecto

### Requisitos Previos
* Android Studio Ladybug o superior.
* Cuenta de Firebase (Plan Blaze para Cloud Functions, aunque incluye capa gratuita).
* Docker (Opcional, pero recomendado para desplegar el backend).

### 1. Configuración del Cliente (Android)
1.  Clona este repositorio.
2.  Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
3.  Agrega una app Android y descarga el archivo `google-services.json`.
4.  Coloca el archivo en `app/google-services.json`.
5.  Define la contraseña maestra de administrador en tu `gradle.properties` (o variables de entorno):
    ```properties
    ADMIN_MASTER_PASSWORD=TuContraseñaSecreta
    ```
6.  Sincroniza y ejecuta la app.

---

## ☁️ Despliegue del Backend (Cloud Functions)

> **Nota Importante:** Para evitar errores de compatibilidad de versiones de Node.js entre Windows y Firebase, y problemas de corrupción de archivos (`EBADENGINE`), este proyecto utiliza **Docker** para el despliegue.

### Estructura de Archivos Requerida
La carpeta `functions/` contiene el código fuente (`index.js`). Sin embargo, por seguridad, el archivo de credenciales **NO** está incluido en el repositorio.

1.  Ve a la consola de Firebase > Configuración del proyecto > Cuentas de servicio.
2.  Genera una **Nueva clave privada**.
3.  Renombra el archivo descargado a `service-account.json`.
4.  Colócalo dentro de la carpeta `functions/` de este proyecto.

### Pasos para Desplegar (Usando Docker)

Ejecuta el siguiente comando en la raíz del proyecto (carpeta `firebase-backend` o similar) usando PowerShell o Terminal:

```bash
# Inicia un contenedor efímero de Node 20
# La opción -v /app/functions/node_modules aísla las dependencias para evitar corrupción en Windows
docker run --rm -it -v ${PWD}:/app -v /app/functions/node_modules -w /app node:20-slim bash
