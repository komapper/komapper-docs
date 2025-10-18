---
title: "Data Types"
weight: 18
description: >
---

## Overview {#overview}

This section describes data type mapping between Kotlin and SQL.

## Dialect data types {#dialect-data-types}

A [dialect]({{< relref "dialect.md" >}}) determines the default data type mapping between Kotlin and SQL.

## User-defined data types {#user-defined-data-types}

To map a user-defined Kotlin data type to a SQL data type, 
you must create and register a class that conforms to the Service Provider Interface specification.

For example, suppose you want to map the following Kotlin data type `example.Age` 
to an SQL INTEGER type.

```kotlin
package example

data class Age(val value: Int)
```

### In the case of JDBC {#user-defined-data-types-for-jdbc}

Create a class that implements `org.komapper.jdbc.spi.JdbcUserDefinedDataType` to perform the mapping:

```kotlin
package example.jdbc

import example.Age
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.SQLType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class AgeType : JdbcUserDefinedDataType<Age> {
    override val name: String = "integer"

    override val type: KType = typeOf<Age>()

    override val sqlType: SQLType = JDBCType.INTEGER

    override fun getValue(rs: ResultSet, index: Int): Age {
        return Age(rs.getInt(index))
    }

    override fun getValue(rs: ResultSet, columnLabel: String): Age {
        return Age(rs.getInt(columnLabel))
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Age) {
        // The second argument must correspond to the type of the jdbcType property.
        ps.setInt(index, value.value)
    }

    override fun toString(value: Age): String {
        return value.value.toString()
    }
}
```

Register the above class in a file with the following name:

- `META-INF/services/org.komapper.jdbc.spi.JdbcUserDefinedDataType`

This file contains the fully qualified name of the class as follows:

```
example.jdbc.AgeType
```

You can register multiple classes together by separating lines.

### In the case of R2DBC {#user-defined-data-types-for-r2dbc}

Create a class that implements `org.komapper.r2dbc.spi.R2dbcUserDefinedDataType` to perform the mapping:

```kotlin
package example.r2dbc

import example.Age
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import kotlin.reflect.KClass
import kotlin.reflect.typeOf
import java.sql.JDBCType
import java.sql.SQLType

class AgeType : R2dbcUserDefinedDataType<Age> {

    override val name: String = "integer"

    override val type: KType = typeOf<Age>()

    override val r2dbcType: Class<Int> = Int::class.javaObjectType

    override val sqlType: SQLType = JDBCType.INTEGER

    override fun getValue(row: Row, index: Int): Age? {
        return row.get(index, Int::class.javaObjectType)?.let { Age(it) }
    }

    override fun getValue(row: Row, columnLabel: String): Age? {
        return row.get(columnLabel, Int::class.javaObjectType)?.let { Age(it) }
    }

    override fun setValue(statement: Statement, index: Int, value: Age) {
        // The second argument must be of the same type as the r2dbcType property
        statement.bind(index, value.value)
    }

    override fun setValue(statement: Statement, name: String, value: Age) {
        // The second argument must be of the same type as the r2dbcType property
        statement.bind(name, value.value)
    }

    override fun toString(value: Age): String {
        return value.value.toString()
    }
}
```

Register the above class in a file with the following name:

- `META-INF/services/org.komapper.r2dbc.spi.R2dbcUserDefinedDataType`

This file contains the fully qualified name of the class as follows:

```
example.r2dbc.AgeType
```

You can register multiple classes together by separating lines.

## Data type conversion {#data-type-conversion}

To convert a data type to another type,
you must create and register a class that conforms to the Service Provider Interface specification.

For example, suppose you want to treat `Int` as the following `example.PhoneNumber` in your application.

```kotlin
package example

data class PhoneNumber(val value: Int)
```

Create a class that implements `org.komapper.core.spi.DataTypeConverter` to perform the conversion:

```kotlin
package example

import org.komapper.core.spi.DataTypeConverter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PhoneNumberTypeConverter : DataTypeConverter<PhoneNumber, Int> {
    override val exteriorType: KType = typeOf<PhoneNumber>()
    override val interiorType: KType = typeOf<Int>()

    override fun unwrap(exterior: PhoneNumber): Int {
        return exterior.value
    }

    override fun wrap(interior: Int): PhoneNumber {
        return PhoneNumber(interior)
    }
}
```

Register the above class in a file with the following name:

- `META-INF/services/org.komapper.core.spi.DataTypeConverter`

This file contains the fully qualified name of the class as follows:

```
example.PhoneNumberTypeConverter
```

You can register multiple classes together by separating lines.

## Value classes {#value-classes}

When you use a value class, the inner type of the value class is used for mapping.

## Alternate types {#alternate-types}

You can change the SQL type to be mapped by specifying a value class for the `@KomapperColumn.alternateType` property.

For example, if you want to map `kotlin.String` to `CLOB` or `TEXT` instead of `VARCHAR` or `VARCHAR2`,
specify `org.komapper.core.type.ClobString`.

```kotlin
@KomapperColumn(alternateType = ClobString::class)
val description: String
```

The `alternateType` property allows users to specify their own value class.
However, the [user-defined data type]({{< relref "#user-defined-data-types" >}}) corresponding to that value class
must be created and registered.

Note that the value class must meet the following requirements:

- the constructor must be public
- the parameter property must be public and non-nullable
- the parameter property type must match the entity property type annotated with `@KomapperColumn`

## Support for kotlinx-datetime {#kotlinx-datetime-support}

Komapper supports following data types of [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime):

- kotlinx.datetime.Instant
- kotlinx.datetime.LocalDate
- kotlinx.datetime.LocalDateTime

To use these types, declare `kotlinx-datetime` in the Gradle dependencies declaration as follows:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
}
```

Also, declare `komapper-datetime-jdbc` or `komapper-datetime-r2dbc`:

```kotlin
val komapperVersion: String by project
dependencies {
    runtimeOnly("org.komapper:komapper-datetime-jdbc:$komapperVersion")
}
```

{{< alert title="Note" >}}
If you use one of the [Starters]({{< relref "starter.md" >}}) ,
you do not need to declare `komapper-datetime-jdbc` and `komapper-datetime-r2dbc`.
{{< /alert >}}