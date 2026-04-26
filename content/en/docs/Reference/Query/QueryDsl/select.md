---
title: "SELECT Queries"
linkTitle: "SELECT"
weight: 10
description: >
---

## Overview {#overview}

SELECT queries are constructed by calling `from`, `with`, or `select` on `QueryDsl`.

The following query corresponds to SQL that retrieves all records from the `ADDRESS` table.

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_
*/
```

## from

To specify a FROM clause, call `from`.
You should specify the entity metamodel for `from`.

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_
*/
```

With `from`, you can also specify a subquery along with the entity metamodel.
The entity metamodel must correspond to the result of the subquery.

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

The entity definition corresponding to the above `Meta.employeeRank` is as follows:

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

To specify a WITH clause, call `with`.
The `with` function requires specifying an entity metamodel and a subquery.
The entity metamodel must correspond to the result of the subquery.

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

The entity definition corresponding to the above `Meta.employeeRank` is as follows:

```kotlin
@KomapperEntity
data class EmployeeRank(
    @KomapperId
    val employeeId: Int,
    val employeeName: String,
    val rank: Int,
)
```

## values

To build a `VALUES` table value constructor, call `QueryDsl.values`.
The result is a `SubqueryExpression` that can be passed to any function that accepts a subquery,
most notably `from(metamodel, subquery)` and `with(metamodel, subquery)`.
The entity metamodel must correspond to the columns produced by `values`.

The following example uses `values` as a derived table:

```kotlin
val t = Meta.t

val rows = QueryDsl.values(t) {
    row { t.n eq 1 }
    row { t.n eq 2 }
    row { t.n eq 3 }
}

val query = QueryDsl.from(t, rows).where { t.n greater 1 }.orderBy(t.n)
/*
select t0_.n from (values (?), (?), (?)) as t0_ (n) where t0_.n > ? order by t0_.n asc
*/
```

The following example uses `values` as the body of a CTE:

```kotlin
val t = Meta.t

val rows = QueryDsl.values(t) {
    row { t.n eq 1 }
    row { t.n eq 2 }
    row { t.n eq 3 }
}

val query = QueryDsl.with(t, rows).from(t).orderBy(t.n).select(t.n)
/*
with t (n) as (values (?), (?), (?)) select t0_.n from t as t0_ order by t0_.n asc
*/
```

The entity definition corresponding to the above `Meta.t` is as follows:

```kotlin
@KomapperEntity
data class T(
    @KomapperId(virtual = true)
    val n: Int,
)
```

The generated SQL varies by dialect:

- MySQL 8 emits `VALUES ROW(...)`.
- MariaDB and MySQL 5 fall back to `SELECT ... UNION ALL ...`.
- SQL Server wraps the `VALUES` in `SELECT ... FROM (VALUES ...)` when used as a CTE body.
- MySQL 5 does not support CTEs at all, so `with(metamodel, values)` is unavailable.

## withRecursive

To specify a WITH RECURSIVE clause, call `withRecursive`.
The `withRecursive` function requires specifying an entity metamodel and a subquery.
The entity metamodel must correspond to the result of the subquery.

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

The entity definition corresponding to the above `Meta.t` is as follows:

```kotlin
@KomapperEntity
data class T(
    @KomapperId(virtual = true)
    val n: Int,
)
```

## where

To specify a WHERE clause, call the `where` function:

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

## innerJoin

To perform an INNER JOIN, call the `innerJoin` function:

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).innerJoin(e) { a.addressId eq e.addressId }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ inner join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID)
*/
```

## leftJoin

To perform an LEFT OUTER JOIN, call the `leftJoin` function:

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).leftJoin(e) { a.addressId eq e.addressId }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ left outer join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID)
*/
```

## fullJoin

To perform a FULL OUTER JOIN, call the `fullJoin` function:

```kotlin
val query: Query<List<Pair<String?, String?>>> =
    QueryDsl.from(a).fullJoin(e) { a.addressId eq e.addressId }.select(a.street, e.employeeName)
/*
select t0_.STREET, t1_.EMPLOYEE_NAME from ADDRESS as t0_ full outer join EMPLOYEE as t1_ on (t0_.ADDRESS_ID = t1_.ADDRESS_ID)
*/
```

{{< alert color="warning" title="Warning" >}}
If the [Dialect]({{< relref "../../dialect.md" >}}) you are using does not support FULL OUTER JOIN (e.g., H2, MariaDB, MySQL),
`UnsupportedOperationException` will be thrown when executing the query.
FULL OUTER JOIN is supported by PostgreSQL, Oracle, and SQL Server.
{{< /alert >}}

## forUpdate

