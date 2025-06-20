---
title: "エンティティクラス"
weight: 20
description: >
---

## 概要 {#overview}

Komapperでは、データベースのテーブルに対応するKotlinクラスをエンティティクラスと呼びます。

エンティティクラスをテーブルにマッピングさせるには別途アノテーションを用いたマッピング定義が必要です。

マッピング定義はコンパイル時に解析されその結果がメタモデルとなります。
メタモデルはクエリの構築や実行で利用されます。

## エンティティクラスの定義 {#entity-class-definition}

エンティティクラスは次の要件を満たさなければいけません。

- Data Classである
- 可視性がprivateでない
- 型パラメータを持っていない

例えば、次のようなテーブル定義があるとします。

```sql
create table if not exists ADDRESS (
  ADDRESS_ID integer not null auto_increment,
  STREET varchar(500) not null,
  VERSION integer not null,
  CREATED_AT timestamp,
  UPDATED_AT timestamp,
  constraint pk_ADDRESS primary key(ADDRESS_ID)
);
```

上記のテーブル定義に対応するエンティティクラス定義は次のようになります。

```kotlin
data class Address(
  val id: Int = 0,
  val street: String,
  val version: Int = 0,
  val createdAt: LocalDateTime? = null,
  val updatedAt: LocalDateTime? = null,
)
```

プロパティとカラムの間における型の対応関係については [データ型]({{< relref "data-type.md" >}}) を参照ください。

## マッピング定義 {#mapping-definition}

マッピング定義の作成方法は2種類あります。

- エンティティクラス自身がマッピング定義を持つ方法（セルフマッピング）
- エンティティクラスとは別にエンティティ定義クラスを作成する方法（分離マッピング）

同一のエンティティクラスに対して1つの方法のみ適用できます。

### セルフマッピング {#self-mapping-definition}

このときエンティティクラスは前のセクションで説明した要件に加えて次の条件を満たさなければいけません。

- `@KomapperEntity`で注釈される

例えば、前のセクションで示した`Address`クラスにこの方法を適用すると次のように変更できます。

```kotlin
@KomapperEntity
data class Address(
  @KomapperId
  @KomapperAutoIncrement
  @KomapperColumn(name = "ADDRESS_ID")
  val id: Int = 0,
  val street: String,
  @KomapperVersion
  val version: Int = 0,
  @KomapperCreatedAt
  val createdAt: LocalDateTime? = null,
  @KomapperUpdatedAt
  val updatedAt: LocalDateTime? = null,
)
```

### 分離マッピング {#separation-mapping-definition}

エンティティ定義クラスは次の要件を満たさなければいけません。

- Data Classである
- 可視性がprivateでない
- 型パラメータを持っていない
- `@KomapperEntityDef`で注釈され引数でエンティティクラスを受け取る
- エンティティクラスに定義されたプロパティと異なる名前のプロパティを持たない

例えば、前のセクションで示した`Address`クラスに対するエンティティ定義クラスは次のように記述できます。

```kotlin
@KomapperEntityDef(Address::class)
data class AddressDef(
  @KomapperId
  @KomapperAutoIncrement
  @KomapperColumn(name = "ADDRESS_ID")
  val id: Nothing,
  @KomapperVersion
  val version: Nothing,
  @KomapperCreatedAt
  val createdAt: Nothing,
  @KomapperUpdatedAt
  val updatedAt: Nothing,
)
```

エンティティ定義クラスは、参照するエンティティクラスに定義された同名のプロパティに対し様々な設定ができます。
定義されないプロパティに対してはデフォルトのマッピング定義が適用されます。
上記の例ではエンティティクラスに登場する`street`プロパティがエンティティ定義クラスには登場しませんが、
`street`プロパティにはテーブル上の`STREET`カラムにマッピングされます。

エンティティ定義クラスのプロパティの型に制約はありません。上記の例では`Nothing`を使っています。

## メタモデル {#metamodel}

マッピング定義からは`org.komapper.core.dsl.metamodel.EntityMetamodel`のインターフェースを実装する形でメタモデルが生成されます。

生成されたメタモデルは`org.komapper.core.dsl.Meta`オブジェクトの拡張プロパティとなります。
アプリケーションではこの拡張プロパティを使ってクエリを組み立てられます。

```kotlin
// get a generated metamodel
val a = Meta.address

// define a query
val query = QueryDsl.from(a).where { a.street eq "STREET 101" }.orderBy(a.id)
```

### aliases {#metamodel-aliases}

上述の例では拡張プロパティの名前は`address`ですが、これは`@KomapperEntity`や`@KomapperEntityDef`の`aliases`プロパティで変更できます。

