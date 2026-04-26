---
title: "トランザクション"
weight: 40
description: >
---

## 概要 {#overview}

Komapperはトランザクション管理の高レベルAPIを提供します。

{{< alert title="Note" >}}
KomapperをSpring FrameworkやQuarkusと組み合わせる場合、
このAPIはSpring FrameworkやQuarkusのトランザクションマネージャと連動します。
{{< /alert >}}

## トランザクションの制御 {#transaction-control}

Komapperが提供するトランザクション制御のためのAPIはJDBC版とR2DBC版で異なりますが見た目上のインターフェースは統一されています。
`JdbcDatabase`もしくは`R2dbcDatabase`が下記のように`db`という変数で宣言されていることを前提に説明を進めます。

```kotlin
val db = JdbcDatabase("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
```

```kotlin
val db = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")
```

### トランザクションの開始と終了 {#beginning-and-ending}

`db`に定義された`withTransaction`関数の呼び出すことでトランザクションを開始できます。

```kotlin
db.withTransaction {
    ..
}
```

`withTransaction`関数にはトランザクション属性とトランザクションプロパティを指定できます。

```kotlin
db.withTransaction(
  transactionAttribute = TransactionAttribute.REQUIRES_NEW, 
  transactionProperty = TransactionProperty.IsolationLevel.SERIALIZABLE) {
    ..
}
```

トランザクション属性は以下の2種類のいずれかを指定できます。

REQUIRED
: 現在のトランザクションをサポートし、存在しない場合は新しいトランザクションを作成する

REQUIRES_NEW
: 新しいトランザクションを作成し、現在のトランザクションが存在する場合はそれを一時停止する

トランザクションプロパティは、様々なプロパティ要素を含むことができます。

IsolationLevel
: トランザクション分離レベル

LockWaitTime
: ロック待機時間

Name
: トランザクションの名前

ReadOnly
: トランザクションをリードオンリーモードで開始するかどうか

複数のトランザクションプロパティ要素を `+`演算子で1つのトランザクションプロパティにまとめることができます。

```kotlin
val property = TransactionProperty.IsolationLevel.SERIALIZABLE + TransactionProperty.Name("myTx") + TransactionProperty.ReadOnly(true)
```

`withTransaction`関数の呼び出しが終わるとトランザクションはコミットもしくはロールバックされます。

ロールバックされる条件は次のとおりです。

- `withTransaction`関数の呼び出しが例外のスローにより終了する
- `withTransaction`関数に渡されたラムダ式の中で明示的にロールバックを行う

ロールバックの条件に合致しない場合コミットされます。

### 明示的なロールバック {#explicit-rollback}

`setRollbackOnly`関数を呼び出すと`withTransaction`関数終了時にロールバックが実行されます。

```kotlin
db.withTransaction { tx ->
    ..
    tx.setRollbackOnly()
    ..
}
```

すでに`setRollbackOnly`関数を呼び出したかどうかは`isRollbackOnly`関数で確認できます。

```kotlin
db.withTransaction { tx ->
    ..
    if (tx.isRollbackOnly()) {
        ..
    }
    ..
}
```

### 現在のトランザクションの参照 {#inspecting-current-transaction}

`isActive`関数はトランザクションブロックの内側で実行されている間は`true`を、外側では`false`を返します。
トランザクションの内外どちらからも呼び出される可能性があるコードで利用できます。

```kotlin
db.withTransaction { tx ->
    ..
    if (tx.isActive()) {
        ..
    }
    ..
}
```

`transactionProperty`プロパティは現在のトランザクションに適用されている`TransactionProperty`を返します。
`required`や`requiresNew`をネストして呼び出した場合、それらをマージした結果が反映されます。

```kotlin
db.withTransaction(transactionProperty = TransactionProperty.Name("myTx")) { tx ->
    val name: String? = tx.transactionProperty[TransactionProperty.Name]?.value
    ..
}
```

### 新規トランザクションの開始と終了 {#beginning-and-ending-new-transaction}

すでに開始されたトランザクションの中で別のトランザクションを新しく開始するには`requiresNew`関数を呼び出します。

```kotlin
db.withTransaction { tx ->
    ..
    tx.requiresNew {
        ..
    }
    ..
}
```

`requiresNew`関数にはトランザクションプロパティを指定できます。

```kotlin
db.withTransaction { tx ->
    ..
    tx.requiresNew(transactionProperty = TransactionProperty.IsolationLevel.SERIALIZABLE) {
        ..
    }
    ..
}
```

`requiresNew`関数の呼び出しが終わるとトランザクションはコミットもしくはロールバックされます。

ロールバックされる条件は次のとおりです。

- `requiresNew`関数の呼び出しが例外のスローにより終了する
- `requiresNew`関数に渡されたラムダ式の中で明示的にロールバックを行う

ロールバックの条件に合致しない場合コミットされます。

## トランザクショナルなFlow {#transactional-flow}

`R2dbcDatabase`はトランザクションを表す`Flow`を構築するための`flowTransaction`関数を提供します。

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

上記の例では、`flowTransaction`の呼び出しは`Flow`を構築しているだけです。
トランザクションは`transactionalFlow`を実際に使う際に初めて実行されます。