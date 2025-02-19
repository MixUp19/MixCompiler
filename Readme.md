# Proyecto de Compilador

Este proyecto es un compilador simple implementado en Java. Incluye un lexer y un parser para procesar un lenguaje de programación personalizado.

## Estructura del Proyecto

- `src/main/java/org/compiler/model/Lexer.java`: Contiene la implementación del lexer.
- `src/main/java/org/compiler/model/Parser.java`: Contiene la implementación del parser.
- `src/main/java/org/compiler/model/util/Pair.java`: Clase utilitaria para manejar pares de valores.
- `src/main/java/org/compiler/model/util/TiposDeTokens.java`: Clase enum que define los tipos de tokens.

## Requisitos

- Java 11 o superior
- Maven 3.6.0 o superior

## Construcción del Proyecto

Para construir el proyecto, ejecute el siguiente comando en el directorio raíz del proyecto:

```sh
mvn clean install