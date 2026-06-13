---
title: "概要"
linkTitle: "概要"
weight: 1
description: >
  サーバーサイドKotlinのためのORMライブラリ
---

## Komapperとは？ {#what-is-it}

KomapperはサーバーサイドKotlinのためのORMライブラリーです。

Komapperを利用するには以下の環境が必要です。

- Kotlin 2.3.21 以上
- JRE 17 以上
- Gradle 8.2 以上

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

KomapperはQuarkusとの連携もサポートしています。
例については [komapper-examples](https://github.com/komapper/komapper-examples)
リポジトリ直下のquarkus-jdbcプロジェクトを参照ください。

## 主要な概念 {#core-concepts}

ドキュメント全体を通して以下の用語が登場します。

- **Database**: クエリを実行するためのエントリーポイントです。
  `JdbcDatabase`もしくは`R2dbcDatabase`のインスタンスを生成し、クエリの実行やトランザクションの制御に利用します。
  詳細は [データベース]({{< relref "../Reference/database" >}}) と
  [トランザクション]({{< relref "../Reference/transaction" >}}) を参照ください。
- **エンティティクラス**: データベースのテーブルにマッピングされる、プレーンなKotlinのデータクラスです。
  Komapperのクラスを継承したり参照したりする必要はありません。
  詳細は [エンティティクラス]({{< relref "../Reference/entity-class" >}}) を参照ください。
- **マッピング定義**: エンティティクラスとテーブルを対応づけるアノテーションです。
  エンティティクラス自身に記述することも、別のクラスに分離して記述することもできます。
  詳細は [エンティティクラス]({{< relref "../Reference/entity-class" >}}) を参照ください。
- **メタモデル**: マッピング定義からコンパイル時に生成されるテーブルやカラムの情報です。
  `Meta`オブジェクトを介して公開され、タイプセーフなクエリの構築を可能にします。
  詳細は [アノテーションプロセッシング]({{< relref "../Reference/annotation-processing" >}}) を参照ください。
- **クエリ**: 実行するSQLを表現する不変オブジェクトです。
  クエリの構築は`QueryDsl`が、実行は`Database`インスタンスが担います。構築と実行は完全に分離されています。
  詳細は [クエリ]({{< relref "../Reference/Query" >}}) を参照ください。
- **ダイアレクト**: データ型のマッピングなど、データベースやドライバの差異を吸収するモジュールです。
  詳細は [ダイアレクト]({{< relref "../Reference/dialect" >}}) を参照ください。

## なぜKomapperを選ぶのか？ {#why-komapper}

Komapperには他のORMとは異なるいくつかの設計上の選択があります。

- **予測可能でステートレスなデータアクセス。**
  Komapperには永続化コンテキストがありません。遅延ローディングもダーティチェックもキャッシュもありません。
  SQLはクエリを明示的に実行したときにのみ発行されるため、いつどのようなSQLが実行されるのかが常に明確です。
  クエリDSLで表現できない場合は
  [SQLテンプレート]({{< relref "../Reference/Query/QueryDsl/template" >}})
  を使ってSQLを完全に制御できます。
- **コンパイル時に検証されるマッピング。**
  マッピング情報はKSPによりコンパイル時に生成・検証されます。
  クエリは生成されたメタモデルに対して記述するため、
  エンティティクラスに存在しないプロパティへの参照や互換性のない型の比較はコンパイルエラーになります。
  コアモジュールはリフレクションを使用しません。
- **ブロッキングとリアクティブで共通のプログラミングモデル。**
  JDBCとR2DBCのAPIはほぼ同一です。
  JDBCで開発を始めて後からR2DBCへ移行する、あるいは両方をサポートする、
  といったことがライブラリを学び直すことなく可能です。
- **Kotlinのために設計されたAPI。**
  KomapperはJavaのライブラリをKotlinに適合させたものではなく、Kotlinのために作られています。
  不変なデータクラス、Value Class、コルーチンを自然に扱え、
  可変プロパティや引数なしコンストラクタは必要ありません。

## 他のORMとの比較 {#comparison-with-other-orms}

このセクションでは、JVM上で人気のある2つのORMとKomapperのアプローチの違いをまとめます。

### KomapperとHibernate（JPA） {#komapper-vs-hibernate}

[Hibernate](https://hibernate.org/) は最も広く使われているJPA実装です。
JPAは永続化コンテキストを中心に設計されています。
データベースから読み込まれたエンティティは管理対象オブジェクトとなり、
変更は自動的に検出されて（ダーティチェック）フラッシュ時にデータベースへ同期されます。
また、関連はプロキシを通じて遅延ロードできます。
これは強力な仕組みですが、いつどのようなSQLが実行されるのか予測しづらくなることがあります。
さらに、JPAのエンティティには引数なしコンストラクタが必要で通常は可変クラスとして定義するため、
Kotlinプロジェクトでは`kotlin-jpa`や`all-open`といったコンパイラプラグインが必要になるのが一般的です。

Komapperは正反対のアプローチを取ります。
永続化コンテキストは存在せず、エンティティはプレーンな（通常は不変の）データクラスであり、
すべてのSQLはクエリの実行によって明示的に発行されます。
また、JPAの仕様がブロッキングなJDBCアクセスのみを対象としているのに対し、
KomapperはJDBCとR2DBCの両方に対してほぼ同一のAPIを提供します。

### KomapperとExposed {#komapper-vs-exposed}

[Exposed](https://github.com/JetBrains/Exposed) はJetBrainsが開発するKotlin向けのORMフレームワークで、
タイプセーフなSQL DSLとDAOの2つのAPIスタイルを提供します。
Komapperとの最大の違いはマッピングのメタデータを定義する方法です。
Exposedではテーブルを表すオブジェクトを手で記述します。
また、DAO APIではエンティティクラスがフレームワークの基底クラスを継承します。

Komapperではアノテーションを付与したエンティティクラスからKSPがコンパイル時にメタモデルを生成します。
そのため、マッピングは常にエンティティクラスと同期し、コンパイラによって検証されます。
エンティティクラスはKomapperから完全に独立したプレーンなデータクラスのままです
（マッピング定義を別クラスに分離することもできます）。
また、SQLを手で書きたい場合のためにSQLテンプレートを提供します。

### 比較のまとめ {#comparison-summary}

|                  | Komapper                  | Exposed                                                  | Hibernate（JPA）                                   |
|------------------|---------------------------|----------------------------------------------------------|----------------------------------------------------|
| マッピングのメタデータ | KSPがコンパイル時に生成      | テーブルオブジェクトを手で記述                              | アノテーションを実行時に処理                          |
| エンティティクラス    | プレーンなKotlinデータクラス | 任意のクラス（SQL DSL）または`Entity`を継承したクラス（DAO API） | 引数なしコンストラクタを持つ可変クラス                  |
| 状態管理           | ステートレス                | ステートレス（SQL DSL）またはステートフル（DAO API）           | ダーティチェックと遅延ローディングを備えた永続化コンテキスト |
| SQLの発行タイミング  | クエリを実行したときのみ      | DSLのステートメントを実行したとき                            | セッションが決定（通常はフラッシュ時）                  |

3つのライブラリはいずれも活発にメンテナンスされており、それぞれに強みがあります。
JPA標準とそのエコシステムを重視するならHibernate、
アノテーション処理なしでスキーマをKotlinコードとして定義したいならExposed、
コンパイル時に検証されるマッピング・ステートレスで予測可能なSQL実行・JDBCとR2DBCの統一APIを求めるならKomapperが適しています。

## サポートするデータベース {#supported-databases}

KomapperはJDBCとR2DBCの両方で以下のデータベースをサポートします。

- H2 Database Engine
- MariaDB
- MySQL
- Oracle Database
- PostgreSQL
- SQL Server

データベースやドライバの差異はダイアレクトモジュールが吸収します。
サポートするバージョンやデータ型のマッピングについては
[ダイアレクト]({{< relref "../Reference/dialect" >}}) を参照ください。

## 次に見るべきドキュメント {#where-should-i-go-next}

* [クイックスタート]({{< relref "../Quickstart" >}})
* [サンプルアプリケーション]({{< relref "../Examples" >}})
* [リファレンス]({{< relref "../Reference" >}})
