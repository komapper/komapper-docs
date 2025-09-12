---
title: "SELECTクエリ"
linkTitle: "検索"
weight: 10
description: 検索のためのクエリ
---

## 概要 {#overview}

SELECTクエリは`QueryDsl`の`from`、`with`または`select`を呼び出して構築します。

次のクエリは`ADDRESS`テーブルを全件取得するSQLに対応します。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_
*/
```

## from

FROM句を指定する場合は`from`を呼び出します。
`from`にはエンティティメタモデルを指定します。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_
*/
```

`from`にはエンティティメタモデルと共にサブクエリを指定できます。
エンティティメタモデルはサブクエリの結果に対応していなければいけません。

```kotlin
val e = Meta.employee
val t = Meta.employeeRank

val subquery = QueryDsl.from(e).select(
    e.employeeId,
    e.employeeName,
    rank().over {
        partitionBy(e.departmentId)
        orderBy(e.salary.desc())
    }
)

val query = QueryDsl.from(t, subquery)
    .where {
        t.rank eq 1
    }
    .orderBy(t.employeeId)
/*
select t0_.employee_id, t0_.employee_name, t0_.rank from (select t1_.employee_id as employee_id, t1_.employee_name as employee_name, rank() over(partition by t1_.department_id order by t1_.salary desc) as rank from employee as t1_) as t0_ where t0_.rank = ? order by t0_.employee_id asc
*/
```

上述の`Meta.employeeRank`に対応するエンティティ定義は次の通りです。

```kotlin
@KomapperEntity
data class EmployeeRank(
    @KomapperId
    val employeeId: Int,
    val employeeName: String,
    val rank: Int,
)
```

## with

WITH句を指定する場合は`with`を呼び出します。
`with`関数には、エンティティメタモデルとサブクエリを指定します。
エンティティメタモデルはサブクエリの結果に対応していなければいけません。

```kotlin
val e = Meta.employee
val t = Meta.employeeRank

val subquery = QueryDsl.from(e).select(
    e.employeeId,
    e.employeeName,
    rank().over {
        partitionBy(e.departmentId)
        orderBy(e.salary.desc())
    }
)

val query = QueryDsl.with(t, subquery)
        .from(e)
        .innerJoin(t) { e.employeeId eq t.employeeId }
        .where { t.rank eq 1 }
        .orderBy(e.departmentId)
        .select(e.departmentId, e.employeeId, e.employeeName)
/*
with employee_rank (employee_id, employee_name, rank) as (select t0_.employee_id, t0_.employee_name, rank() over(partition by t0_.department_id order by t0_.salary desc) from employee as t0_) select t0_.department_id, t0_.employee_id, t0_.employee_name from employee as t0_ inner join employee_rank as t1_ on (t0_.employee_id = t1_.employee_id) where t1_.rank = ? order by t0_.department_id asc
*/
```

上述の`Meta.employeeRank`に対応するエンティティ定義は次の通りです。

```kotlin
@KomapperEntity
data class EmployeeRank(
    @KomapperId
    val employeeId: Int,
    val employeeName: String,
    val rank: Int,
)
```

## withRecursive

WITH RECURSIVE句を指定する場合は`withRecursive`を呼び出します。
`withRecursive`関数には、エンティティメタモデルとサブクエリを指定します。
エンティティメタモデルはサブクエリの結果に対応していなければいけません。

```kotlin
val t = Meta.t
val subquery =
    QueryDsl.select(literal(1)).unionAll(
    QueryDsl.from(t).where { t.n less 10 }.select(t.n + 1),
)
val query = QueryDsl.withRecursive(t, subquery).from(t).select(sum(t.n))
/*
with recursive t (n) as ((select 1) union all (select (t0_.n + ?) from t as t0_ where t0_.n < ?)) select sum(t0_.n) from t as t0_
*/
```

上述の`Meta.t`に対応するエンティティ定義は次の通りです。

```kotlin
@KomapperEntity
data class T(
    @KomapperId(virtual = true)
    val n: Int,
)
```

