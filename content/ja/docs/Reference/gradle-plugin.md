---
title: "Gradleプラグイン"
weight: 200
description: >
---

## 概要 {#overview}

Komapperが提供するGradleプラグインは、
データベースのメタデータから次の2種類のKotlinソースコードを生成します。

- エンティティクラス
- マッピング定義

エンティティクラスやマッピング定義については [エンティティクラス]({{< relref "entity-class" >}}) を参照ください。

{{< alert title="Note" >}}
Gradleプラグインの利用は必須ではありません。
データベース上にテーブル定義がすでに存在する場合に利用を検討ください。
{{< /alert >}}

## 利用方法 {#how-to-use}

プラグインの最新版は 
[Gradleプラグインのポータルのサイト](https://plugins.gradle.org/plugin/org.komapper.gradle) 
で確認できます。

下記のコードはプラグインを利用したGradleビルドスクリプトの例です。

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
    // TestcontainersとPostgreSQLのJDBCドライバへの依存を定義する
    dependencies {
        classpath(platform("org.testcontainers:testcontainers-bom:1.17.1"))
        classpath("org.testcontainers:postgresql")
        classpath("org.postgresql:postgresql:42.3.4")
    }
}

// Komapperプラグインの利用を宣言する
plugins {
  id("org.komapper.gradle") version "5.3.0"
}

