---
title: "クイックスタート"
linkTitle: "クイックスタート"
weight: 2
description: >
  最小構成でKomapperを動かす
---

## 概要 {#overview}

H2 Database EngineにJDBCで接続するアプリケーションを作成します。

## 必要要件 {#prerequisites}

- JDK 11、もしくはそれ以降のバージョン
- Gradle 6.8.3、もしくはそれ以降のバージョン

## インストール {#install}

JDKとGradleをインストールしてください。

{{< alert title="Note" >}}
[sdkman](https://sdkman.io/) を使ってインストールすることをお勧めします。
{{< /alert >}}

## アプリケーションの作成 {#create-application}

### ビルドスクリプト {#build-script}

ビルドスクリプトをGradle Kotlin DSLを使って書きます。

以下のコードをbuild.gradle.ktsに記述してください。

```kotlin
plugins {
    application
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
    kotlin("jvm") version "2.1.21"
}

application {
    mainClass.set("org.komapper.quickstart.ApplicationKt")
}

dependencies {
    val komapperVersion = "5.4.0"
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

最初に、データベースのテーブルに対応するエンティティクラスを作ります。

```kotlin
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

上記のクラスの作成が終わったら一度 [ビルド]({{< relref "./#build" >}}) してください。
メタモデルクラスのソースコードが出力され、後続のコードで利用できるようになります。

次に、main関数を書きます。

```kotlin
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
21:00:53.099 [main] DEBUG org.komapper.SQL - create table if not exists employee (id integer not null auto_increment, name varchar(500) not null, version integer not null, created_at timestamp not null, updated_at timestamp not null, constraint pk_employee primary key(id));
21:00:53.117 [main] DEBUG org.komapper.SQL - insert into employee (name, version, created_at, updated_at) values (?, ?, ?, ?), (?, ?, ?, ?)
21:00:53.140 [main] DEBUG org.komapper.SQL - select t0_.id, t0_.name, t0_.version, t0_.created_at, t0_.updated_at from employee as t0_ order by t0_.id asc
RESULT 0: Employee(id=1, name=AAA, version=0, createdAt=2021-05-05T21:00:53.115127, updatedAt=2021-05-05T21:00:53.115127)
RESULT 1: Employee(id=2, name=BBB, version=0, createdAt=2021-05-05T21:00:53.115250, updatedAt=2021-05-05T21:00:53.115250)
```

EmployeeインスタンスにIDやタイムスタンプが設定されていることがわかります。
これらはKomapperにより自動的に設定されました。

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