## where

WHERE句を指定する場合は`where`を呼び出します。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

## innerJoin

INNER JOINを行う場合は`innerJoin`を呼び出します。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).innerJoin(e) { a.addressId eq e.addressId }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ inner join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID)
*/
```

## leftJoin

LEFT OUTER JOINを行う場合は`leftJoin`を呼び出します。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).leftJoin(e) { a.addressId eq e.addressId }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ left outer join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID)
*/
```

## fullJoin

FULL OUTER JOINを行う場合は`fullJoin`を呼び出します。

```kotlin
val query: Query<List<Pair<String?, String?>>> =
    QueryDsl.from(a).fullJoin(e) { a.addressId eq e.addressId }.select(a.street, e.employeeName)
/*
select t0_.STREET, t1_.EMPLOYEE_NAME from ADDRESS as t0_ full outer join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID)
*/
```

{{< alert color="warning" title="Warning" >}}
利用している [ダイアレクト]({{< relref "../../dialect.md" >}}) がFULL OUTER JOINをサポートしない場合（H2、MariaDB、MySQL等）、
クエリの実行時に`UnsupportedOperationException`がスローされます。
FULL OUTER JOINはPostgreSQL、Oracle、SQL Serverでサポートされています。
{{< /alert >}}

## forUpdate

FOR UPDATE句を指定する場合は`forUpdate`を呼び出します。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }.forUpdate()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ? for update
*/
```

`forUpdate`に渡すラムダ式の中で、`nowait`、 `skipLocked`、 `wait`などの関数を呼び出しLockオプションを指定できます。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }.forUpdate { nowait() }
/*
select t0_.address_id, t0_.street, t0_.version from address as t0_ where t0_.address_id = ? for update nowait
*/
```

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }.forUpdate { skipLocked() }
/*
select t0_.address_id, t0_.street, t0_.version from address as t0_ where t0_.address_id = ? for update skip locked
*/
```

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }.forUpdate { wait(1) }
/*
select t0_.address_id, t0_.street, t0_.version from address as t0_ where t0_.address_id = ? for update wait 1
*/
```


{{< alert color="warning" title="Warning" >}}
利用している [ダイアレクト]({{< relref "../../dialect.md" >}}) がLockオプションをサポートしない場合、
クエリの実行時に`UnsupportedOperationException`がスローされます。
{{< /alert >}}

`forUpdate`に渡すラムダ式の中で`of`関数を使うことでLock対象のテーブルを指定できます。

```kotlin
val a = Meta.address
val e = Meta.employee
val address: Address = db.runQuery {
    QueryDsl.from(a)
        .innerJoin(e) { a.addressId eq e.addressId }
        .where { a.addressId eq 10 }
        .forUpdate {
            of(a)
            nowait()
        }
        .first()
}
/*
select t0_.address_id, t0_.street, t0_.version from address as t0_ inner join employee as t1_ on (t0_.address_id = t1_.address_id) where t0_.address_id = ? for update of t0_ nowait
*/
```

{{< alert color="warning" title="Warning" >}}
利用している [ダイアレクト]({{< relref "../../dialect.md" >}}) がLock対象のテーブル指定をサポートしない場合、
クエリの実行時に`UnsupportedOperationException`がスローされます。
{{< /alert >}}

## orderBy

ORDER BY句を指定する場合は`orderBy`を呼び出します。

```kotlin
val query: Query<List<Adress>> = QueryDsl.from(a).orderBy(a.addressId)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ order by t0_.ADDRESS_ID asc
*/
```

デフォルトでは昇順ですが降順を指定する場合はカラムを`orderBy`に渡す前にカラムに対して`desc`を呼び出します。
また、昇順を表す`asc`を明示的に呼び出すことやカラムを複数指定することもできます。

```kotlin
val query: Query<List<Adress>> = QueryDsl.from(a).orderBy(a.addressId.desc(), a.street.asc())
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ order by t0_.ADDRESS_ID desc, t0_.STREET asc
*/
```