To specify a FOR UPDATE clause, call the `forUpdate` function:

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).where { a.addressId eq 1 }.forUpdate()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ? for update
*/
```

In the lambda expression passed to the `forUpdate` function, 
you can specify lock options by calling functions such as `nowait`, `skipLocked`, and `wait`.

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
If the [Dialect]({{< relref "../../dialect.md" >}}) you are using does not support the lock option
`UnsupportedOperationException` will be thrown when executing the query.
{{< /alert >}}

The table to be locked can be specified by using the `of` function in a lambda expression passed to the `forUpdate` function.

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
If the [Dialect]({{< relref "../../dialect.md" >}}) you are using does not support specifying the table to be locked
`UnsupportedOperationException` will be thrown when executing the query.
{{< /alert >}}

## orderBy

To specify an ORDER BY clause, call the `orderBy` function:

```kotlin
val query: Query<List<Adress>> = QueryDsl.from(a).orderBy(a.addressId)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ order by t0_.ADDRESS_ID asc
*/
```

The default order is ascending. 
To specify descending order, call the `desc` function on the column before passing the column to the `orderBy` function. 
You can also explicitly call the `asc` function for ascending order.

Multiple columns can be specified in the `orderBy` function.

```kotlin
val query: Query<List<Adress>> = QueryDsl.from(a).orderBy(a.addressId.desc(), a.street.asc())
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ order by t0_.ADDRESS_ID desc, t0_.STREET asc
*/
```

To control the sort order of nulls, functions such as
`ascNullsFirst`, `ascNullsLast`, `descNullsFirst`, and `descNullsLast` 
can also be called on the columns.

```kotlin
val query: Query<List<Employee>> = QueryDsl.from(e).orderBy(e.managerId.ascNullsFirst())
/*
select t0_.EMPLOYEE_ID, t0_.EMPLOYEE_NO, t0_.EMPLOYEE_NAME, t0_.MANAGER_ID, t0_.HIREDATE, t0_.SALARY, t0_.DEPARTMENT_ID, t0_.ADDRESS_ID, t0_.VERSION from EMPLOYEE as t0_ order by t0_.MANAGER_ID asc nulls first
 */
```

## offset, limit {#offset-limit}

To extract a portion of rows from a specified position, call the `offset` and `limit` function:

```kotlin
val query: Query<List<Adress>> = QueryDsl.from(a).orderBy(a.addressId).offset(10).limit(3)
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ order by t0_.ADDRESS_ID asc offset ? rows fetch first ? rows only
*/
```

## distinct

To specify a DISTINCT keyword, call the `distinct` function:

```kotlin
val query: Query<List<Department>> = QueryDsl.from(d).distinct().innerJoin(e) { d.departmentId eq e.departmentId }
/*
select distinct t0_.DEPARTMENT_ID, t0_.DEPARTMENT_NO, t0_.DEPARTMENT_NAME, t0_.LOCATION, t0_.VERSION from DEPARTMENT as t0_ inner join EMPLOYEE as t1_ on (t0_.DEPARTMENT_ID = t1_.DEPARTMENT_ID)
*/
```

## select

To do a projection, call the `select` function.

Here is an example of projecting a single column:

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

Here is an example of projecting two columns:

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

Here is an example of projecting three columns:

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

Here is an example of projecting four or more columns:

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

If more than three columns are projected, the resulting value will be included in a `Record`. 
You can retrieve the values from the `Record` using the columns specified in the `select` function as keys.

## selectNotNull

To project a column that is certain not to be null, call the `selectNotNull` function.

Here is an example of projecting a single column:

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

## selectAsRecord

If you want to receive the result as a `Record` in a projection of less than four columns, 
call the `selectAsRecord` function instead of the `select` function.

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

If you want to project the result and receive it as a specific entity, call `selectAsEntity`. Specify the entity's metamodel as the first argument, and the properties to project as the subsequent arguments. The order and type of properties must match the constructor of the entity class.

In the following example, the `EMPLOYEE` table is being queried, but the result is received as an `Address` entity.

```kotlin
val query: Query<List<Address>> = QueryDsl.from(e)
    .selectAsEntity(a, e.addressId, e.employeeName, e.version)
/*
select t0_.ADDRESS_ID, t0_.EMPLOYEE_NAME, t0_.VERSION from EMPLOYEE as t0_
*/
```

When you annotate the entity class you want to receive as a result with `@KomapperProjection`,
you can use a dedicated extension function to write your code more concisely as follows:

```kotlin
val e = Meta.employee

val query: Query<List<Address>> = QueryDsl.from(e)
    .selectAsAddress(
        version = e.version,
        addressId = e.addressId,
        street = e.employeeName,
    )
