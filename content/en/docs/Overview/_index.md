---
title: "Overview"
linkTitle: "Overview"
weight: 1
description: >
  Komapper is an ORM library for server-side Kotlin
---

## What is it? {#what-is-it}

Komapper is an ORM library for server-side Kotlin.

To use Komapper, the following prerequisites must be met:

- Kotlin 2.3.21 or later
- JRE 17 or later
- Gradle 8.2 or later

Komapper has several strengths as follows:

- Support for both JDBC and R2DBC
- Code generation at compile-time
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

### Support for both JDBC and R2DBC {#support-for-both-jdbc-and-r2dbc}

Komapper can access databases using either [JDBC](https://jcp.org/en/jsr/detail?id=221)
or [R2DBC](https://r2dbc.io/), and provides almost the same APIs for both.
Komapper absorbs most of the differences between JDBC and R2DBC
by leveraging Kotlin coroutines.

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

1. In the R2DBC version, the `main` function is a suspending function.
2. The method of creating Database instance is different.

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

For more information on annotation-based mapping, see
[Entity Classes]({{< relref "../Reference/entity-class" >}}).
For details on compile-time annotation processing, see
[Annotation Processing]({{< relref "../Reference/annotation-processing" >}}).

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

See [Query Composition]({{< relref "../Reference/Query/composition" >}}) for details.

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

Here is an example of using a value class in a query:

```kotlin
val e = Meta.employee
val query = QueryDsl.from(e).where { e.age greaterEq Age(40) }
val seniorEmployeeList = db.runQuery { query }
```

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

For the complete working code, see the spring-boot-jdbc and spring-boot-r2dbc projects under the
[komapper-examples](https://github.com/komapper/komapper-examples) repository.
See also [Starters]({{< relref "../Reference/starter" >}}).

Komapper also works with Quarkus.
See the quarkus-jdbc project in the
[komapper-examples](https://github.com/komapper/komapper-examples) repository for an example.

## Core concepts {#core-concepts}

The following terms appear throughout the documentation:

- **Database**: The entry point for executing queries.
  You create a `JdbcDatabase` or `R2dbcDatabase` instance and use it to run queries and control transactions.
  See [Databases]({{< relref "../Reference/database" >}}) and
  [Transactions]({{< relref "../Reference/transaction" >}}).
- **Entity class**: A plain Kotlin data class that maps to a database table.
  Entity classes do not need to inherit from or reference any Komapper types.
  See [Entity Classes]({{< relref "../Reference/entity-class" >}}).
- **Mapping definition**: Annotations that associate an entity class with a table.
  They can be written directly on the entity class or in a separate class,
  keeping your entity classes free of mapping concerns.
  See [Entity Classes]({{< relref "../Reference/entity-class" >}}).
- **Metamodel**: A representation of a table and its columns, generated from the mapping definition
  at compile time and exposed through the `Meta` object.
  Metamodels make queries type-safe.
  See [Annotation Processing]({{< relref "../Reference/annotation-processing" >}}).
- **Query**: An immutable object that describes the SQL to be executed.
  Queries are constructed with `QueryDsl` and executed by a `Database` instance —
  construction and execution are completely separated.
  See [Queries]({{< relref "../Reference/Query" >}}).
- **Dialect**: A module that absorbs the differences between databases and drivers,
  such as data type mappings.
  See [Dialects]({{< relref "../Reference/dialect" >}}).

## Why Komapper? {#why-komapper}

Komapper makes several design choices that distinguish it from other ORMs:

- **Predictable, stateless data access.**
  Komapper has no persistence context: no lazy loading, no dirty checking, and no caching layer.
  SQL is issued only when you explicitly run a query,
  so it is always clear which SQL runs and when.
  When the query DSL is not enough,
  [SQL templates]({{< relref "../Reference/Query/QueryDsl/template" >}})
  give you full control over the SQL.
- **Compile-time verified mapping.**
  Mapping information is generated and validated at compile time by KSP.
  Queries are written against the generated metamodels,
  so mistakes such as referencing a non-existent entity property
  or comparing incompatible types are caught as compile errors.
  The core modules use no reflection.
- **One programming model for blocking and reactive.**
  The JDBC and R2DBC APIs are nearly identical.
  You can start with JDBC and move to R2DBC — or support both —
  without relearning the library.
- **Designed for Kotlin.**
  Komapper is built for Kotlin rather than adapted from Java:
  immutable data classes, value classes, and coroutines are supported naturally,
  and no mutable properties or no-arg constructors are required.

## Comparison with other ORMs {#comparison-with-other-orms}

This section summarizes how Komapper's approach differs from
two other popular ORMs on the JVM.

### Komapper vs Hibernate (JPA) {#komapper-vs-hibernate}

[Hibernate](https://hibernate.org/) is the most widely used JPA implementation.
JPA is built around a persistence context:
entities loaded from the database are managed objects,
changes to them are detected automatically (dirty checking) and
synchronized with the database at flush time,
and associations can be loaded lazily through proxies.
This is powerful, but it can make it difficult to predict which SQL is executed and when.
It also constrains how you write entity classes:
JPA entities require no-arg constructors and are typically mutable,
so Kotlin projects usually need compiler plugins such as `kotlin-jpa` and `all-open`.

Komapper takes the opposite approach.
There is no persistence context —
entities are plain, usually immutable, data classes,
and every SQL statement is issued explicitly by running a query.
In addition, while the JPA specification covers only blocking JDBC access,
Komapper provides nearly identical APIs for both JDBC and R2DBC.

### Komapper vs Exposed {#komapper-vs-exposed}

[Exposed](https://github.com/JetBrains/Exposed) is an ORM framework for Kotlin
developed by JetBrains.
It offers two API styles: a typesafe SQL DSL and a DAO API.
The biggest difference from Komapper is how mapping metadata is defined.
In Exposed, you describe tables by writing table objects by hand,
and with the DAO API your entity classes extend framework base classes.

In Komapper, the metamodel is generated at compile time
from annotated entity classes by KSP,
so the mapping always stays in sync with your entity classes
and is verified by the compiler.
Entity classes remain plain data classes that are completely independent of Komapper —
mapping definitions can even be placed in separate classes.
Komapper also provides SQL templates for cases where you want to write SQL by hand.

### Summary {#comparison-summary}

|                    | Komapper                         | Exposed                                                      | Hibernate (JPA)                                          |
|--------------------|----------------------------------|--------------------------------------------------------------|----------------------------------------------------------|
| Mapping metadata   | Generated at compile time by KSP | Table objects written by hand                                | Annotations processed at runtime                         |
| Entity classes     | Plain Kotlin data classes        | Any classes (SQL DSL) or classes extending `Entity` (DAO API) | Mutable classes with no-arg constructors                 |
| State management   | Stateless                        | Stateless (SQL DSL) or stateful (DAO API)                    | Persistence context with dirty checking and lazy loading |
| When SQL is issued | Only when a query is run         | When a DSL statement is executed                             | Determined by the session, typically at flush time       |

All three libraries are actively maintained and each has its own strengths.
Choose Hibernate if you want the JPA standard and its ecosystem,
Exposed if you prefer defining your schema as Kotlin code without annotation processing,
and Komapper if you want compile-time verified mapping,
stateless and predictable SQL execution,
and a unified API for JDBC and R2DBC.

## Supported databases {#supported-databases}

Komapper supports the following databases through both JDBC and R2DBC:

- H2 Database Engine
- MariaDB
- MySQL
- Oracle Database
- PostgreSQL
- SQL Server

Differences between databases and drivers are absorbed by dialect modules.
See [Dialects]({{< relref "../Reference/dialect" >}}) for
supported versions and data type mappings.

## Where should I go next? {#where-should-i-go-next}

* [Quickstart]({{< relref "../Quickstart" >}}): Get started with Komapper
* [Examples]({{< relref "../Examples" >}}): Check out some example code!
* [Reference]({{< relref "../Reference" >}}): Learn about each feature in detail