```kotlin
@KomapperEntity(aliases = ["addr"])
data class Address(
  ...
)
```

`aliases`プロパティには複数の名前を指定できます。
その際、名前ごとに異なるインスタンスとして公開されます。
複数の異なるインスタンスが必要となる主なユースケースは自己結合やサブクエリです。

```kotlin
@KomapperEntity(aliases = ["employee", "manager"])
data class Employee(
  ...
)
```

例えば、マネージャーの一覧を取得するには上記のように複数の名前をつけた上で以下のようなクエリを作ります。

```kotlin
val e = Meta.employee
val m = Meta.manager
val query: Query<List<Employee>> = QueryDsl.from(m)
  .distinct()
  .innerJoin(e) {
    m.employeeId eq e.managerId
  }
```

なお、事前に名前を持ったメタモデルを定義しない場合でも、`clone`関数を使えば同じことが実現可能です。

```kotlin
val e = Meta.employee
val m = e.clone()
val query: Query<List<Employee>> = QueryDsl.from(m)
  .distinct()
  .innerJoin(e) {
    m.employeeId eq e.managerId
  }
```

### unit {#metamodel-unit}

上述の例では`address`の拡張プロパティを持つのは`org.komapper.core.dsl.Meta`ですが、
このオブジェクトは`@KomapperEntity`や`@KomapperEntityDef`の`unit`プロパティで変更できます。

```kotlin
object MyMeta

@KomapperEntity(unit = MyMeta::class)
data class Address(
  ...
)
```

上のように定義すると`unit`プロパティに指定したオブジェクトが`address`の拡張プロパティを持ちます。

```kotlin
// get a generated metamodel
val a = MyMeta.address

// define a query
val query = QueryDsl.from(a).where { a.street eq "STREET 101" }.orderBy(a.id)
```

### clone {#metamodel-clone}

`clone`関数を使って既存のメタモデルを基に別のメタモデルを生成できます。
主なユースケースは、データ構造が同じで名前だけが異なるテーブルにデータをコピーする場合です。

```kotlin
val a = Meta.address
val archive = a.clone(table = "ADDRESS_ARCHIVE")
val query = QueryDsl.insert(archive).select {
  QueryDsl.from(a).where { a.id between 1..5 }
}
```

cloneしたメタモデルを他のメタモデルと同様に公開したい場合は、
オブジェクトでインスタンスを保持した上で`Meta`オブジェクトの拡張プロパティを定義してください。

```kotlin
object MetamodelHolder {
  private val _addressArchive = Meta.address.clone(table = "ADDRESS_ARCHIVE")
  val Meta.addressArchive get() = _addressArchive
}
```

### define {#metamodel-define}

`define`関数を使ってメタモデルに対しデフォルトのWHERE句を定義できます。
あるメタモデルを使う際に必ず同じ検索条件を用いたいケースで便利です。

```kotlin
object MetamodelHolder {
  private val _bostonOnly = Meta.department.define { d ->
    where {
      d.location eq "BOSTON"
    }
  }
  val Meta.bostonOnly get() = _bostonOnly
}
```

上記の`bostonOnly`メタモデルを利用すると、
クエリで検索条件を指定しないにも関わらずWHERE句をもったSQLが生成されます。

```kotlin
val d = Meta.bostonOnly
val query = QueryDsl.from(d)
/*
select t0_.DEPARTMENT_ID, t0_.DEPARTMENT_NO, t0_.DEPARTMENT_NAME, t0_.LOCATION, t0_.VERSION from DEPARTMENT as t0_ where t0_.LOCATION = ?
*/
```

WHERE句を持つクエリを組み立てた場合は検索条件がAND演算子で連結されます。

```kotlin
val d = Meta.bostonOnly
val query = QueryDsl.from(d).where { d.departmentNo greaterEq 0 }
/*
select t0_.DEPARTMENT_ID, t0_.DEPARTMENT_NO, t0_.DEPARTMENT_NAME, t0_.LOCATION, t0_.VERSION from DEPARTMENT as t0_ where t0_.LOCATION = ? and t0_.DEPARTMENT_NO >= ?
*/
```

defineしたメタモデルを結合先としてクエリに含めた場合もこの機能は有効です。