```

Using named arguments, you have the freedom to specify the properties in any order.

## having

To specify a HAVING clause, call the `having` function:

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
If there is no call to the `groupBy` function, the GROUP BY clause is inferred and generated from 
the arguments passed to the `select` function.
{{< /alert >}}

## groupBy

To specify a GROUP BY clause, call the `groupBy` function.

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

To specify a UNION operation, combine queries with the `union` function:

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
In addition to the `union` function, `unionAll`, `except`, and `intersect` functions are available as set operators.
However, if the [Dialect]({{< relref "../../dialect.md" >}}) you are using does not support them,
`UnsupportedOperationException` will be thrown when executing the query.
{{< /alert >}}

## first

To indicate that the query returns first row, call the `first` function at the end:

```kotlin
val query: Query<Address> = QueryDsl.from(a).where { a.addressId eq 1 }.first()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

## firstOrNull

To indicate that the query returns first row or `null`, call the `firstOrNull` function at the end:

```kotlin
val query: Query<Address?> = QueryDsl.from(a).where { a.addressId eq 1 }.firstOrNull()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

The `firstOrNull` function returns `null` if the query result is empty.

## single

To indicate that the query returns absolutely single row, call the `single` function at the end:

```kotlin
val query: Query<Address> = QueryDsl.from(a).where { a.addressId eq 1 }.single()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

## singleOrNull

To indicate that the query returns single row or `null`, call the `singleOrNull` function at the end:

```kotlin
val query: Query<Address?> = QueryDsl.from(a).where { a.addressId eq 1 }.singleOrNull()
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_ where t0_.ADDRESS_ID = ?
*/
```

The `singleOrNull` function returns `null` if the query result is empty or has more than one row.

## collect

To process the result set as `kotlinx.coroutines.flow.Flow`, call the `collect` function at the end:

```kotlin
val query: Query<Unit> = QueryDsl.from(a).collect { flow: Flow<Address> -> flow.collect { println(it) } }
/*
select t0_.ADDRESS_ID, t0_.STREET, t0_.VERSION from ADDRESS as t0_
*/
```

{{< alert title="Note" >}}
With the `collect` function, all rows are processed as they are read one by one, 
rather than being processed after all rows have been read into memory. 
Thus, memory usage can be improved.
{{< /alert >}}

## include

To include columns from the joined table in the SELECT clause, call the `include` function:

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

When the above query is executed, the return value is a `org.komapper.core.dsl.query.EntityStore` instance.

You can retrieve a set of entities and a map of entity relationships from the `EntityStore` instance as follows:

```kotlin
val store: EntityStore = db.runQuery { query }

val addresses: Set<Address> = store[a]
val employees: Set<Employee> = store[e]
val departments: Set<Department> = store[d]

val departmentEmployees: Map<Department, Set<Employee>> = store.oneToMany(d, e)
val employeeDepartment: Map<Employee, Department?> = store.oneToOne(e, d)
val employeeAddress: Map<Employee, Address?> = store.oneToOne(e, a)
```

You can obtain a map whose key is the ID of the entity:

```kotlin
val departmentIdEmployees: Map<Int, Set<Employee>> = store.oneToManyById(d, e)
```

See also [Association API]({{< relref "../../association" >}}), 
which simplifies the procedure for retrieving entity objects from the `EntityStore`.

## includeAll

To include columns from all joined tables in the SELECT clause, call the `includeAll` function:

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

The above code is equivalent to the example shown in [include](#include).

## options

To customize the behavior of the query, call the `options` function.
The `options` function accept a lambda expression whose parameter represents default options.
Call the `copy` function on the parameter to change its properties.

```kotlin
val query: Query<List<Address>> = QueryDsl.from(a).options {
    it.copy(
      fetchSize = 100,
      queryTimeoutSeconds = 5
    )
}
```

The options that can be specified are as follows:

allowMissingWhereClause
: Whether an empty WHERE clause is allowed or not. Default is `true`.

escapeSequence
: Escape sequence specified for the LIKE predicate. The default is `null` to indicate the use of Dialect values.

fetchSize
: Default is `null` to indicate that the driver value should be used.

hint
: SQL hint embedded as a comment immediately after the `SELECT` keyword. Default is `null`. Useful for engines that read query configuration from comments, such as StarRocks (e.g. `SET_VAR(query_timeout=3600)`).

maxRows
: Default is `null` to indicate use of the driver's value.

queryTimeoutSeconds
: Query timeout in seconds. Default is `null` to indicate that the driver value should be used.

suppressLogging
: Whether to suppress SQL log output. Default is `false`.

Properties explicitly set here will be used in preference to properties with the same name that exist 
in [executionOptions]({{< relref "../../database-config/#executionoptions" >}}).
