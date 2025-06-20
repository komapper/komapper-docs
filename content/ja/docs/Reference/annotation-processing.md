---
title: "アノテーションプロセッシング"
weight: 25
description: >
---

## 概要 {#overview}

コンパイル時、Komapperは [マッピング定義]({{< relref "entity-class/#mapping-definition" >}}) 内のアノテーションを処理し、
結果をメタモデルのソースコードとして生成します。
アノテーションの処理とコードの生成には [Kotlin Symbol Processing API](https://github.com/google/ksp) (KSP)を利用します。

KSPを実行するためには、Gradleビルドスクリプトを次のように記述します。

```kotlin
plugins {
  id("com.google.devtools.ksp") version "2.1.21-2.0.2"
  kotlin("jvm") version "2.1.21"
}

dependencies {
  val komapperVersion = "5.4.0"
  ksp("org.komapper:komapper-processor:$komapperVersion")
}
```

`komapper-processor`モジュールにはKSPのアノテーションプロセッサが含まれます。

上記設定後、Gradleのbuildタスクを実行すると`build/generated/ksp/main/kotlin`ディレクトリ以下にコードが生成されます。

## オプション {#options}

オプションによりアノテーションプロセッサの挙動を変更できます。
利用可能なオプションは以下の通りです。

- komapper.prefix
- komapper.suffix
- komapper.enumStrategy
- komapper.namingStrategy
- komapper.metaObject
- komapper.alwaysQuote
- komapper.enableEntityMetamodelListing
- komapper.enableEntityStoreContext
- komapper.enableEntityProjection

オプションを指定するにはGradleのビルドスクリプトで次のように記述します。

```kotlin
ksp {
  arg("komapper.prefix", "")
  arg("komapper.suffix", "Metamodel")
  arg("komapper.enumStrategy", "ordinal")
  arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
  arg("komapper.metaObject", "example.Metamodels")
  arg("komapper.alwaysQuote", "true")
  arg("komapper.enableEntityMetamodelListing", "true")
  arg("komapper.enableEntityStoreContext", "true")
  arg("komapper.enableEntityProjection", "true")  
}
```

### komapper.prefix

生成されるメタモデルクラスのプレフィックスです。
デフォルト値は`_`（アンダースコア）です。

### komapper.suffix

生成されるメタモデルクラスのサフィックスです。
デフォルト値は空文字です。

### komapper.enumStrategy

Enum型のプロパティをデータベースのカラムどうマッピングするかの戦略です。
値には`name`または`ordinal`のいずれかを選択できます。
デフォルト値は`name`です。
なお、`@KomapperEnum`による指定はこの戦略よりも優先されます。

`komapper.enumStrategy`オプションに指定可能な値の定義は次の通りです。

name
: Enumクラスの`name`プロパティを文字列型のカラムにマッピングする。

ordinal
: Enumクラスの`ordinal`プロパティを整数型のカラムにマッピングする。

type
: Enumクラスをenum型のカラムにマッピングする。
Enumクラスに対応する [ユーザー定義のデータ型]({{< relref "data-type#user-defined-data-types" >}}) が必要であることに注意してほしい。

### komapper.namingStrategy

Kotlinのエンティクラスとプロパティからデータベースのテーブルとカラムの名前をどう解決するのかの戦略です。
値には`implicit`、`lower_snake_case`、`UPPER_SNAKE_CASE`のいずれかを選択できます。
デフォルト値は`lower_snake_case`です。
解決されたデータベースのテーブルとカラムの名前は生成されるメタモデルのコードの中に含まれます。
なお、`@KomapperTable`や`@KomapperColumn`で名前が指定される場合この戦略で決定される名前よりも優先されます。

`komapper.namingStrategy`オプションに指定可能な値の定義は次の通りです。

implicit
: エンティティクラスやプロパティの名前をそのままテーブルやカラムの名前とする。

lower_snake_case
: エンティティクラスやプロパティの名前をキャメルケースからスネークケースに変換した上で全て小文字にしテーブルやカラムの名前とする。

UPPER_SNAKE_CASE
: エンティティクラスやプロパティの名前をキャメルケースからスネークケースに変換した上で全て大文字にしテーブルやカラムの名前とする。

### komapper.metaObject

メタモデルのインスタンスを拡張プロパティとして提供するobjectを指定します。
デフォルト値は`org.komapper.core.dsl.Meta`です。

### komapper.alwaysQuote

SQL文の中でテーブル名やカラム名をクォートするかどうかです。
デフォルト値は`false`です。

### komapper.enableEntityMetamodelListing

エンティティメタモデルの一覧を取得できるようにするかどうかです。
デフォルト値は`false`です。

`true`を設定すると、次のような方法で一覧を取得できます。

```kotlin
val metamodels: List<EntityMetamodel<*, *, *>> = Meta.all()
```

```kotlin
val metamodels: List<EntityMetamodel<*, *, *>> = EntityMetamodels.list(Meta)
```

### komapper.enableEntityStoreContext

`EntityStore`のコンテキストを有効にするかどうかです。
デフォルト値は`false`です。

`true`を設定すると、[context receiver](https://kotlinlang.org/docs/whatsnew1620.html#prototype-of-context-receivers-for-kotlin-jvm)
を利用する[Association API]({{< relref "association" >}})のコードを生成します。

[context receiverを使った走査のコード]({{< relref "association#example-navigation-code-using-context-receiver" >}}) も参照ください。

{{< alert title="Note" >}}
このオプションを有効にするには、 合わせて以下のコードをGradleのビルドスクリプトに追加してください。

```kotlin
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}
```
{{< /alert >}}

### komapper.enableEntityProjection

クエリの結果をエンティティへ射影することを有効にするかどうかです。
有効にすると全てのエンティティクラスについて`SelectQuery`と`TemplateSelectQueryBuilder`の拡張関数が生成されます。
デフォルト値は`false`です。

拡張関数の名前は`selectAsAddress`のように`selectAs + エンティティクラスの単純名`となります。

拡張関数の名前を変更したり、特定のエンティティクラスについてのみ射影を有効にしたい場合は`@KomapperProjection`を使ってください。