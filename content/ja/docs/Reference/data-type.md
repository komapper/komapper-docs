---
title: "データ型"
weight: 18
description: >
---

## 概要 {#overview}

KotlinとSQLのデータ型の対応関係について説明します。

## ダイアレクトのデータ型 {#dialect-data-types}

データ型に関するKotlinとSQLのデフォルトの対応関係は利用する [ダイアレクト]({{< relref "dialect.md" >}}) によって決まります。

## ユーザー定義のデータ型 {#user-defined-data-types}

独自に定義したKotlinのデータ型とSQLのデータ型をマッピングさせるには、
Service Provider Interfaceの仕様に則ったクラスの作成と登録が必要です。

例えば、次のような`example.Age`というKotlinのデータ型をデータベースのINTEGER型にマッピングするとします。

```kotlin
package example

data class Age(val value: Int)
```

### JDBCの場合 {#user-defined-data-types-for-jdbc}

マッピングを実行するクラスを`org.komapper.jdbc.spi.JdbcUserDefinedDataType`を実装して作成します。

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
        // 第二引数はjdbcTypeプロパティに対応する型でなければいけません
        ps.setInt(index, value.value)
    }

    override fun toString(value: Age): String {
        return value.value.toString()
    }
}
```

上述のクラスは次の名前のファイルに登録します。

- `META-INF/services/org.komapper.jdbc.spi.JdbcUserDefinedDataType`

ファイルには次のようにクラスの完全修飾名を記載します。

```
example.jdbc.AgeType
```

この例では1つのクラスのみを登録していますが、行を分けて記載すれば複数のクラスをまとめて登録できます。

### R2DBCの場合 {#user-defined-data-types-for-r2dbc}

マッピングを実行するクラスを`org.komapper.r2dbc.spi.R2dbcUserDefinedDataType`を実装して作成します。

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
        // 第二引数はr2dbcTypeプロパティと同じ型でなければいけません
        statement.bind(index, value.value)
    }

    override fun setValue(statement: Statement, name: String, value: Age) {
        // 第二引数はr2dbcTypeプロパティと同じ型でなければいけません
        statement.bind(name, value.value)
    }

    override fun toString(value: Age): String {
        return value.value.toString()
    }
}
```

上述のクラスは次の名前のファイルに登録します。

- `META-INF/services/org.komapper.r2dbc.spi.R2dbcUserDefinedDataType`

ファイルには次のようにクラスの完全修飾名を記載します。

```
example.r2dbc.AgeType
```

この例では1つのクラスのみを登録していますが、行を分けて記載すれば複数のクラスをまとめて登録できます。

## データ型の変換 {#data-type-conversion}

データ型を他の型に変換してアプリケーションで扱うには
Service Provider Interfaceの仕様に則ったクラスの作成と登録が必要です。

例えば、`Int`をアプリケーションでは次のような`example.PhoneNumber`として扱うものとします。

```kotlin
package example

data class PhoneNumber(val value: Int)
```

変換を実行するクラスを`org.komapper.core.spi.DataTypeConverter`を実装して作成します。

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

上述のクラスは次の名前のファイルに登録します。

- `META-INF/services/org.komapper.core.spi.DataTypeConverter`

ファイルには次のようにクラスの完全修飾名を記載します。

```
example.PhoneNumberTypeConverter
```

この例では1つのクラスのみを登録していますが、行を分けて記載すれば複数のクラスをまとめて登録できます。

## Value class {#value-classes}

value classを利用する場合、value classの内側の型がマッピングに利用されます。

## Alternate type {#alternate-types}

`@KomapperColumn`の`alternateType`プロパティにvalue classを指定することでマッピング対象のSQL型を変更できます。

例えば、`kotlin.String`を`VARCHAR`や`VARCHAR2`ではなく`CLOB`または`TEXT`にマッピングしたい場合、
`org.komapper.core.type.ClobString`を指定します。

```kotlin
@KomapperColumn(alternateType = ClobString::class)
val description: String
```

`alternateType`プロパティにはユーザーが独自に作成したvalue classを指定できます。
ただし、そのvalue classに対応する[ユーザー定義のデータ型]({{< relref "#user-defined-data-types" >}})の作成と登録が必要です。

なお、value classは次の要件を満たす必要があります。

- コンストラクターがpublicである
- パラメータープロパティがpublicでnon-nullableである
- パラメータープロパティの型が`@KomapperColumn`で注釈されたエンティティプロパティの型と同じである

## kotlinx-datetimeのサポート {#kotlinx-datetime-support}

Komapperは [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) の以下のデータ型をサポートします。

- kotlinx.datetime.Instant
- kotlinx.datetime.LocalDate
- kotlinx.datetime.LocalDateTime

これらの型を利用するには、Gradleの依存関係の宣言で次のように宣言してください。

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
}
```

また、`komapper-datetime-jdbc`もしくは`komapper-datetime-r2dbc`を追加してください。

```kotlin
val komapperVersion: String by project
dependencies {
    runtimeOnly("org.komapper:komapper-datetime-jdbc:$komapperVersion")
}
```

{{< alert title="Note" >}}
[スターター]({{< relref "starter.md" >}}) を利用する場合、
`komapper-datetime-jdbc`や`komapper-datetime-r2dbc`の宣言は不要です。
{{< /alert >}}
