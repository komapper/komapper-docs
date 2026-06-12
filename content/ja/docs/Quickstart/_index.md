---
title: "クイックスタート"
linkTitle: "クイックスタート"
weight: 2
description: >
  最小構成でKomapperを動かす
---

## 概要 {#overview}

H2 Database EngineにJDBCで接続するアプリケーションを作成します。

このチュートリアルでは以下のことを行います。

- Gradleプロジェクトをゼロからセットアップする
- エンティティクラスを定義し、コンパイル時にメタモデルを生成する
- トランザクション内でスキーマの作成と基本的なCRUD操作（挿入・検索・更新・削除）を行うプログラムを書く

アプリケーションはインメモリのH2データベースにアクセスするため、
データベースサーバーのインストールや起動は不要です。

## 必要要件 {#prerequisites}

- JDK 17、もしくはそれ以降のバージョン
- Gradle 7.6.4、もしくはそれ以降のバージョン

## インストール {#install}

JDKとGradleをインストールしてください。

{{< alert title="Note" >}}
[sdkman](https://sdkman.io/) を使ってインストールすることをお勧めします。
{{< /alert >}}

## アプリケーションの作成 {#create-application}

### プロジェクトレイアウト {#project-layout}

アプリケーションは以下のファイルで構成されます。

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

ディレクトリを作成したら、settings.gradle.ktsにプロジェクト名を定義します。

```kotlin
rootProject.name = "komapper-quickstart"
```

残りのファイルについては以降のセクションで説明します。

### ビルドスクリプト {#build-script}

ビルドスクリプトをGradle Kotlin DSLを使って書きます。

以下のコードをbuild.gradle.ktsに記述してください。

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

このビルドスクリプトのポイントは以下の2点です。

1. `plugins`ブロックで`com.google.devtools.ksp`プラグインを指定する
2. `dependencies`ブロックで同じバージョン番号を持つKomapperのモジュールを読み込む

`com.google.devtools.ksp`は [Kotlin Symbol Processing API](https://github.com/google/ksp) のプラグインです。
コンパイル時のコード生成に必要です。
プラグインのバージョン番号内のハイフンより前の値は、使用するKotlinのバージョンと等しいかより大きな値でなければいけません。

`dependencies`ブロックで指定するKomapperのモジュールのそれぞれの概要は以下の通りです。

- komapper-platform: Komapperのモジュールに関して推奨バージョンを提供します。
- komapper-starter-jdbc: Komapperを使ったJDBC接続に必要かつ便利なモジュール一式をまとめたモジュールです。
- komapper-dialect-h2-jdbc: H2 Database Engineに接続するために必要なモジュールです。
- komapper-processor: コンパイル時にコード生成を行うモジュールです。`ksp`というキーワードを使って宣言されていることに注意してください。
`ksp`はKotlin Symbol Processing APIのプラグインが提供する機能です。

### ソースコード {#source-code}

最初に、データベースのテーブルに対応するエンティティクラスをEmployee.ktに作ります。

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

このクラスは、アノテーションによって`EMPLOYEE`テーブルにマッピングされるプレーンなデータクラスです。

- `@KomapperEntity`: エンティティであることを表します。このアノテーションが付与されたクラスからメタモデルが生成されます。
- `@KomapperId`: プライマリキーであることを表します。
- `@KomapperAutoIncrement`: プライマリキーがデータベースの自動インクリメント機能によって生成されることを表します。
- `@KomapperVersion`: 楽観的排他制御に使われるバージョン番号であることを表します。
- `@KomapperCreatedAt`: エンティティの挿入時にタイムスタンプが自動で設定されます。
- `@KomapperUpdatedAt`: エンティティの挿入時と更新時にタイムスタンプが自動で設定されます。

利用可能なすべてのアノテーションについては
[エンティティクラス]({{< relref "../Reference/entity-class" >}}) を参照ください。

上記のクラスの作成が終わったら一度 [ビルド]({{< relref "./#build" >}}) してください。
メタモデルクラスのソースコードが出力され、後続のコードで利用できるようになります。

次に、main関数をApplication.ktに書きます。

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

1. 接続文字列を与えてデータベースを表すインスタンスを生成します。このインスタンスはトランザクション制御やクエリの実行に必要となります。
2. トランザクションを開始します。開始時にトランザクション属性や分離レベルを指定することもできます。
3. ソースコード生成したメタモデルクラスのインスタンスを取得します。メタモデルのインスタンスは`Meta`オブジェクトの拡張プロパティとして公開されます。
4. メタモデルを使ってスキーマを生成します。この機能は単純なサンプル作成に便利ですが、プロダクションレベルのアプリケーションでの利用は非推奨です。
5. 複数のエンティティを一度に追加します。
6. 全件をエンティティとして取得します。
7. 取得したエンティティをループで出力します。
8. 検索条件にマッチした最初の行をエンティティとして取得します。
`first`関数は拡張関数のため、`org.komapper.core.dsl.query`パッケージからのインポートが必要です。
9. エンティティを更新します。エンティティは不変なデータクラスなので、`copy`関数で名前を変更したコピーを作り更新クエリに渡します。
クエリはバージョン番号がインクリメントされた更新後のエンティティを返します。
10. エンティティを削除します。
11. 残っている従業員の数をカウントする集約クエリを発行します。
`count`関数は`org.komapper.core.dsl.operator`パッケージで定義されています。

上述のコードではクエリの構築と実行を同時に行っていますが、下記のように分けて行うこともできます。

```kotlin
// build a query
val query = QueryDsl.from(e).orderBy(e.id)
// run the query
val employees = database.runQuery(query)
```

### ビルド {#build}

ビルドをするには次のGradleコマンドを実行します。

```sh
$ gradle build
```

コマンド実行後、`build/generated/ksp/main/kotlin`ディレクトリを確認してください。
Kotlin Symbol Processing APIによって生成されたコードが存在することがわかります。

### 実行 {#run}

アプリケーションを動かすには次のGradleコマンドを実行します。

```sh
$ gradle run
```

アプリケーションを実行するとコンソール上に次のような出力が表示されます。

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

この出力にはいくつかの注目すべきポイントがあります。

- EmployeeインスタンスにIDやタイムスタンプが設定されています。
  これらはKomapperにより自動的に設定されました。
- 実行されたSQLがDEBUGレベルでログ出力されています。
  komapper-starter-jdbcモジュールにはSLF4JのアダプタとLogbackが含まれているため、
  特別なロギングの設定は不要です。
  詳細は [ロギング]({{< relref "../Reference/logging" >}}) を参照ください。
- UPDATE文に注目してください。
  `@KomapperVersion`アノテーションを付与したことで、
  Komapperは`version`カラムをインクリメントするとともにWHERE句の条件に含めています（楽観的排他制御）。
  他のトランザクションによって行が変更されておりバージョンが一致しない場合は
  `org.komapper.core.OptimisticLockException`がスローされます。
  DELETE文でも同様にバージョンがチェックされます。
- 2人の従業員のうち1人を削除したため、カウントは1になっています。

## 完全なコードの取得 {#get-complete-code}

完全なコードを得るには以下のリポジトリを確認ください。

- https://github.com/komapper/komapper-quickstart

上記リンク先のリポジトリではGradle Wrapperを使っています。
したがって、Gradleをインストールしなくてもアプリケーションを動かすことができます。
本ページではビルドと実行で2つのGradleコマンドを示しましたが、Gradle Wrapperを使うにはそれぞれ次のコマンドを使ってください。

```shell
$ ./gradlew build
```

```shell
$ ./gradlew run
```

## 次に見るべきドキュメント {#where-should-i-go-next}

* [サンプルアプリケーション]({{< relref "../Examples" >}}): Spring Boot、Quarkus、Ktorなどのサンプル
* [クエリ]({{< relref "../Reference/Query" >}}): サポートするすべてのクエリの詳細
* [エンティティクラス]({{< relref "../Reference/entity-class" >}}): マッピング用アノテーションの詳細