```kotlin
val e = Meta.employee
val d = Meta.bostonOnly
val query = QueryDsl.from(e).innerJoin(d) {
  e.departmentId eq d.departmentId
}
/*
select t0_.EMPLOYEE_ID, t0_.EMPLOYEE_NO, t0_.EMPLOYEE_NAME, t0_.MANAGER_ID, t0_.HIREDATE, t0_.SALARY, t0_.DEPARTMENT_ID, t0_.ADDRESS_ID, t0_.VERSION from EMPLOYEE as t0_ inner join DEPARTMENT as t1_ on (t0_.DEPARTMENT_ID = t1_.DEPARTMENT_ID) where t1_.LOCATION = ?
*/
```

SELECT文だけでなくUPDATE文やDELETE文でも有効です。

```kotlin
val d = Meta.bostonOnly
val query = QueryDsl.delete(d).all()
/*
delete from DEPARTMENT as t0_ where t0_.LOCATION = ?
*/
```

デフォルトのWHERE句にパラメータを渡したい場合は、拡張関数として定義することもできます。
ただし、メタモデルが毎回異なるインスタンスとなることは注意してください。

```kotlin
object MetamodelHolder {
    fun Meta.locationSpecificDepartment(value: String) = Meta.department.define { d ->
        where {
            d.location eq value
        }
    }
}
```

上記の拡張関数を呼び出す例です。

```kotlin
val d = Meta.locationSpecificDepartment("NEW YORK")
val query = QueryDsl.from(d)
val list = db.runQuery { query }
```

## クラスに付与するアノテーション一覧 {#annotation-list-for-class}

ここで説明するアノテーションは全て`org.komapper.annotation`パッケージに属します。

### @KomapperEntity

エンティティクラスがマッピング定義を持つことを表します。
[aliases]({{< relref "#metamodel-aliases" >}})プロパティと[unit]({{< relref "#metamodel-unit" >}})プロパティを持ちます。

```kotlin
@KomapperEntity(aliases = ["addr"])
data class Address(
  ...
)
```

### @KomapperEntityDef

エンティティ定義クラスであることを表します。
`entity`プロパティや[aliases]({{< relref "#metamodel-aliases" >}})プロパティや[unit]({{< relref "#metamodel-unit" >}})プロパティを指定できます。

```kotlin
@KomapperEntityDef(entity = Address::class, aliases = ["addr"])
data class AddressDef(
  ...
)
```

### @KomapperTable

エンティティクラスとマッピングするテーブルの名前を明示的に指定します。

```kotlin
@KomapperEntityDef(Address::class)
@KomapperTable("ADDRESS", schema = "ACCOUNT", alwaysQuote = true)
data class AddressDef(
  ...
)
```

`catalog`プロパティや`schema`プロパティにはテーブルが属するカタログやスキーマの名前を指定できます。

`alwaysQuote`プロパティに`true`を設定すると生成されるSQLの識別子が引用符で囲まれます。

このアノテーションでテーブルの名前を指定しない場合、アノテーション処理の`komapper.namingStrategy`オプションに従って名前が解決されます。

[アノテーションプロセッシングのオプション]({{< relref "annotation-processing#options" >}})も参照ください。

### @KomapperProjection

クエリの結果をエンティティへ射影することを有効にします。

```kotlin
@KomapperEntity
@KomapperProjection
data class Address(
  ...
)
```

`function`プロパティには生成される拡張関数の名前を指定できます。
指定しない場合、`selectAsAddress`のように`selectAs + エンティティクラスの単純名`となります。
拡張関数は`SelectQuery`と`TemplateSelectQueryBuilder`に対して生成されます。

`SelectQuery`の拡張関数は次のように利用できます。

```kotlin
val e = Meta.employee

val query: Query<List<Address>> = QueryDsl.from(e)
    .where { e.employeeName startsWith "S" }
    .selectAsAddress(
        version = e.version,
        addressId = e.employeeId,
        street = concat(e.employeeName, " STREET"),
    )
```

`TemplateSelectQueryBuilder`の拡張関数は次のように利用できます。

```kotlin
val sql = "select address_id, street, version from address order by address_id"
val query: Query<List<Address>> = QueryDsl.fromTemplate(sql).selectAsAddress()
```

このアノテーションを利用せずに全エンティティクラスについて射影を有効にしたい場合、
アノテーション処理の`komapper.enableEntityProjection`オプションを利用できます。

[アノテーションプロセッシングのオプション]({{< relref "annotation-processing#options" >}})も参照ください。


## プロパティに付与するアノテーション一覧 {#annotation-list-for-property}

ここで説明するアノテーションは全て`org.komapper.annotation`パッケージに属します。

### @KomapperId

プライマリーキーであることを表します。
複合プライマリキーを表すために、 1つのエンティティクラス内に複数指定することもできます。

`virtual`プロパティに`true`が設定された場合、
[SCHEMAクエリ]({{< relref "query/QueryDsl/schema" >}})は注釈されたプロパティを主キーだと見做しません。