NULLのソート順序を制御するために、カラムに対して`ascNullsFirst`、`ascNullsLast`、`descNullsFirst`、`descNullsLast`を呼び出すこともできます。

```kotlin
val query: Query<List<Employee>> = QueryDsl.from(e).orderBy(e.managerId.ascNullsFirst())
/*
select t0_.EMPLOYEE_ID, t0_.EMPLOYEE_NO, t0_.EMPLOYEE_NAME, t0_.MANAGER_ID, t0_.HIREDATE, t0_.SALARY, t0_.DEPARTMENT_ID, t0_.ADDRESS_ID, t0_.VERSION from EMPLOYEE as t0_ order by t0_.MANAGER_ID asc nulls first
 */
```

## offset, limit {#offset-limit}

指定した位置から一部の行を取り出すには`offset`や`limit`を呼び出します。

```kotlin
val query: Query<List<Adress>> = QueryDsl.from(a).orderBy(a.addressId).offset(10).limit(3)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ order by t0_.ADDRESS_ID asc offset ? rows fetch first ? rows only
*/
```

## distinct

DISTINCTキーワードを指定するには`distinct`を呼び出します。

```kotlin
val query: Query<List<Department>> = QueryDsl.from(d).distinct().innerJoin(e) { d.departmentId eq e.departmentId }
/*
select distinct t0_.DEPARTMENT_ID, t0_.DEPARTMENT_NO, t0_.DEPARTMENT_NAME, t0_.LOCATION, t0_.VERSION from DEPARTMENT as t0_ inner join EMPLOYEE as t1_ on (t0_.DEPARTMENT_ID = t1_.DEPARTMENT_ID)
*/
```

## select

射影を行うには`select`を呼び出します。

1つのカラムを射影する例です。

```kotlin
val query: Query<List<String?>> = QueryDsl.from(a)
    .where {
        a.addressId inList listOf(1, 2)
    }
    .orderBy(a.addressId)
    .select(a.street)
/*
select t0_.STREET from ADDRESS as t0_ where t0_.ADDRESS_ID in (?, ?) order by t0_.ADDRESS_ID asc
*/
```

2つのカラムを射影する例です。

```kotlin
val query: Query<List<Pair<Int?, String?>>> = QueryDsl.from(a)
    .where {
        a.addressId inList listOf(1, 2)
    }
    .orderBy(a.addressId)
    .select(a.addressId, a.street)
/*
select t0_.ADDRESS_ID, t0_.STREET from ADDRESS as t0_ where t0_.ADDRESS_ID in (?, ?) order by t0_.ADDRESS_ID asc
*/
```

3つのカラムを射影する例です。

```kotlin
val query: Query<List<Triple<Int?, String?, Int?>>> = QueryDsl.from(a)
    .where {
        a.addressId inList listOf(1, 2)
    }
    .orderBy(a.addressId)
    .select(a.addressId, a.street, a.version)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID in (?, ?) order by t0_.ADDRESS_ID asc
*/
```

4つ以上のカラムを射影する例です。

```kotlin
val query: Query<List<Record>> = QueryDsl.from(a)
    .where {
        a.addressId inList listOf(1, 2)
    }
    .orderBy(a.addressId)
    .select(a.addressId, a.street, a.version, concat(a.street, " test"))
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION, (concat(t0_.STREET, ?)) from ADDRESS as t0_ where t0_.ADDRESS_ID in (?, ?) order by t0_.ADDRESS_ID asc
*/

val list: List<Record> = db.runQuery { query }
for (record: Record in list) {
  println(record[a.addressId])
  println(record[a.street])
  println(record[a.version])
  println(record[concat(a.street, " test")])
}
```

4つ以上のカラムを射影した場合、結果の値は`Record`に含まれます。
クエリの`select`に指定したカラムをkeyにして値を取得できます。

## selectNotNull

NULLでないことが確実なカラムを射影するには`selectNotNull`を呼び出せます。

1つのカラムを射影する例です。

