---
title: "Quickstart"
linkTitle: "Quickstart"
weight: 2
description: >
  Try Komapper with minimum settings
---

## Overview {#overview}

We show you how to create an application that uses JDBC to access H2 Database Engine.

In this tutorial, you will:

- Set up a Gradle project from scratch
- Define an entity class and generate a metamodel at compile time
- Write a program that creates a schema and performs basic CRUD operations —
  insert, select, update, and delete — in a transaction

The application accesses an in-memory H2 database,
so you do not need to install or start a database server.

## Prerequisites {#prerequisites}

- JDK 17 or later
- Gradle 7.6.4 or later

## Install {#install}

Install JDK and Gradle.

{{< alert title="Note" >}}
We recommend that you install JDK using [sdkman](https://sdkman.io/).
{{< /alert >}}

## Create Application {#create-application}

### Project layout {#project-layout}

The application consists of the following files:

```
komapper-quickstart/
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        └── kotlin/
            └── org/
                └── komapper/
                    └── quickstart/
                        ├── Application.kt
                        └── Employee.kt
```

Create the directories, then define the project name in settings.gradle.kts:

```kotlin
rootProject.name = "komapper-quickstart"
```

The remaining files are explained in the following sections.

### Build Script {#build-script}

Write your build scripts using Gradle Kotlin DSL.

Include the following code in your build.gradle.kts:

```kotlin
plugins {
    application
    id("com.google.devtools.ksp") version "2.3.9"
    kotlin("jvm") version "2.4.0"
}

application {
    mainClass.set("org.komapper.quickstart.ApplicationKt")
}

dependencies {
    val komapperVersion = "7.0.0"
    platform("org.komapper:komapper-platform:$komapperVersion").let {
        implementation(it)
        ksp(it)
    }
    implementation("org.komapper:komapper-starter-jdbc")
    implementation("org.komapper:komapper-dialect-h2-jdbc")
    ksp("org.komapper:komapper-processor")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

repositories {
    mavenCentral()
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
```

There are two key points in this build script:

1. Specify the `com.google.devtools.ksp` plugin in the `plugins` block
2. Use Komapper modules with the same version number in the `dependencies` block.

`com.google.devtools.ksp` is a plugin for [Kotlin Symbol Processing API](https://github.com/google/ksp).
It is required for code generation at compile time.
The value before the hyphen in the plugin version number 
must be equal to or greater than the version of Kotlin you are using.

The following is an overview of each of the Komapper modules specified in the `dependencies` block:

- komapper-platform: Provides recommended versions of Komapper modules.
- komapper-starter-jdbc: This module contains a set of modules that are necessary and useful for JDBC access using Komapper.
- komapper-dialect-h2-jdbc: This module is necessary to connect to the H2 Database Engine.
- komapper-processor: A module to generate code at compile-time. Note that they are declared using the keyword `ksp`.
  The `ksp` keyword is provided by the Kotlin Symbol Processing API plugin.

### Source code {#source-code}

First, create an entity class in Employee.kt:

```kotlin
package org.komapper.quickstart

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import java.time.LocalDateTime

@KomapperEntity
data class Employee(
  @KomapperId @KomapperAutoIncrement
  val id: Int = 0,
  val name: String,
  @KomapperVersion
  val version: Int = 0,
  @KomapperCreatedAt
  val createdAt: LocalDateTime = LocalDateTime.MIN,
  @KomapperUpdatedAt
  val updatedAt: LocalDateTime = LocalDateTime.MIN,
)
```

This is a plain Kotlin data class that is mapped to the `EMPLOYEE` table by annotations:

- `@KomapperEntity`: Marks the class as an entity. The metamodel is generated from classes with this annotation.
- `@KomapperId`: Marks the property as the primary key.
- `@KomapperAutoIncrement`: Indicates that the primary key is generated using the database's auto-increment feature.
- `@KomapperVersion`: Marks the property as the version number used for optimistic locking.
- `@KomapperCreatedAt`: The timestamp is set automatically when the entity is inserted.
- `@KomapperUpdatedAt`: The timestamp is set automatically when the entity is inserted or updated.

For all available annotations, see [Entity Classes]({{< relref "../Reference/entity-class" >}}).

Once you have finished creating the above class, [build]({{< relref "./#build" >}}) it.
The metamodel code will be output and can be used in subsequent code.

Next, create a main logic in Application.kt:

```kotlin
package org.komapper.quickstart

import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase

fun main() {
  // (1) create a database instance
  val database = JdbcDatabase("jdbc:h2:mem:quickstart;DB_CLOSE_DELAY=-1")

  // (2) start transaction
  database.withTransaction {

    // (3) get an entity metamodel
    val e = Meta.employee

    // (4) create schema
    database.runQuery {
      QueryDsl.create(e)
    }

    // (5) insert multiple employees at once
    database.runQuery {
      QueryDsl.insert(e).multiple(Employee(name = "AAA"), Employee(name = "BBB"))
    }

    // (6) select all
    val employees = database.runQuery {
      QueryDsl.from(e).orderBy(e.id)
    }

    // (7) print all results
    for ((i, employee) in employees.withIndex()) {
      println("RESULT $i: $employee")
    }

    // (8) select one employee by name
    val employee = database.runQuery {
      QueryDsl.from(e).where { e.name eq "AAA" }.first()
    }

    // (9) update the employee
    val updated = database.runQuery {
      QueryDsl.update(e).single(employee.copy(name = "CCC"))
    }
    println("UPDATED: $updated")

    // (10) delete the employee
    database.runQuery {
      QueryDsl.delete(e).single(updated)
    }

    // (11) count the remaining employees
    val count = database.runQuery {
      QueryDsl.from(e).select(count())
    }
    println("COUNT: $count")
  }
}
```

1. Create an instance representing the database by providing a connection string. 
This instance is needed for transaction control and query execution.
2. Start the transaction. You can also specify transaction attributes and isolation levels at the start.
3. Get an instance of the metamodel class generated by the source code. 
Instances of the metamodel are exposed as extended properties of the `Meta` object. 
4. Generate a schema using the metamodel. 
This feature is useful for creating simple samples, but is deprecated for use in production-level applications.
5. Add multiple entities at once. 
6. Retrieve all records as entities. 
7. Output the retrieved entities.
8. Retrieve the first row that matches the search criteria as an entity.
Note that the `first` function is an extension function
that must be imported from the `org.komapper.core.dsl.query` package.
9. Update the entity. Because the entity is an immutable data class,
create a modified copy with the `copy` function and pass it to the update query.
The query returns the updated entity, whose version number is incremented.
10. Delete the entity.
11. Issue an aggregate query that counts the remaining employees.
The `count` function is defined in the `org.komapper.core.dsl.operator` package.

In the above code, query construction and execution are written together,
but they can also be separated:

```kotlin
// build a query
val query = QueryDsl.from(e).orderBy(e.id)
// run the query
val employees = database.runQuery(query)
```

### Build {#build}

To build your application, execute the following Gradle command:

```sh
$ gradle build
```

After executing the command, check the `build/generated/ksp/main/kotlin` directory.
You can see that the code generated by Kotlin Symbol Processing API exists.

### Run {#run}

To run your application, execute the following Gradle command:

```sh
$ gradle run
```

When you run the application, you will see the following output on the console:

```
07:02:18.613 [main] DEBUG org.komapper.Sql -- create table if not exists employee (id integer generated always as identity not null, name varchar(500) not null, version integer not null, created_at timestamp not null, updated_at timestamp not null, constraint pk_employee primary key(id))
07:02:18.629 [main] DEBUG org.komapper.Sql -- insert into employee (name, version, created_at, updated_at) values (?, ?, ?, ?), (?, ?, ?, ?)
07:02:18.648 [main] DEBUG org.komapper.Sql -- select t0_.id, t0_.name, t0_.version, t0_.created_at, t0_.updated_at from employee as t0_ order by t0_.id asc
RESULT 0: Employee(id=1, name=AAA, version=0, createdAt=2026-06-13T07:02:18.624055, updatedAt=2026-06-13T07:02:18.624055)
RESULT 1: Employee(id=2, name=BBB, version=0, createdAt=2026-06-13T07:02:18.624119, updatedAt=2026-06-13T07:02:18.624119)
07:02:18.668 [main] DEBUG org.komapper.Sql -- select t0_.id, t0_.name, t0_.version, t0_.created_at, t0_.updated_at from employee as t0_ where t0_.name = ?
07:02:18.673 [main] DEBUG org.komapper.Sql -- update employee set name = ?, version = ? + 1, updated_at = ? where id = ? and version = ?
UPDATED: Employee(id=1, name=CCC, version=1, createdAt=2026-06-13T07:02:18.624055, updatedAt=2026-06-13T07:02:18.671793)
07:02:18.675 [main] DEBUG org.komapper.Sql -- delete from employee as t0_ where t0_.id = ? and t0_.version = ?
07:02:18.681 [main] DEBUG org.komapper.Sql -- select count(*) from employee as t0_
COUNT: 1
```

There are several points to note in this output:

- The `Employee` instances have IDs and timestamps set.
  These were set automatically by Komapper.
- The executed SQL statements are logged at DEBUG level.
  The komapper-starter-jdbc module includes an SLF4J adapter and Logback,
  so no special logging configuration is required.
  See [Logging]({{< relref "../Reference/logging" >}}) for details.
- Look at the UPDATE statement.
  Thanks to the `@KomapperVersion` annotation,
  Komapper increments the `version` column and includes it in the WHERE clause (optimistic locking).
  If the row had been changed by another transaction and the version no longer matched,
  an `org.komapper.core.OptimisticLockException` would be thrown.
  The version is checked in the DELETE statement in the same way.
- The count is 1 because one of the two employees was deleted.

## Get complete code {#get-complete-code}

To get complete code,
see https://github.com/komapper/komapper-quickstart

In the above repository, Gradle Wrapper is available.
So you can execute `./gradlew build` and `./gradlew run` instead of `gradle build` and `gradle run`.

## Where should I go next? {#where-should-i-go-next}

* [Examples]({{< relref "../Examples" >}}): More sample applications, including Spring Boot, Quarkus, and Ktor
* [Queries]({{< relref "../Reference/Query" >}}): All supported query types in detail
* [Entity Classes]({{< relref "../Reference/entity-class" >}}): All mapping annotations in detail
