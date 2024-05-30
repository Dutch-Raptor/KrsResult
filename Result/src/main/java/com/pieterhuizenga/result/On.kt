package com.pieterhuizenga.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <T, E>KrsResult<T, E>.onOk(block: (T) -> Unit): KrsResult<T, E> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (this.isOk()) {
        block(value)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
public inline fun <T, E>KrsResult<T, E>.onErr(block: (E) -> Unit): KrsResult<T, E> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (this.isErr()) {
        block(error)
    }
    return this
}