```kotlin
val query: Query<List<String>> = QueryDsl.from(a)
    .where {
        a.addressId inList listOf(1, 2)
    }
    .orderBy(a.addressId)
    .selectNotNull(a.street)
/*
select t0_.STREET from ADDRESS as t0_ where t0_.ADDRESS_ID in (?, ?) order by t0_.ADDRESS_ID asc
*/
```

`query`の型が`Query<List<String>>`であることに注目してください。
これは、`query`を実行して得られる値の型が`<List<String>>`であることを表しています。

なお、 このケースで`selectNotNull`の代わりに`select`を使うと、実行して得られる型は`<List<String?>>`です。

## selectAsRecord

4つ未満のカラムの射影で結果を`Record`として受け取りたい場合は`select`の代わりに`selectAsRecord`を呼び出します。

```kotlin
val query: Query<List<Record> = QueryDsl.from(a)
    .where {
        a.addressId inList listOf(1, 2)
    }
    .orderBy(a.addressId)
    .selectAsRecord(a.street)
/*
select t0_.STREET from ADDRESS as t0_ where t0_.ADDRESS_ID in (?, ?) order by t0_.ADDRESS_ID asc
*/
```

## selectAsEntity

結果を射影して任意のエンティティとして受け取りたい場合は`selectAsEntity`を呼び出します。
第一引数にはエンティティのメタモデル、第二引数以降には射影するプロパティを指定します。
プロパティの順番や型はエンティティクラスのコンストラクタに合わせなければいけません。

次の例では`EMPLOYEE`テーブルを検索していますが、結果を`Address`エンティティとして受け取っています。

```kotlin
val query: Query<List<Address> = QueryDsl.from(e)
    .selectAsEntity(a, e.addressId, e.employeeName, e.version)
/*
select t0_.ADDRESS_ID, t0_.EMPLOYEE_NAME, t0_.VERSION from EMPLOYEE as t0_
*/
```

結果として受け取りたいエンティティクラスに`@KomapperProjection`を付与している場合、
専用の拡張関数を使って以下のように簡潔に記述できます。

```kotlin
val e = Meta.employee

val query: Query<List<Address>> = QueryDsl.from(e)
    .selectAsAddress(
        version = e.version,
        addressId = e.addressId,
        street = e.employeeName,
    )
```

名前つき引数を使えばプロパティを指定する順番は自由です。

## having

HAVING句を指定するには`having`を呼び出します。

```kotlin
val query: Query<List<Pair<Int?, Long?>>> = QueryDsl.from(e)
    .having {
        count(e.employeeId) greaterEq 4L
    }
    .orderBy(e.departmentId)
    .select(e.departmentId, count(e.employeeId))
/*
select t0_.DEPARTMENT_ID, count(t0_.EMPLOYEE_ID) from EMPLOYEE as t0_ group by t0_.DEPARTMENT_ID having count(t0_.EMPLOYEE_ID) >= ? order by t0_.DEPARTMENT_ID asc
*/
```

{{< alert title="Note" >}}
`groupBy`の呼び出しがない場合、GROUP BY句は`select`関数に渡された引数から推測されて生成されます。
{{< /alert >}}

## groupBy

GROUP BY句を指定するには`groupBy`を呼び出します。

```kotlin
val query: Query<List<Pair<Int?, Long?>>> = QueryDsl.from(e)
    .groupBy(e.departmentId)
    .having {
        count(e.employeeId) greaterEq 4L
    }
    .orderBy(e.departmentId)
    .select(e.departmentId, count(e.employeeId))
/*
select t0_.DEPARTMENT_ID, count(t0_.EMPLOYEE_ID) from EMPLOYEE as t0_ group by t0_.DEPARTMENT_ID having count(t0_.EMPLOYEE_ID) >= ? order by t0_.DEPARTMENT_ID asc
*/
```

## union

UNION演算を行うにはクエリを`union`で連携します。

