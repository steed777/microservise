# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/gradle-plugin/reference/html/#build-image)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Описание проекта
В файле application.yml можно изменять следующие данные:
* server.port - порт текущего rest сервиса
* уровень логирования различных пакетов
* application.destination-service-properties.base-url - url микросервиса В
* application.service-id - id сервиса погоды который будет вызываться
* application.weather-properties.url-map - карта серверов погоды, поддерживается только get запросы в карту можно добавлять новые сервисы по примеру существуещего
* application.weather-properties.url-map.{service-id}.base-url - url сервиса погоды, обязательно указать полный запрос по примеру
* application.weather-properties.url-map.{service-id}.temp-path - путь к данным температуры погоды в приходящем документе формата JSON