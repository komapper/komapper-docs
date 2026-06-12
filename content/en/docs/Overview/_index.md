---
title: "Overview"
linkTitle: "Overview"
weight: 1
description: >
  Komapper is an ORM library for server-side Kotlin
---

## What is it? {#what-is-it}

Komapper is an ORM library for server-side Kotlin.
Komapper supports Kotlin 2.3.21 or later and requires JRE 17 or later.

Komapper has several strengths as follows:

- Support for both JDBC and R2DBC
- Code generation at compile-time
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

### Support for both JDBC and R2DBC {#support-for-both-jdbc-and-r2dbc}

Komapper provides almost the same APIs for both JDBC and R2DBC.

JDBC sample code:

```kotlin
fun main() {
    // create a Database instance
    val db = JdbcDatabase("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = Meta.address

    // execute simple CRUD operations in a transaction
    db.withTransaction {
        // create a schema
        db.runQuery {
            QueryDsl.create(a)
        }

        // INSERT
        val newAddress = db.runQuery {
            QueryDsl.insert(a).single(Address(street = "street A"))
        }

        // SELECT
        val address1 = db.runQuery {
            QueryDsl.from(a).where { a.id eq newAddress.id }.first()
        }
    }
}
```

R2DBC sample code:

```kotlin
suspend fun main() {
    // create a Database instance
    val db = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = Meta.address

    // execute simple CRUD operations in a transaction
    db.withTransaction {
        // create a schema
        db.runQuery {
            QueryDsl.create(a)
        }

        // INSERT
        val newAddress = db.runQuery {
            QueryDsl.insert(a).single(Address(street = "street A"))
        }

        // SELECT
        val address1 = db.runQuery {
            QueryDsl.from(a).where { a.id eq newAddress.id }.first()
        }
    }
}
```

The visual differences between above sample code are the following two points:

2. In the R2DBC version, the `main` function is a suspending function.
4. The method of creating Database instance is different.

For the complete working code, see the console-jdbc and console-r2dbc projects under the
[komapper-examples](https://github.com/komapper/komapper-examples) repository.

### Code generation at compile-time {#code-generation-at-compile-time}

Komapper uses the [Kotlin Symbol Processing API](https://github.com/google/ksp) to generate
the metamodel (table and column information) as Kotlin source code at compile time.

With this mechanism, Komapper does not need to use reflection or read metadata from the database at runtime.
This improves runtime reliability and performance.

Code generation is processed by reading annotations.
For example, if you want to map the `Address` class to the `ADDRESS` table, you can write as follows:

```kotlin
data class Address(
    val id: Int,
    val street: String,
    val version: Int
)

@KomapperEntityDef(Address::class)
data class AddressDef(
    @KomapperId val id: Nothing,
    @KomapperVersion val version: Nothing,
)
```

The generated metamodel is exposed to the application via
the extended properties of the `org.komapper.core.dsl.Meta` object.
Applications can use the metamodel to construct queries in a type-safe manner:

```kotlin
// get a generated metamodel
val a = Meta.address

// define a query
val query = QueryDsl.from(a).where { a.street eq "STREET 101" }.orderBy(a.id)
```

### Immutable and composable queries {#immutable-and-composable-queries}

Komapper's queries are virtually immutable.
Therefore, they are safely composable without worrying about problems associated with state sharing.

```kotlin
// get a generated metamodel
val a = Meta.address

// define queries
val query1 = QueryDsl.from(a)
val query2 = query1.where { a.id eq 1 }
val query3 = query2.where { or { a.id eq 2 } }.orderBy(a.street)
val query4 = query1.zip(query2)
    
// issue "select * from address"
val list1 = db.runQuery { query1 }
// issue "select * from address where id = 1"
val list2 = db.runQuery { query2 }
// issue "select * from address where id = 1 or id = 2 order by street"
val list3 = db.runQuery { query3 }
// issue "select * from address" and "select * from address where id = 1"
val (list4, list5) = db.runQuery { query4 }
```

Not only can you create other queries based on existing queries using the where function, etc.,
but you can also combine multiple queries into a single query using the zip function, etc.

### Support for Kotlin value classes {#support-for-kotlin-value-classes}

You can use a value class as a property of your entity class as follows:

```kotlin
@JvmInline
value class Age(val value: Int)

data class Employee(val id: Int = 0, val name: String, val age: Age)

@KomapperEntityDef(Employee::class)
data class EmployeeDef(@KomapperId @KomapperAutoIncrement val id: Nothing)
```

No special settings are required to use value classes.

### Easy Spring Boot integration {#easy-spring-boot-integration}

We provide starter modules to make Spring Boot integration easy.

For example, if you want to access H2 database using JDBC in combination with Spring Boot, 
you only need to add the following configuration to the dependencies block in the Gradle build script:

```kotlin
val komapperVersion: String by project

dependencies {
    implementation("org.komapper:komapper-spring-boot-starter-jdbc:$komapperVersion")
    implementation("org.komapper:komapper-dialect-h2-jdbc:$komapperVersion")
}
```

Your application works with Spring Boot managed datasources and transactions.

## Where should I go next?

* [Quickstart]({{< relref "../Quickstart" >}}): Get started with Komapper
* [Examples]({{< relref "../Examples" >}}): Check out some example code!