```kotlin
val q1: Query<List<Pair<Int?, String?>>> = QueryDsl.from(e).where { e.employeeId eq 1 }
    .select(e.employeeId alias "ID", e.employeeName alias "NAME")
val q2: Query<List<Pair<Int?, String?>>> = QueryDsl.from(a).where { a.addressId eq 2 }
  .select(a.addressId alias "ID", a.street alias "NAME")
val q3: Query<List<Pair<Int?, String?>>> = QueryDsl.from(d).where { d.departmentId eq 3 }
  .select(d.departmentId alias "ID", d.departmentName alias "NAME")
val query: Query<List<Pair<Int?, String?>>> = (q1 union q2 union q3).orderBy("ID", desc("NAME"))
/*
(select t0_.EMPLOYEE_ID as "ID", t0_.EMPLOYEE_NAME as "NAME" from EMPLOYEE as t0_ where t0_.EMPLOYEE_ID = ?) union (select t1_.ADDRESS_ID as "ID", t1_.STREET as "NAME" from ADDRESS as t1_ where t1_.ADDRESS_ID = ?) union (select t2_.DEPARTMENT_ID as "ID", t2_.DEPARTMENT_NAME as "NAME" from DEPARTMENT as t2_ where t2_.DEPARTMENT_ID = ?) order by "ID" asc, "NAME" desc
*/
```

{{< alert title="Note" >}}
`union`に加え、`unionAll`、`except`、`intersect`といった関数をセット演算子として利用できます。
ただし、[ダイアレクト]({{< relref "../../dialect.md" >}}) がサポートしていない場合、
クエリを実行した時点で`UnsupportedOperationException`がスローされます。
{{< /alert >}}

## first

最初の1件を返却するクエリであることを示すには最後に`first`を呼び出します。

```kotlin
val query: Query<Address> = QueryDsl.from(a).where { a.addressId eq 1 }.first()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

## firstOrNull

最初の1件もしくは`null`を返却するクエリであることを示すには最後に`firstOrNull`を呼び出します。

```kotlin
val query: Query<Address?> = QueryDsl.from(a).where { a.addressId eq 1 }.firstOrNull()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

`firstOrNull`関数は、クエリの結果が空の場合に`null`を返します。

## single

必ず1件を返却するクエリであることを示すには最後に`single`を呼び出します。

```kotlin
val query: Query<Address> = QueryDsl.from(a).where { a.addressId eq 1 }.single()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

## singleOrNull

1件もしくは`null`を返却するクエリであることを示すには最後に`singleOrNull`を呼び出します。

```kotlin
val query: Query<Address?> = QueryDsl.from(a).where { a.addressId eq 1 }.singleOrNull()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

`singleOrNull`関数は、クエリの結果が空もしくは2件以上の行を持つ場合に`null`を返します。

## collect

結果セットを`kotlinx.coroutines.flow.Flow`として処理するには最後に`collect`を呼び出します。

```kotlin
val query: Query<Unit> = QueryDsl.from(a).collect { flow: Flow<Address> -> flow.collect { println(it) } }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_
*/
```

{{< alert title="Note" >}}
`collect`を使うと、結果セットを全てメモリに読み込んだ後に処理するのではなく結果セットを1件ずつ読み込みながら処理することになります。
したがって、メモリの使用効率を向上させられます。
{{< /alert >}}

## include

JOINしたテーブルのカラムをSELECT句に含める場合は`include`を呼び出します。

```kotlin
val a = Meta.address
val e = Meta.employee
val d = Meta.department

val query: Query<EntityStore> = QueryDsl.from(a)
  .innerJoin(e) {
    a.addressId eq e.addressId
  }.innerJoin(d) {
    e.departmentId eq d.departmentId
  }.include(e, d)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION, t1_.EMPLOYEE_ID, t1_.EMPLOYEE_NO, t1_.EMPLOYEE_NAME, t1_.MANAGER_ID, t1_.HIREDATE, t1_.SALARY, t1_.DEPARTMENT_ID, t1_.ADDRESS_ID, t1_.VERSION, t2_.DEPARTMENT_ID, t2_.DEPARTMENT_NO, t2_.DEPARTMENT_NAME, t2_.LOCATION, t2_.VERSION from ADDRESS as t0_ inner join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID) inner join DEPARTMENT as t2_ on (t1_.DEPARTMENT_ID = t2_.DEPARTMENT_ID)
*/
```

