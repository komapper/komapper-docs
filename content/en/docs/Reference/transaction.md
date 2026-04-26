---
title: "Transaction"
weight: 40
description: >
---

## Overview {#overview}

Komapper provides a High-level API for transaction management.

{{< alert title="Note" >}}
When Komapper is combined with Spring Framework or Quarkus, 
this API works with the Spring Framework and Quarkus transaction managers.
{{< /alert >}}

## Transaction control {#transaction-control}

The API for transaction control provided by Komapper differs between the JDBC and R2DBC versions, 
but the visual interface is unified. 
The following explanation assumes that `JdbcDatabase` or `R2dbcDatabase` is declared with 
the variable `db` as shown below.

```kotlin
val db = JdbcDatabase("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
```

```kotlin
val db = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")
```

### Beginning and ending transactions {#beginning-and-end}

A transaction can be begun by calling the `withTransaction` function:

```kotlin
db.withTransaction {
    ..
}
```

The `withTransaction` function accepts a transaction attribute and a transaction property`:

```kotlin
db.withTransaction(
  transactionAttribute = TransactionAttribute.REQUIRES_NEW, 
  transactionProperty = TransactionProperty.IsolationLevel.SERIALIZABLE) {
    ..
}
```

The transaction attribute can be one of the following two types:

REQUIRED
: Support a current transaction; create a new one if none exists.

REQUIRES_NEW
: Create a new transaction, suspending the current transaction if one exists.

The transaction property can contain various property elements:

IsolationLevel
: Isolation level requested for the transaction.

LockWaitTime
: Lock wait timeout.

Name
: Name of the transaction.

ReadOnly
: Whether the transaction should be started in read-only mode.


Multiple transaction property elements can be combined into a single transaction property with the `+` operator:

```kotlin
val property = TransactionProperty.IsolationLevel.SERIALIZABLE + TransactionProperty.Name("myTx") + TransactionProperty.ReadOnly(true)
```

The transaction is committed or rolled back when the call to the `withTransaction` function is finished.

The conditions for rollback are as follows:

- Call to the `withTransaction` function is terminated with an exception.
- [Explicit rollback]({{< relref "#explicit-rollback" >}}) is indicated in `withTransaction` function.

If the above rollback conditions are not met, the commit is performed.

### Explicit rollback {#explicit-rollback}

If the `setRollbackOnly` function is called, rollback is performed at the end of the `withTransaction` function:

```kotlin
db.withTransaction { tx ->
    ..
    tx.setRollbackOnly()
    ..
}
```

You can check if you have already called the `setRollbackOnly` function with the `isRollbackOnly` function:

```kotlin
db.withTransaction { tx ->
    ..
    if (tx.isRollbackOnly()) {
        ..
    }
    ..
}
```

### Inspecting the current transaction {#inspecting-current-transaction}

The `isActive` function returns `true` while the code is executing inside a transaction block, and `false` otherwise.
This is useful for code paths that may be invoked both inside and outside of a transactional context.

```kotlin
db.withTransaction { tx ->
    ..
    if (tx.isActive()) {
        ..
    }
    ..
}
```

The `transactionProperty` property exposes the combined `TransactionProperty` of the current transaction.
The value reflects the property merged through nested `required` and `requiresNew` calls.

```kotlin
db.withTransaction(transactionProperty = TransactionProperty.Name("myTx")) { tx ->
    val name: String? = tx.transactionProperty[TransactionProperty.Name]?.value
    ..
}
```

### Beginning and ending new transactions {#beginning-and-ending-new-transaction}

To begin a new transaction when the current transaction exists, call the `requiresNew` function.

```kotlin
db.withTransaction { tx ->
    ..
    tx.requiresNew {
        ..
    }
    ..
}
```

The `requiresNew` function accepts a transaction property`:

```kotlin
db.withTransaction { tx ->
    ..
    tx.requiresNew(transactionProperty = TransactionProperty.IsolationLevel.SERIALIZABLE) {
        ..
    }
    ..
}
```

The transaction is committed or rolled back when the call to the `requiresNew` function is finished.

The conditions for rollback are as follows:

- Call to the `requiresNew` function is terminated with an exception.
- [Explicit rollback]({{< relref "#explicit-rollback" >}}) is indicated in the `requiresNew` function.

If the above rollback conditions are not met, the commit is performed.

## Transactional flows {#transactional-flow}

Only `R2dbcDatabase` provides the `flowTransaction` function that constructs a transactional flow:

```kotlin
val db = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")

val transactionalFlow: Flow<Address> = db.flowTransaction {
    val a = Meta.address
    val address = db.runQuery {
        QueryDsl.from(a).where { a.addressId eq 15 }.first()
    }
    db.runQuery { 
        QueryDsl.update(a).single(address.copy(street = "TOKYO")) 
    }
    val addressFlow = db.flowQuery { 
        QueryDsl.from(a).orderBy(a.addressId)
    }
    emitAll(addressFlow)
}

// Transaction is executed
val list = transactionalFlow.toList()
```

In the above example, the call to the `flowTransaction` function only constructs a flow. 
The transaction is executed for the first time when the `transactionalFlow` is collected.