```kotlin
@KomapperId(virtual = true)
val id: Int
```

### @KomapperSequence

プライマリキーがデータベースのシーケンスで生成されることを表します。
必ず`@KomapperId`と一緒に付与する必要があります。

このアノテーションを付与するプロパティの型は次のいずれかでなければいけません。

- Int
- Long
- UInt
- 上述の型をプロパティとして持つValue Class

```kotlin
@KomapperId
@KomapperSequence(name = "ADDRESS_SEQ", startWith = 1, incrementBy = 100)
val id: Int
```

`name`プロパティにはシーケンスの名前を指定しなければいけません。カタログやスキーマの指定もできます。

`startWith`プロパティと`incrementBy`プロパティの値はシーケンス定義に合わせなければいけません。

`alwaysQuote`プロパティに`true`を設定すると生成されるSQLの識別子が引用符で囲まれます。

### @KomapperAutoIncrement

プライマリーキーがデータベースの自動インクリメント機能で生成されることを表します。
必ず`@KomapperId`と一緒に付与する必要があります。

このアノテーションを付与するプロパティの型は次のいずれかでなければいけません。

- Int
- Long
- UInt
- 上述の型をプロパティとして持つValue Class

### @KomapperVersion

楽観的排他制御に使われるバージョン番号であることを表します。

このアノテーションを付与すると、
[Updateクエリ]({{< relref "query/QueryDsl/update" >}})や
[Deleteクエリ]({{< relref "query/QueryDsl/delete" >}})で楽観的排他制御が行われます。

このアノテーションを付与するプロパティの型は次のいずれかでなければいけません。

- Int
- Long
- UInt
- 上述の型をプロパティとして持つValue Class

### @KomapperCreatedAt

生成時のタイムスタンプであることを表します。

このアノテーションを付与すると、
[Insertクエリ]({{< relref "query/QueryDsl/insert" >}})にてタイムスタンプがプロパティに設定されます。

このアノテーションを付与するプロパティの型は次のいずれかでなければいけません。

- java.time.Instant
- java.time.LocalDateTime
- java.time.OffsetDateTime
- kotlinx.datetime.Instant
- kotlinx.datetime.LocalDateTime
- 上述の型をプロパティとして持つValue Class

{{< alert title="Note" >}}
kotlinx-datetimeのデータ型を使うには、
[kotlinx-datetimeのサポート]({{< relref "data-type#kotlinx-datetime-support" >}}) を参照ください。
{{< /alert >}}

### @KomapperUpdatedAt

更新時のタイムスタンプであることを表します。

このアノテーションを付与すると、
[Insertクエリ]({{< relref "query/QueryDsl/insert" >}})と
[Updateクエリ]({{< relref "query/QueryDsl/update" >}})にてタイムスタンプがプロパティに設定されます。

このアノテーションを付与するプロパティの型は次のいずれかでなければいけません。

- java.time.Instant
- java.time.LocalDateTime
- java.time.OffsetDateTime
- kotlinx.datetime.Instant
- kotlinx.datetime.LocalDateTime
- 上述の型をプロパティとして持つValue Class

{{< alert title="Note" >}}
kotlinx-datetimeのデータ型を使うには、
[kotlinx-datetimeのサポート]({{< relref "data-type#kotlinx-datetime-support" >}}) を参照ください。
{{< /alert >}}

### @KomapperEnum

Enum型のプロパティに対し、プロパティとカラムのマッピング方法を明示的に指定します。

```kotlin
@KomapperEnum(EnumType.ORDINAL)
val color: Color // このcolorプロパティがColorというEnum型に対応すると想定してください
```

`@KomapperEnum`の`type`プロパティには次のいずれかを指定できます。

EnumType.NAME
: Enumクラスの`name`プロパティを文字列型のカラムにマッピングする。

EnumType.ORDINAL
: Enumクラスの`ordinal`プロパティを整数型のカラムにマッピングする。

EnumType.PROPERTY
: Enumクラスの任意のプロパティをカラムにマッピングする。
マッピング対象のプロパティ名は`@KomapperEnum`の`hint`プロパティに指定する必要がある。

EnumType.TYPE
: Enumクラスをenum型のカラムにマッピングする。
Enumクラスに対応する [ユーザー定義のデータ型]({{< relref "data-type#user-defined-data-types" >}}) が必要であることに注意してほしい。

