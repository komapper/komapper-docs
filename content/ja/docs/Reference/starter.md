---
title: "スターター"
weight: 100
description: >
---

## 概要 {#overview}

Starterモジュールを使うとKomapperを使ったプロジェクトを簡単に始められます。
Starterモジュールにはいくつかの種類があります。

## シンプルなスターター {#simple-starter}

### komapper-starter-jdbc

このスターターはKomapperをJDBCと組み合わせて動かすのに必要かつ便利なライブラリを含みます。
利用するにはGradleの依存関係の宣言で次のように記述します。

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-starter-jdbc:$komapperVersion")
}
```

### komapper-starter-r2dbc

このスターターはKomapperをR2DBCと組み合わせて動かすのに必要かつ便利なライブラリを含みます。
利用するにはGradleの依存関係の宣言で次のように記述します。

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-starter-r2dbc:$komapperVersion")
}
```

## Spring Boot連携のためのスターター {#spring-boot-starter}

### komapper-spring-boot-starter-jdbc

このスターターはKomapperをJDBCと組み合わせてSpring Boot上で動かすのに必要かつ便利なライブラリを含みます。
利用するにはGradleの依存関係の宣言で次のように記述します。

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-spring-boot-starter-jdbc:$komapperVersion")
}
```

このスターターを使う上で特別な設定は不要です。
Spring Bootの仕様に従ってJDBCの接続文字列をapplication.propertiesに記述すれば動きます。

```
spring.datasource.url=jdbc:h2:mem:example-spring-boot;DB_CLOSE_DELAY=-1
```

### komapper-spring-boot-starter-test-jdbc

このスターターには、Spring Boot と JDBC を組み合わせて Komapper をテストするためのツールが含まれています。
使用するには、Gradle の依存関係定義に以下を追加してください:

```kotlin
val komapperVersion: String by project
dependencies {
    testImplementation("org.komapper:komapper-spring-boot-starter-test-jdbc:$komapperVersion")
}
```

具体的には、`komapper-spring-boot-starter-test-jdbc` は Komapper JDBC アプリケーション向けの専用
[Spring Boot テストスライス](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing.spring-boot-applications.autoconfigured-tests)
`@KomapperJdbcTest` を提供します。
このテストスライスは Komapper とデータベース関連のインフラ Bean のみを初期化し、Spring Boot アプリケーションコンテキスト全体はロードしないため、データベースアクセス層のコードをテストするのに適しています。

```kotlin
@KomapperJdbcTest
class MyDaoTest(
    @Autowired val db: JdbcDatabase,
) {
    // ...
}
```

デフォルトでは、`@KomapperJdbcTest` テストは
[トランザクション管理](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html#testcontext-tx-enabling-transactions)
され、各テスト終了時にロールバックされます。
トランザクション管理を無効にしたい場合は、テスト単位またはクラス全体で次のように設定できます:

```kotlin
@KomapperJdbcTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MyNonTransactionalKomapperTests {
    // ...
}
```

### komapper-spring-boot-starter-r2dbc

このスターターはKomapperをR2DBCと組み合わせてSpring Boot上で動かすのに必要かつ便利なライブラリを含みます。
利用するにはGradleの依存関係の宣言で次のように記述します。

```kotlin
val komapperVersion: String by project
dependencies {
    implementation("org.komapper:komapper-spring-boot-starter-r2dbc:$komapperVersion")
}
```

このスターターを使う上で特別な設定は不要です。
Spring Bootの仕様に従ってR2DBCの接続文字列をapplication.propertiesに記述すれば動きます。

```
spring.r2dbc.url=r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1
```

### komapper-spring-boot-starter-test-r2dbc

このスターターには、Spring Boot と R2DBC を組み合わせて Komapper をテストするためのツールが含まれています。
使用するには、Gradle の依存関係定義に以下を追加してください:

```kotlin
val komapperVersion: String by project
dependencies {
    testImplementation("org.komapper:komapper-spring-boot-starter-test-r2dbc:$komapperVersion")
}
```

具体的には、`komapper-spring-boot-starter-test-r2dbc` は Komapper R2DBC アプリケーション向けの専用
[Spring Boot テストスライス](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing.spring-boot-applications.autoconfigured-tests)
`@KomapperR2dbcTest` を提供します。
このテストスライスは Komapper とデータベース関連のインフラ Bean のみを初期化し、Spring Boot アプリケーションコンテキスト全体はロードしないため、データベースアクセス層のコードをテストするのに適しています。

```kotlin
@KomapperR2dbcTest
class MyDaoTest(
    @Autowired val db: R2dbcDatabase,
) {
    // ...
}
```

デフォルトでは、`@KomapperR2dbcTest` テストはトランザクション管理されません。
