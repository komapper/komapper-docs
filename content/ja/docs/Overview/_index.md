---
title: "概要"
linkTitle: "概要"
weight: 1
description: >
  サーバーサイドKotlinのためのORMライブラリ
---

## Komapperとは？ {#what-is-it}

KomapperはサーバーサイドKotlinのためのORMライブラリーです。
Kotlinの 2.3.21 以上をサポートします。動作にはJREの 17 以上が必要です。

Komapperにはいくつかの強みがあります。

- JDBCとR2DBCのサポート
- コンパイル時のコード生成
- 不変で合成可能なクエリ
- Value Classのサポート
- Spring Bootのサポート

### JDBCとR2DBCのサポート {#support-for-both-jdbc-and-r2dbc}

Komapperは [JDBC](https://jcp.org/en/jsr/detail?id=221) もしくは
[R2DBC](https://r2dbc.io/) を用いてデータベースにアクセスできます。

KomapperはKotlinコルーチンの機能を活用することでJDBCとR2DBCの違いの大部分を吸収しています。

例えば、JDBCを用いるコードは次のように書けます。

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

一方でR2DBCを使うコードは次のように書けます。（上述のJDBC版との違いがわかるでしょうか？）

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

見た目上の違いは以下の2点のみです。

1. R2DBC版では`main`関数が`suspend`関数である
2. `db`インスタンスの生成方法が異なる

動作する完全なコードについては [komapper-examples](https://github.com/komapper/komapper-examples)
リポジトリ直下のconsole-jdbcとconsole-r2dbcのプロジェクトを参照ください。

### コンパイル時のコード生成 {#code-generation-at-compile-time}

Komapperは [Kotlin Symbol Processing API](https://github.com/google/ksp)
を使ってコンパイル時にデータベースアクセスに必要なメタモデル（テーブルやカラムの情報）をKotlinのソースコードとして生成します。

この仕組みによりKomapperは実行時にリフレクションを用いたりデータベースからメタデータを読み取ったりする必要がありません。
そのため実行時の信頼性とパフォーマンスが向上します。

コード生成はアノテーションの読み取りによって行われます。
例えば、`Address`クラスを`ADDRESS`テーブルにマッピングさせる場合次のように記述できます。

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

生成されたメタモデルは`org.komapper.core.dsl.Meta`オブジェクトの拡張プロパティを介してアプリケーションに公開されます。
アプリーケーションはメタモデルを使うことでタイプセーフにクエリを組み立てられます。

```kotlin
// get a generated metamodel
val a = Meta.address

// define a query
val query = QueryDsl.from(a).where { a.street eq "STREET 101" }.orderBy(a.id)
```

アノテーションを使ったマッピングに関する詳細は
[エンティティクラス]({{< relref "../Reference/entity-class" >}}) を、
コンパイル時のアノテーション処理に関する詳細は
[アノテーションプロセッシング]({{< relref "../Reference/annotation-processing" >}}) を参照ください。

### 不変で合成可能なクエリ {#immutable-and-composable-queries}

Komapperのクエリは実質的に不変（immutable）です。
従って、状態の共有に伴う不具合を心配することなく安全に合成可能（composable）です。

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

`where`関数などを使って既存のクエリを基に他のクエリを作るだけでなく、
`zip`関数などを使って複数のクエリを単一のクエリに合成することもできます。

詳細は [クエリの合成]({{< relref "../Reference/Query/composition" >}}) を参照ください。

### Value Classのサポート {#support-for-kotlin-value-classes}

Value Classをエンティティクラスのプロパティとして利用できます。 利用に当たって特別な設定は不要です。

```kotlin
@JvmInline
value class Age(val value: Int)

data class Employee(val id: Int = 0, val name: String, val age: Age)

@KomapperEntityDef(Employee::class)
data class EmployeeDef(@KomapperId @KomapperAutoIncrement val id: Nothing)
```

クエリでの利用例です。

```kotlin
val e = Meta.employee
val query = QueryDsl.from(e).where { e.age greaterEq Age(40) }
val seniorEmployeeList = db.runQuery { query }
```

### Spring Bootのサポート {#easy-spring-boot-integration}

KomapperはSpring Bootとの組み合わせを容易にするstarterを提供します。

例えば、JDBCを使ったデータアクセスをSpring Bootと組み合わせて行いたい場合、
Gradleのdependenciesブロックに次のような設定をするだけで必要なライブラリの依存性が解決されSpring Boot管理のデータソースやトランザクションと連動します。

```kotlin
val komapperVersion: String by project

dependencies {
    implementation("org.komapper:komapper-spring-boot-starter-jdbc:$komapperVersion")
    implementation("org.komapper:komapper-dialect-h2-jdbc:$komapperVersion")
}
```

動作する完全なコードについては [komapper-examples](https://github.com/komapper/komapper-examples)
リポジトリ直下のspring-boot-jdbcとspring-boot-r2dbcのプロジェクトを参照ください。

関連情報として [スターター]({{< relref "../Reference/Starter" >}}) も参照ください。

## 次に見るべきドキュメント {#where-should-i-go-next}

* [クイックスタート]({{< relref "../Quickstart" >}})
* [サンプルアプリケーション]({{< relref "../Examples" >}})