```kotlin
enum class Color(val code: String) { RED("r"), GREEN("g"), BLUE("b") }

@KomapperEntity
data class Box(
    @KomapperId
    val id: Int,
    @KomapperEnum(EnumType.NAME)
    val top: Color,
    @KomapperEnum(EnumType.ORDINAL)
    val bottom: Color,
    @KomapperEnum(EnumType.PROPERTY, hint = "code")
    val side: Color
)
```

Enum型のプロパティに対して`@KomapperEnum`を指定しない場合、
アノテーション処理の`komapper.enumStrategy`オプションに従ってマッピング方法が解決されます。

[アノテーションプロセッシングのオプション]({{< relref "annotation-processing#options" >}})も参照ください。

### @KomapperColumn

プロパティとマッピングするカラムの名前を明示的に指定します。

```kotlin
@KomapperColumn(name = "ADDRESS_ID", alwaysQuote = true, masking = true)
val id: Nothing
```

`alwaysQuote`プロパティに`true`を設定すると生成されるSQLの識別子が引用符で囲まれます。

`masking`プロパティに`true`を設定すると、 ログの中で対応するデータがマスキングされます。

`updatable` プロパティが `false` に設定されている場合、そのカラムは、明示的に `set {}` ブロックが指定されていない限り、[`update()`]({{< relref "Query/QueryDsl/update" >}}) および [`insert().onDuplicateKeyUpdate()`]({{< relref "Query/QueryDsl/insert#onduplicatekeyupdate" >}}) DSL によって生成されるデフォルトの `UPDATE` 文から除外されます。

`insertable` プロパティが `false` に設定されている場合、そのカラムは [`insert()`]({{< relref "Query/QueryDsl/insert" >}}) DSL によって生成される `INSERT` 文から除外されます。
これは、データベースで生成されるデフォルト値を持つカラム、計算されたカラム、自動インクリメントフィールドなどに有用です。

`alternateType`プロパティを利用すると、マッピングするSQLの型を変更できます。
詳細は、[Alternate type]({{< relref "data-type#alternate-types" >}})を参照ください。

このアノテーションでカラムの名前を指定しない場合、アノテーション処理の`komapper.namingStrategy`オプションに従って名前が解決されます。

[アノテーションプロセッシングのオプション]({{< relref "annotation-processing#options" >}})も参照ください。

### @KomapperIgnore

マッピングの対象外であることを表します。

### @KomapperEmbeddedId

複合プライマリキーの[組込バリュー](https://www.martinfowler.com/eaaCatalog/embeddedValue.html)を表します。

```kotlin
data class EmoloyeeId(val id1: Int, val id2: String)

@KomapperEntity
data class Employee(@KomapperEmbeddedId val id: EmoloyeeId, val name: String)
```

`virtual`プロパティに`true`が設定された場合、
[SCHEMAクエリ]({{< relref "query/QueryDsl/schema" >}})は注釈されたプロパティを複合主キーだと見做しません。

```kotlin
data class EmoloyeeId(val id1: Int, val id2: String)

@KomapperEntity
data class Employee(@KomapperEmbeddedId(virtual = true) val id: EmoloyeeId, val name: String)
```

### @KomapperEmbedded

[組込バリュー](https://www.martinfowler.com/eaaCatalog/embeddedValue.html)を表します。

```kotlin
data class Money(val amount: BigDecimal, val currency: String)

@KomapperEntity
data class Employee(@KomapperId val id: Int, @KomapperEmbedded val salary: Money)
```

### @KomapperEnumOverride

[組込バリュー](https://www.martinfowler.com/eaaCatalog/embeddedValue.html)内のEnum型のプロパティに対し、
[`@KomapperEnum`]({{< relref "#komapperenum" >}})を適用します。

```kotlin
enum class Currency { JPY, USD }

data class Money(val amount: BigDecimal, val currency: Currency)

@KomapperEntity
data class Employee( 
  @KomapperId
  val id: Int,
  @KomapperEmbedded
  @KomapperEnumOverrde("currency", KomapperEnum(EnumType.ORDINAL))
  val salary: Money
)
```

### @KomapperColumnOverride

[組込バリュー](https://www.martinfowler.com/eaaCatalog/embeddedValue.html)内のプロパティに対し、
[`@KomapperColumn`]({{< relref "#komappercolumn" >}})を適用します。

```kotlin
data class Money(val amount: BigDecimal, val currency: String)

@KomapperEntity
data class Employee( 
  @KomapperId
  val id: Int,
  @KomapperEmbedded
  @KomapperColumnOverrde("amount", KomapperColumn("SALARY_AMOUNT"))
  @KomapperColumnOverrde("currency", KomapperColumn("SALARY_CURRENCY"))
  val salary: Money
)
```