// Komapperプラグインに関する設定を行う
komapper {
    generators {
        // 利用するデータベースごとにregisterブロックに適当な名前をつけてブロック内に設定を記述する
        register("postgresql") {
            val initScript = file("src/main/resources/init_postgresql.sql")
            // JDBCパラメータの設定。Testcontainers上のPostgreSQLを利用する
            jdbc {
                driver.set("org.testcontainers.jdbc.ContainerDatabaseDriver")
                url.set("jdbc:tc:postgresql:13.3:///test?TC_INITSCRIPT=file:${initScript.absolutePath}")
                user.set("test")
                password.set("test")
            }
            packageName.set("org.komapper.example.postgresql")
            overwriteEntities.set(true)
            overwriteDefinitions.set(true)
        }
    }
}
```

上記の設定をした上で下記のコマンドを実行するとエンティティクラスやマッピング定義のコードを生成できます。

```sh
$ ./gradlew komapperGenerator
```

`register`に指定した名前を明示しても同じ結果が得られます。

```sh
$ ./gradlew komapperPostgresqlGenerator
```

[サンプルアプリケーション]({{< relref "../Examples#codegen" >}}) も参考にしてください。

## パラメータ一覧 {#parameter-list}

`register`ブロック内で設定可能なパラメータを示します。

### jdbc.driver

JDBCドライバのクラス名を表します。

<span class="-text-red">設定必須</span>です。

### jdbc.url

JDBCのURLを表します。

<span class="-text-red">設定必須</span>です。

### jdbc.user

JDBCのユーザーを表します。

### jdbc.password

JDBCのパスワードを表します。

### catalog

データベースのメタデータ取得に使われるカタログです。

この値はパラメータとして
[DatabaseMetaData#getTables](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/DatabaseMetaData.html#getTables(java.lang.String,java.lang.String,java.lang.String,java.lang.String%5B%5D))
メソッドに渡されます。

### schemaPattern

データベースのメタデータ取得に使われるスキーマのパターンです。

`SALES%`のようにLIKE述語と同様の記述ができます。

この値はパラメータとして
[DatabaseMetaData#getTables](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/DatabaseMetaData.html#getTables(java.lang.String,java.lang.String,java.lang.String,java.lang.String%5B%5D))
メソッドに渡されます。

### tableNamePattern

データベースのメタデータ取得に使われるテーブルのパターンです。

`JOB%`のようにLIKE述語と同様の記述ができます。

この値はパラメータとして
[DatabaseMetaData#getTables](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/DatabaseMetaData.html#getTables(java.lang.String,java.lang.String,java.lang.String,java.lang.String%5B%5D))
メソッドに渡されます。

### tableTypes

データベースのメタデータ取得に使われるテーブルタイプです。

デフォルトの値は`TABLE`のみを含んだリストです。
このパラメータは以下のような値を含むことができます。

- TABLE
- VIEW

この値はパラメータとして
[DatabaseMetaData#getTables](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/DatabaseMetaData.html#getTables(java.lang.String,java.lang.String,java.lang.String,java.lang.String%5B%5D))
メソッドに渡されます。

### destinationDir

生成されるKotlinソースコードの出力先です。

デフォルトの値は`src/main/kotlin`です。

{{< alert title="Note" >}}
エンティティクラスのソースコードは`entities.kt`、マッピング定義のソースコードは`entityDefinitions.kt`というファイル名で出力されます。

これらのファイル名は変更できません。
{{< /alert >}}

### useSelfMapping

[セルフマッピング]({{< relref "entity-class#self-mapping-definition" >}})を使うかどうかです。
`false`の場合、[分離マッピング]({{< relref "entity-class#separation-mapping-definition" >}})が使われます。

デフォルトの値は`false`です。

### packageName

生成されるエンティティクラスやマッピング定義クラスのパッケージ名です。

### prefix

生成されるエンティティクラスの単純名のプレフィックスです。

デフォルトの値は空文字です。

### suffix

生成されるエンティティクラス名のサフィックスです。

デフォルトの値は空文字です。

### singularize

生成されるエンティティクラス名を単数化するかどうかを表します。
`true`の場合、複数形のテーブル名から単数形のエンティティクラス名を生成します。

デフォルトの値は`false`です。

### useTableNameAsAlias

テーブル名をもとに[aliases]({{< relref "entity-class#metamodel-aliases" >}})プロパティを生成するかどうかを表します。

デフォルトの値は`false`です。

### overwriteEntities

生成されるエンティティクラスのソースコードを上書きするかどうかを表します。

デフォルトの値は`false`です。

### overwriteDefinitions

生成されるマッピング定義のソースコードを上書きするかどうかを表します。

デフォルトの値は`false`です。

### declareAsNullable

生成されるエンティティクラスの全プロパティをNULL許容型として宣言するかどうかを表します。

デフォルトの値は`false`です。
この値が`false`の場合、 NULL許容型として宣言するかどうかはプロパティごとにデータベースのメタデータから判定します。

### useCatalog

生成されるマッピング定義でカタログ名を明示するかどうかを表します。

デフォルトの値は`false`です。

### useSchema

生成されるマッピング定義でスキーマ名を明示するかどうかを表します。

デフォルトの値は`false`です。

### propertyTypeResolver

生成されるエンティティクラスのプロパティの型を決定するリゾルバです。

デフォルト値は`org.komapper.codegen.PropertyTypeResolver.of()`です。

### enquote

SQLの識別子を引用符で囲むことを行う関数です。

デフォルト値は`org.komapper.codegen.Enquote.of()`です。

### classNameResolver

生成されるエンティティクラスの名前を決定するリゾルバです。

デフォルト値は`org.komapper.codegen.ClassNameResolver.of(prefix, suffix, singularize)`です。

### propertyNameResolver

生成されるエンティティクラスのプロパティの名前を決定するリゾルバです。

デフォルト値は`org.komapper.codegen.PropertyNameResolver.of()`です。

### versionPropertyName

[`@KomapperVersion`]({{< relref "entity-class#komapperversion" >}}) アノテーションを付与するプロパティの名前を指定できます。

デフォルトの値は空文字です。

### createdAtPropertyName

[`@KomapperCreatedAt`]({{< relref "entity-class#komappercreatedat" >}}) アノテーションを付与するプロパティの名前を指定できます。

デフォルトの値は空文字です。

### updatedAtPropertyName

[`@KomapperUpdatedAt`]({{< relref "entity-class#komapperupdatedat" >}}) アノテーションを付与するプロパティの名前を指定できます。

デフォルトの値は空文字です。
