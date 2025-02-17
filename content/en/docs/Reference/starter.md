---
title: "Starters"
weight: 100
description: >
---

## Overview {#overview}

The Starter module makes it easy to start a project using Komapper.
There are several types of starter modules.

## Simple starters {#simple-starter}

### komapper-starter-jdbc

This starter includes all the necessary and useful libraries to run Komapper with JDBC.
To use it, you must include the following in your Gradle dependency declaration:

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-starter-jdbc:$komapperVersion")
}
```

### komapper-starter-r2dbc

This starter includes all the necessary and useful libraries to run Komapper with R2DBC.
To use it, you must include the following in your Gradle dependency declaration:

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-starter-r2dbc:$komapperVersion")
}
```

## Spring Boot starters {#spring-boot-starter}

### komapper-spring-boot-starter-jdbc

This starter includes all the necessary and useful libraries to 
run Komapper on Spring Boot in combination with JDBC.
To use it, you must include the following in your Gradle dependency declaration:

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-spring-boot-starter-jdbc:$komapperVersion")
}
```

No special configuration is required to use this starter.
Just write the JDBC connection string in your application.properties 
according to the Spring Boot specification.

```
spring.datasource.url=jdbc:h2:mem:example-spring-boot;DB_CLOSE_DELAY=-1
```

### komapper-spring-boot-starter-test-jdbc

This starter includes tools to test Komapper on Spring Boot in combination with JDBC.
To use it, you must include the following in your Gradle dependency declaration:

```kotlin
val komapperVersion: String by project
dependencies {
    testImplementation("org.komapper:komapper-spring-boot-starter-test-jdbc:$komapperVersion")
}
```

Specifically, `komapper-spring-boot-starter-test-jdbc` provides a dedicated
[Spring Boot test slice](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing.spring-boot-applications.autoconfigured-tests)
`@KomapperJdbcTest` for Komapper JDBC applications.
It only initializes Komapper and database infrastructure beans and does not load
the entire Spring Boot application context, so it is suitable for testing database access layer code.

```kotlin
@KomapperJdbcTest
class MyDaoTest(
    @Autowired val db: JdbcDatabase,
) {
    // ...
}
```

By default, `@KomapperJdbcTest` tests are
[transactional](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html#testcontext-tx-enabling-transactions)
and roll back at the end of each test.
If that is not what you want, you can disable transaction management for a test
or for the whole class as follows:

```kotlin
@KomapperJdbcTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MyNonTransactionalKomapperTests {
	// ...
}
```

### komapper-spring-boot-starter-r2dbc

This starter includes all the necessary and useful libraries to
run Komapper on Spring Boot in combination with R2DBC.
To use it, you must include the following in your Gradle dependency declaration:

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-spring-boot-starter-r2dbc:$komapperVersion")
}
```

No special configuration is required to use this starter.
Just write the R2DBC connection string in your application.properties
according to the Spring Boot specification.

```
spring.r2dbc.url=r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1
```

### komapper-spring-boot-starter-test-r2dbc

This starter includes tools to test Komapper on Spring Boot in combination with R2DBC.
To use it, you must include the following in your Gradle dependency declaration:

```kotlin
val komapperVersion: String by project
dependencies {
    testImplementation("org.komapper:komapper-spring-boot-starter-test-r2dbc:$komapperVersion")
}
```

Specifically, `komapper-spring-boot-starter-test-r2dbc` provides a dedicated
[Spring Boot test slice](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing.spring-boot-applications.autoconfigured-tests)
`@KomapperR2dbcTest` for Komapper R2DBC applications. It only initializes Komapper and
database infrastructure beans and does not load the entire Spring Boot application context,
so it is suitable for testing database access layer code.

```kotlin
@KomapperR2dbcTest
class MyDaoTest(
    @Autowired val db: R2dbcDatabase,
) {
    // ...
}
```

By default, `@KomapperR2dbcTest` tests are not transactional.
