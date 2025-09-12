---
title: "Dialects"
weight: 15
description: >
---

## Overview {#overview}

Dialects absorb differences in databases and drivers.
A typical function of dialects is to resolve the type mappings between Kotlin and SQL.

The classes representing dialects are contained in separate modules (artifacts), 
and the choice of which module to use must be based on the destination database and connectivity type.

| Database           | Type  |            Artifact ID            |     Dialect Class      | Supported Database Version |
|--------------------|:-----:|:---------------------------------:|:----------------------:|----------------------------|
| H2 Database Engine | JDBC  |     komapper-dialect-h2-jdbc      |    H2JdbcSqlDialect    | v2.2.222 and above         |
| H2 Database Engine | R2DBC |     komapper-dialect-h2-r2dbc     |   H2R2dbcSqlDialect    | v2.2.222 and above         |
| MariaDB            | JDBC  |   komapper-dialect-mariadb-jdbc   |   MariaDbJdbcDialect   | v10.6.3 and above          |
| MariaDB            | R2DBC |  komapper-dialect-mariadb-r2dbc   |  MariaDbR2dbcDialect   | v10.6.3 and above          |
| MySQL              | JDBC  |    komapper-dialect-mysql-jdbc    |    MySqlJdbcDialect    | v5.7.x and v8.x            |
| MySQL              | R2DBC |   komapper-dialect-mysql-r2dbc    |   MySqlR2dbcDialect    | v5.7.x and v8.x            |
| Oracle Database    | JDBC  |   komapper-dialect-oracle-jdbc    |   OracleJdbcDialect    | v18c and above             |
| Oracle Database    | R2DBC |   komapper-dialect-oracle-r2dbc   |   OracleR2dbcDialect   | v18c and above             |
| PostgreSQL         | JDBC  | komapper-dialect-postgresql-jdbc  | PostgreSqlJdbcDialect  | v12.9 and above            |
| PostgreSQL         | R2DBC | komapper-dialect-postgresql-r2dbc | PostgreSqlR2dbcDialect | v12.9 and above            |
| SQL Server         | JDBC  |  komapper-dialect-sqlserver-jdbc  |  SqlServerJdbcDialect  | v2019 and above            |
| SQL Server         | R2DBC | komapper-dialect-sqlserver-r2dbc  | SqlServerR2dbcDialect  | v2019 and above            |