このクエリを実行した場合の戻り値は、SQLの結果セットから生成された複数のエンティティを保持する
`org.komapper.core.dsl.query.EntityStore`インスタンスです。

`EntityStore`からエンティティの一覧を`Set`として取得したり、エンティティの関連を`Map`として取得したりが可能です。
上述の`query`を実行して`EntityStore`から一覧や関連を取り出す例を以下に示します。

```kotlin
val store: EntityStore = db.runQuery { query }

val addresses: Set<Address> = store[a]
val employees: Set<Employee> = store[e]
val departments: Set<Department> = store[d]

val departmentEmployees: Map<Department, Set<Employee>> = store.oneToMany(d, e)
val employeeDepartment: Map<Employee, Department?> = store.oneToOne(e, d)
val employeeAddress: Map<Employee, Address?> = store.oneToOne(e, a)
```

関連を表す`Map`のキーをエンティティのIDに変換して取得することもできます。

```kotlin
val departmentIdEmployees: Map<Int, Set<Employee>> = store.oneToManyById(d, e)
```

`EntityStore`からオブジェクトを取り出す手続きを簡易化する[Association API]({{< relref "../../association" >}})も参照ください。

## includeAll

JOINしたテーブル全てのカラムをSELECT句に含めたい場合は、`includeAll`を呼び出します。

```kotlin
val a = Meta.address
val e = Meta.employee
val d = Meta.department

val query: Query<EntityStore> = QueryDsl.from(a)
  .innerJoin(e) {
    a.addressId eq e.addressId
  }.innerJoin(d) {
    e.departmentId eq d.departmentId
  }.includeAll()
/*
select t0_.EMPLOYEE_ID, t0_.EMPLOYEE_NO, t0_.EMPLOYEE_NAME, t0_.MANAGER_ID, t0_.HIREDATE, t0_.SALARY, t0_.DEPARTMENT_ID, t0_.ADDRESS_ID, t0_.VERSION, t1_.ADDRESS_ID, t1_.STREET, t1_.VERSION, t2_.DEPARTMENT_ID, t2_.DEPARTMENT_NO, t2_.DEPARTMENT_NAME, t2_.LOCATION, t2_.VERSION from EMPLOYEE as t0_ inner join ADDRESS as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID) inner join DEPARTMENT as t2_ on (t0_.DEPARTMENT_ID = t2_.DEPARTMENT_ID)
*/
```

これは [include](#include) で示した例と同等です。

## options

クエリの挙動をカスタマイズするには`options`を呼び出します。
ラムダ式のパラメータはデフォルトのオプションを表します。
変更したいプロパティを指定して`copy`メソッドを呼び出してください。

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).options {
    it.copy(
      fetchSize = 100,
      queryTimeoutSeconds = 5
    )
}
```

指定可能なオプションには以下のものがあります。

allowMissingWhereClause
: 空のWHERE句を認めるかどうかです。デフォルトは`true`です。

escapeSequence
: LIKE句に指定されるエスケープシーケンスです。デフォルトは`null`で`Dialect`の値を使うことを示します。

fetchSize
: フェッチサイズです。デフォルトは`null`でドライバの値を使うことを示します。

maxRows
: 最大行数です。デフォルトは`null`でドライバの値を使うことを示します。

queryTimeoutSeconds
: クエリタイムアウトの秒数です。デフォルトは`null`でドライバの値を使うことを示します。

suppressLogging
: SQLのログ出力を抑制するかどうかです。デフォルトは`false`です。

[executionOptions]({{< relref "../../database-config/#executionoptions" >}})
の同名プロパティよりもこちらに明示的に設定した値が優先的に利用されます。