To use a dialect, specify the Artifact ID above in the Gradle dependencies declaration.

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-dialect-h2-jdbc:$komapperVersion")
}
```

## H2 - JDBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type                 | Description                                                                                                                                                               |
|--------------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.math.BigDecimal           | NUMERIC                  |                                                                                                                                                                           |
| java.math.BigInteger           | NUMERIC                  |                                                                                                                                                                           |
| java.sql.Array                 | ARRAY                    |                                                                                                                                                                           |
| java.sql.Blob                  | BLOB                     |                                                                                                                                                                           |
| java.sql.Clob                  | CLOB                     |                                                                                                                                                                           |
| java.sql.NClob                 | CLOB                     |                                                                                                                                                                           |
| java.sql.SQLXML                | CLOB                     |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.time.LocalDate            | DATE                     |                                                                                                                                                                           |
| java.time.LocalDateTime        | TIMESTAMP                |                                                                                                                                                                           |
| java.time.LocalTime            | TIME                     |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.util.UUID                 | UUID                     |                                                                                                                                                                           |
| kotlin.Any                     | JAVA_OBJECT              |                                                                                                                                                                           |
| kotlin.Boolean                 | BOOLEAN                  |                                                                                                                                                                           |
| kotlin.Byte                    | TINYINT                  |                                                                                                                                                                           |
| kotlin.ByteArray               | BINARY                   |                                                                                                                                                                           |
| kotlin.Double                  | DOUBLE                   |                                                                                                                                                                           |
| kotlin.Float                   | REAL                     |                                                                                                                                                                           |
| kotlin.Int                     | INT                      |                                                                                                                                                                           |
| kotlin.Long                    | BIGINT                   |                                                                                                                                                                           |
| kotlin.Short                   | SMALLINT                 |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR                  |                                                                                                                                                                           |
| kotlin.String                  | CLOB                     | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | SMALLINT                 |                                                                                                                                                                           |
| kotlin.UInt                    | BIGINT                   |                                                                                                                                                                           |
| kotlin.UShort                  | INT                      |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE                     |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | TIMESTAMP                |                                                                                                                                                                           |
| enum class                     | VARCHAR                  | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## H2 - R2DBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type                 | Description                                                                                                                                                               |
|--------------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.r2dbc.spi.Blob              | BLOB                     |                                                                                                                                                                           |
| io.r2dbc.spi.Clob              | CLOB                     |                                                                                                                                                                           |
| java.math.BigDecimal           | NUMERIC                  |                                                                                                                                                                           |
| java.math.BigInteger           | NUMERIC                  |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.time.LocalDate            | DATE                     |                                                                                                                                                                           |
| java.time.LocalDateTime        | TIMESTAMP                |                                                                                                                                                                           |
| java.time.LocalTime            | TIME                     |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.util.UUID                 | UUID                     |                                                                                                                                                                           |
| kotlin.Any                     | N/A                      |                                                                                                                                                                           |
| kotlin.Boolean                 | BOOLEAN                  |                                                                                                                                                                           |
| kotlin.Byte                    | TINYINT                  |                                                                                                                                                                           |
| kotlin.ByteArray               | BINARY                   |                                                                                                                                                                           |
| kotlin.Double                  | DOUBLE                   |                                                                                                                                                                           |
| kotlin.Float                   | REAL                     |                                                                                                                                                                           |
| kotlin.Int                     | INT                      |                                                                                                                                                                           |
| kotlin.Long                    | BIGINT                   |                                                                                                                                                                           |
| kotlin.Short                   | SMALLINT                 |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR                  |                                                                                                                                                                           |
| kotlin.String                  | CLOB                     | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | SMALLINT                 |                                                                                                                                                                           |
| kotlin.UInt                    | BIGINT                   |                                                                                                                                                                           |
| kotlin.UShort                  | INT                      |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE                     |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | TIMESTAMP                |                                                                                                                                                                           |
| enum class                     | VARCHAR                  | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## MariaDB - JDBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type        | Description                                                                                                                                                               |
|--------------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.math.BigDecimal           | DECIMAL         |                                                                                                                                                                           |
| java.math.BigInteger           | DECIMAL         |                                                                                                                                                                           |
| java.sql.Array                 | N/A             |                                                                                                                                                                           |
| java.sql.Blob                  | BLOB            |                                                                                                                                                                           |
| java.sql.Clob                  | TEXT            |                                                                                                                                                                           |
| java.sql.NClob                 | TEXT            |                                                                                                                                                                           |
| java.sql.SQLXML                | TEXT            |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP       |                                                                                                                                                                           |
| java.time.LocalDate            | DATE            |                                                                                                                                                                           |
| java.time.LocalDateTime        | DATETIME        |                                                                                                                                                                           |
| java.time.LocalTime            | TIME            |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP       |                                                                                                                                                                           |
| java.util.UUID                 | N/A             |                                                                                                                                                                           |
| kotlin.Any                     | N/A             |                                                                                                                                                                           |
| kotlin.Boolean                 | BIT(1), BOOLEAN |                                                                                                                                                                           |
| kotlin.Byte                    | TINYINT         |                                                                                                                                                                           |
| kotlin.ByteArray               | VARBINARY       |                                                                                                                                                                           |
| kotlin.Double                  | DOUBLE          |                                                                                                                                                                           |
| kotlin.Float                   | FLOAT           |                                                                                                                                                                           |
| kotlin.Int                     | INT             |                                                                                                                                                                           |
| kotlin.Long                    | BIGINT          |                                                                                                                                                                           |
| kotlin.Short                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR         |                                                                                                                                                                           |
| kotlin.String                  | TEXT            | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.UInt                    | BIGINT          |                                                                                                                                                                           |
| kotlin.UShort                  | INT             |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP       |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE            |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | DATETIME        |                                                                                                                                                                           |
| enum class                     | VARCHAR         | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## MariaDB - R2DBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type        | Description                                                                                                                                                               |
|--------------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.r2dbc.spi.Blob              | BLOB            |                                                                                                                                                                           |
| io.r2dbc.spi.Clob              | TEXT            |                                                                                                                                                                           |
| java.math.BigDecimal           | DECIMAL         |                                                                                                                                                                           |
| java.math.BigInteger           | DECIMAL         |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP       |                                                                                                                                                                           |
| java.time.LocalDate            | DATE            |                                                                                                                                                                           |
| java.time.LocalDateTime        | DATETIME        |                                                                                                                                                                           |
| java.time.LocalTime            | TIME            |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP       |                                                                                                                                                                           |
| java.util.UUID                 | N/A             |                                                                                                                                                                           |
| kotlin.Any                     | N/A             |                                                                                                                                                                           |
| kotlin.Boolean                 | BIT(1), BOOLEAN |                                                                                                                                                                           |
| kotlin.Byte                    | TINYINT         |                                                                                                                                                                           |
| kotlin.ByteArray               | VARBINARY       |                                                                                                                                                                           |
| kotlin.Double                  | DOUBLE          |                                                                                                                                                                           |
| kotlin.Float                   | FLOAT           |                                                                                                                                                                           |
| kotlin.Int                     | INT             |                                                                                                                                                                           |
| kotlin.Long                    | BIGINT          |                                                                                                                                                                           |
| kotlin.Short                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR         |                                                                                                                                                                           |
| kotlin.String                  | TEXT            | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.UInt                    | BIGINT          |                                                                                                                                                                           |
| kotlin.UShort                  | INT             |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP       |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE            |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | DATETIME        |                                                                                                                                                                           |
| enum class                     | VARCHAR         | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## MySQL - JDBC

The MySQL Dialect in our system supports both MySQL versions 5 and 8, with the default being version 8.
To instantiate a Dialect specifically for version 5, you should explicitly specify the version as follows:

```kotlin
val dialect = MySqlJdbcDialect(MySqlVersion.V5)
```

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type        | Description                                                                                                                                                               |
|--------------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.math.BigDecimal           | DECIMAL         |                                                                                                                                                                           |
| java.math.BigInteger           | DECIMAL         |                                                                                                                                                                           |
| java.sql.Array                 | N/A             |                                                                                                                                                                           |
| java.sql.Blob                  | BLOB            |                                                                                                                                                                           |
| java.sql.Clob                  | TEXT            |                                                                                                                                                                           |
| java.sql.NClob                 | TEXT            |                                                                                                                                                                           |
| java.sql.SQLXML                | TEXT            |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP       |                                                                                                                                                                           |
| java.time.LocalDate            | DATE            |                                                                                                                                                                           |
| java.time.LocalDateTime        | DATETIME        |                                                                                                                                                                           |
| java.time.LocalTime            | TIME            |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP       |                                                                                                                                                                           |
| java.util.UUID                 | N/A             |                                                                                                                                                                           |
| kotlin.Any                     | N/A             |                                                                                                                                                                           |
| kotlin.Boolean                 | BIT(1), BOOLEAN |                                                                                                                                                                           |
| kotlin.Byte                    | TINYINT         |                                                                                                                                                                           |
| kotlin.ByteArray               | VARBINARY       |                                                                                                                                                                           |
| kotlin.Double                  | DOUBLE          |                                                                                                                                                                           |
| kotlin.Float                   | FLOAT           |                                                                                                                                                                           |
| kotlin.Int                     | INT             |                                                                                                                                                                           |
| kotlin.Long                    | BIGINT          |                                                                                                                                                                           |
| kotlin.Short                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR         |                                                                                                                                                                           |
| kotlin.String                  | TEXT            | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.UInt                    | BIGINT          |                                                                                                                                                                           |
| kotlin.UShort                  | INT             |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP       |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE            |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | DATETIME        |                                                                                                                                                                           |
| enum class                     | VARCHAR         | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## MySQL - R2DBC

The MySQL Dialect in our system supports both MySQL versions 5 and 8, with the default being version 8.
To instantiate a Dialect specifically for version 5, you should explicitly specify the version as follows:

```kotlin
val dialect = MySqlR2dbcDialect(MySqlVersion.V5)
```

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type        | Description                                                                                                                                                               |
|--------------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.r2dbc.spi.Blob              | N/A             |                                                                                                                                                                           |
| io.r2dbc.spi.Clob              | N/A             |                                                                                                                                                                           |
| java.math.BigDecimal           | DECIMAL         |                                                                                                                                                                           |
| java.math.BigInteger           | DECIMAL         |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP       |                                                                                                                                                                           |
| java.time.LocalDate            | DATE            |                                                                                                                                                                           |
| java.time.LocalDateTime        | DATETIME        |                                                                                                                                                                           |
| java.time.LocalTime            | N/A             |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP       |                                                                                                                                                                           |
| java.util.UUID                 | N/A             |                                                                                                                                                                           |
| kotlin.Any                     | N/A             |                                                                                                                                                                           |
| kotlin.Boolean                 | BIT(1), BOOLEAN |                                                                                                                                                                           |
| kotlin.Byte                    | TINYINT         |                                                                                                                                                                           |
| kotlin.ByteArray               | VARBINARY       |                                                                                                                                                                           |
| kotlin.Double                  | DOUBLE          |                                                                                                                                                                           |
| kotlin.Float                   | FLOAT           |                                                                                                                                                                           |
| kotlin.Int                     | INT             |                                                                                                                                                                           |
| kotlin.Long                    | BIGINT          |                                                                                                                                                                           |
| kotlin.Short                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR         |                                                                                                                                                                           |
| kotlin.String                  | TEXT            | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | SMALLINT        |                                                                                                                                                                           |
| kotlin.UInt                    | BIGINT          |                                                                                                                                                                           |
| kotlin.UShort                  | INT             |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP       |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE            |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | DATETIME        |                                                                                                                                                                           |
| enum class                     | VARCHAR         | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## Oracle - JDBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type                 | Description                                                                                                                                                               |
|--------------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.math.BigDecimal           | NUMBER                   |                                                                                                                                                                           |
| java.math.BigInteger           | NUMBER                   |                                                                                                                                                                           |
| java.sql.Array                 | N/A                      |                                                                                                                                                                           |
| java.sql.Blob                  | BLOB                     |                                                                                                                                                                           |
| java.sql.Clob                  | CLOB                     |                                                                                                                                                                           |
| java.sql.NClob                 | N/A                      |                                                                                                                                                                           |
| java.sql.SQLXML                | N/A                      |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.time.LocalDate            | DATE                     |                                                                                                                                                                           |
| java.time.LocalDateTime        | TIMESTAMP                |                                                                                                                                                                           |
| java.time.LocalTime            | TIME                     |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.util.UUID                 | N/A                      |                                                                                                                                                                           |
| kotlin.Any                     | N/A                      |                                                                                                                                                                           |
| kotlin.Boolean                 | NUMBER                   |                                                                                                                                                                           |
| kotlin.Byte                    | NUMBER                   |                                                                                                                                                                           |
| kotlin.ByteArray               | RAW                      |                                                                                                                                                                           |
| kotlin.Double                  | FLOAT                    |                                                                                                                                                                           |
| kotlin.Float                   | FLOAT                    |                                                                                                                                                                           |
| kotlin.Int                     | NUMBER                   |                                                                                                                                                                           |
| kotlin.Long                    | NUMBER                   |                                                                                                                                                                           |
| kotlin.Short                   | NUMBER                   |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR2                 |                                                                                                                                                                           |
| kotlin.String                  | CLOB                     | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | NUMBER                   |                                                                                                                                                                           |
| kotlin.UInt                    | NUMBER                   |                                                                                                                                                                           |
| kotlin.UShort                  | NUMBER                   |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE                     |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | TIMESTAMP                |                                                                                                                                                                           |
| enum class                     | VARCHAR2                 | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## Oracle - R2DBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type                 | Description                                                                                                                                                               |
|--------------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.r2dbc.spi.Blob              | BLOB                     |                                                                                                                                                                           |
| io.r2dbc.spi.Clob              | CLOB                     |                                                                                                                                                                           |
| java.math.BigDecimal           | NUMBER                   |                                                                                                                                                                           |
| java.math.BigInteger           | NUMBER                   |                                                                                                                                                                           |
| java.time.Duration             | INTERVAL DAY TO SECOND   |                                                                                                                                                                           |
| java.time.Instant              | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.time.LocalDate            | DATE                     |                                                                                                                                                                           |
| java.time.LocalDateTime        | TIMESTAMP                |                                                                                                                                                                           |
| java.time.LocalTime            | TIME                     |                                                                                                                                                                           |
| java.time.Period               | INTERVAL YEAR TO MONTH   |                                                                                                                                                                           |
| java.time.OffsetDateTime       | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| java.util.UUID                 | N/A                      |                                                                                                                                                                           |
| kotlin.Any                     | N/A                      |                                                                                                                                                                           |
| kotlin.Boolean                 | NUMBER                   |                                                                                                                                                                           |
| kotlin.Byte                    | NUMBER                   |                                                                                                                                                                           |
| kotlin.ByteArray               | RAW                      |                                                                                                                                                                           |
| kotlin.Double                  | FLOAT                    |                                                                                                                                                                           |
| kotlin.Float                   | FLOAT                    |                                                                                                                                                                           |
| kotlin.Int                     | NUMBER                   |                                                                                                                                                                           |
| kotlin.Long                    | NUMBER                   |                                                                                                                                                                           |
| kotlin.Short                   | NUMBER                   |                                                                                                                                                                           |
| kotlin.String                  | VARCHAR2                 |                                                                                                                                                                           |
| kotlin.String                  | CLOB                     | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | NUMBER                   |                                                                                                                                                                           |
| kotlin.UInt                    | NUMBER                   |                                                                                                                                                                           |
| kotlin.UShort                  | NUMBER                   |                                                                                                                                                                           |
| kotlin.time.Instant            | TIMESTAMP WITH TIME ZONE |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | DATE                     |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | TIMESTAMP                |                                                                                                                                                                           |
| enum class                     | VARCHAR2                 | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## PostgreSQL - JDBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type                             | Description                                                                                                                                                               |
|--------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.math.BigDecimal           | numeric, decimal                     |                                                                                                                                                                           |
| java.math.BigInteger           | numeric, decimal                     |                                                                                                                                                                           |
| java.sql.Array                 | array of data type                   |                                                                                                                                                                           |
| java.sql.Blob                  | N/A                                  |                                                                                                                                                                           |
| java.sql.Clob                  | N/A                                  |                                                                                                                                                                           |
| java.sql.NClob                 | N/A                                  |                                                                                                                                                                           |
| java.sql.SQLXML                | xml                                  |                                                                                                                                                                           |
| java.time.Instant              | timestamp with time zone             |                                                                                                                                                                           |
| java.time.LocalDate            | date                                 |                                                                                                                                                                           |
| java.time.LocalDateTime        | timestamp                            |                                                                                                                                                                           |
| java.time.LocalTime            | time                                 |                                                                                                                                                                           |
| java.time.OffsetDateTime       | timestamp with time zone             |                                                                                                                                                                           |
| java.util.UUID                 | uuid                                 |                                                                                                                                                                           |
| kotlin.Any                     | N/A                                  |                                                                                                                                                                           |
| kotlin.Boolean                 | boolean, bool                        |                                                                                                                                                                           |
| kotlin.Byte                    | smallint                             |                                                                                                                                                                           |
| kotlin.ByteArray               | bytea                                |                                                                                                                                                                           |
| kotlin.Double                  | double precision, float8             |                                                                                                                                                                           |
| kotlin.Float                   | real                                 |                                                                                                                                                                           |
| kotlin.Int                     | integer, int, int4, serial           |                                                                                                                                                                           |
| kotlin.Long                    | bigint, int8, bigserial, serial8     |                                                                                                                                                                           |
| kotlin.Short                   | smallint, int2, smallserial, serial2 |                                                                                                                                                                           |
| kotlin.String                  | character varying, varchar, text     |                                                                                                                                                                           |
| kotlin.String                  | text                                 | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | smallint, int2, smallserial, serial2 |                                                                                                                                                                           |
| kotlin.UInt                    | bigint, int8, bigserial, serial8     |                                                                                                                                                                           |
| kotlin.UShort                  | integer, int, int4, serial           |                                                                                                                                                                           |
| kotlin.time.Instant            | timestamp with time zone             |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | date                                 |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | timestamp                            |                                                                                                                                                                           |
| enum class                     | character varying, varchar, text     | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## PostgreSQL - R2DBC

The following table shows the data type mapping:

| Kotlin Type                        | SQL Type                             | Description                                                                                                                                                               |
|------------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.r2dbc.postgresql.codec.Box      | box                                  |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Circle   | circle                               |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Line     | line                                 |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Lseg     | lseg                                 |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Path     | path                                 |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Point    | point                                |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Polygon  | polygon                              |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Interval | interval                             |                                                                                                                                                                           |
| io.r2dbc.postgresql.codec.Json     | json, jsonb                          |                                                                                                                                                                           |
| io.r2dbc.spi.Blob                  | bytea                                |                                                                                                                                                                           |
| io.r2dbc.spi.Clob                  | text                                 |                                                                                                                                                                           |
| java.math.BigDecimal               | numeric, decimal                     |                                                                                                                                                                           |
| java.math.BigInteger               | numeric, decimal                     |                                                                                                                                                                           |
| java.time.Instant                  | timestamp with time zone             |                                                                                                                                                                           |
| java.time.LocalDate                | date                                 |                                                                                                                                                                           |
| java.time.LocalDateTime            | timestamp                            |                                                                                                                                                                           |
| java.time.LocalTime                | time                                 |                                                                                                                                                                           |
| java.time.OffsetDateTime           | timestamp with time zone             |                                                                                                                                                                           |
| java.util.UUID                     | uuid                                 |                                                                                                                                                                           |
| kotlin.Any                         | N/A                                  |                                                                                                                                                                           |
| kotlin.Array                       | array of data type                   |                                                                                                                                                                           |
| kotlin.Boolean                     | boolean, bool                        |                                                                                                                                                                           |
| kotlin.Byte                        | smallint                             |                                                                                                                                                                           |
| kotlin.ByteArray                   | bytea                                |                                                                                                                                                                           |
| kotlin.Double                      | double precision, float8             |                                                                                                                                                                           |
| kotlin.Float                       | real                                 |                                                                                                                                                                           |
| kotlin.Int                         | integer, int, int4, serial           |                                                                                                                                                                           |
| kotlin.Long                        | bigint, int8, bigserial, serial8     |                                                                                                                                                                           |
| kotlin.Short                       | smallint, int2, smallserial, serial2 |                                                                                                                                                                           |
| kotlin.String                      | character varying, varchar, text     |                                                                                                                                                                           |
| kotlin.String                      | text                                 | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                       | smallint, int2, smallserial, serial2 |                                                                                                                                                                           |
| kotlin.UInt                        | bigint, int8, bigserial, serial8     |                                                                                                                                                                           |
| kotlin.UShort                      | integer, int, int4, serial           |                                                                                                                                                                           |
| kotlin.time.Instant                | timestamp with time zone             |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate         | date                                 |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime     | timestamp                            |                                                                                                                                                                           |
| org.locationtech.jts.geom.Geometry | geometry                             |                                                                                                                                                                           |
| enum class                         | character varying, varchar, text     | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## SQL Server - JDBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type          | Description                                                                                                                                                               |
|--------------------------------|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| java.math.BigDecimal           | decimal           |                                                                                                                                                                           |
| java.math.BigInteger           | decimal           |                                                                                                                                                                           |
| java.sql.Array                 | N/A               |                                                                                                                                                                           |
| java.sql.Blob                  | varbinary(max)    |                                                                                                                                                                           |
| java.sql.Clob                  | text              |                                                                                                                                                                           |
| java.sql.NClob                 | N/A               |                                                                                                                                                                           |
| java.sql.SQLXML                | xml               |                                                                                                                                                                           |
| java.time.Instant              | datetimeoffset    |                                                                                                                                                                           |
| java.time.LocalDate            | date              |                                                                                                                                                                           |
| java.time.LocalDateTime        | datetime2         |                                                                                                                                                                           |
| java.time.LocalTime            | time              |                                                                                                                                                                           |
| java.time.OffsetDateTime       | datetimeoffset    |                                                                                                                                                                           |
| java.util.UUID                 | N/A               |                                                                                                                                                                           |
| kotlin.Any                     | N/A               |                                                                                                                                                                           |
| kotlin.Boolean                 | bit               |                                                                                                                                                                           |
| kotlin.Byte                    | smallint, tinyint |                                                                                                                                                                           |
| kotlin.ByteArray               | varbinary         |                                                                                                                                                                           |
| kotlin.Double                  | float             |                                                                                                                                                                           |
| kotlin.Float                   | real              |                                                                                                                                                                           |
| kotlin.Int                     | int               |                                                                                                                                                                           |
| kotlin.Long                    | bigint            |                                                                                                                                                                           |
| kotlin.Short                   | smallint          |                                                                                                                                                                           |
| kotlin.String                  | varchar, nvarchar |                                                                                                                                                                           |
| kotlin.String                  | text              | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | smallint          |                                                                                                                                                                           |
| kotlin.UInt                    | bigint            |                                                                                                                                                                           |
| kotlin.UShort                  | int               |                                                                                                                                                                           |
| kotlin.time.Instant            | datetimeoffset    |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | date              |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | datetime2         |                                                                                                                                                                           |
| enum class                     | varchar, nvarchar | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |

## SQL Server - R2DBC

The following table shows the data type mapping:

| Kotlin Type                    | SQL Type          | Description                                                                                                                                                               |
|--------------------------------|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.r2dbc.spi.Blob              | varbinary(max)    |                                                                                                                                                                           |
| io.r2dbc.spi.Clob              | text              |                                                                                                                                                                           |
| java.math.BigDecimal           | decimal           |                                                                                                                                                                           |
| java.math.BigInteger           | decimal           |                                                                                                                                                                           |
| java.time.Instant              | datetimeoffset    |                                                                                                                                                                           |
| java.time.LocalDate            | date              |                                                                                                                                                                           |
| java.time.LocalDateTime        | datetime2         |                                                                                                                                                                           |
| java.time.LocalTime            | time              |                                                                                                                                                                           |
| java.time.OffsetDateTime       | N/A               |                                                                                                                                                                           |
| java.util.UUID                 | N/A               |                                                                                                                                                                           |
| kotlin.Any                     | N/A               |                                                                                                                                                                           |
| kotlin.Boolean                 | bit               |                                                                                                                                                                           |
| kotlin.Byte                    | smallint, tinyint |                                                                                                                                                                           |
| kotlin.ByteArray               | varbinary         |                                                                                                                                                                           |
| kotlin.Double                  | float             |                                                                                                                                                                           |
| kotlin.Float                   | real              |                                                                                                                                                                           |
| kotlin.Int                     | int               |                                                                                                                                                                           |
| kotlin.Long                    | bigint            |                                                                                                                                                                           |
| kotlin.Short                   | smallint          |                                                                                                                                                                           |
| kotlin.String                  | varchar, nvarchar |                                                                                                                                                                           |
| kotlin.String                  | text              | `ClobString` must be specified as [alternate type]({{< relref "data-type#alternate-types" >}}).                                                                           |
| kotlin.UByte                   | smallint          |                                                                                                                                                                           |
| kotlin.UInt                    | bigint            |                                                                                                                                                                           |
| kotlin.UShort                  | int               |                                                                                                                                                                           |
| kotlin.time.Instant            | datetimeoffset    |                                                                                                                                                                           |
| kotlinx.datetime.LocalDate     | date              |                                                                                                                                                                           |
| kotlinx.datetime.LocalDateTime | datetimev         |                                                                                                                                                                           |
| enum class                     | varchar, nvarchar | Can be customized by [@KomapperEnum]({{< relref "entity-class#komapperenum" >}}) or [komapper.enumStrategy]({{< relref "annotation-processing#komapperenumstrategy" >}}). |
