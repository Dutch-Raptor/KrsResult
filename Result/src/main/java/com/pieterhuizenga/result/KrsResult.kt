@file:Suppress("unused")
package com.pieterhuizenga.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public sealed class KrsResult<T, E> {
    /**
     * Represents a successful result.
     *
     * @param value The value of the result.
     *
     * ### Example:
     * ```
     * val result: MResult<Int, String> = ok(5)
     *
     * when (result) {
     *    is MResult.Ok -> println(result.value)
     *    is MResult.Err -> println(result.error)
     * } // prints 5
     *
     * if (result.isOk()) {
     *   println(result.value)
     *   // prints 5
     * }
     * ```
     */
    public data class Ok<T, E>(val value: T) : KrsResult<T, E>()

    /**
     * Represents a failed result.
     *
     * @param error The error of the result.
     *
     * ### Example:
     * ```
     * val result: MResult<Int, String> = err("error")
     *
     * when (result) {
     *   is MResult.Ok -> println(result.value)
     *   is MResult.Err -> println(result.error)
     *   // prints "error"
     * }
     *
     * if (result.isErr()) {
     *   println(result.error)
     *   // prints "error"
     * }
     * ```
     */
    public data class Err<T, E>(val error: E) : KrsResult<T, E>()
}

public fun <T, E> KrsResult<T, E>.okOr(default: T): T {
    return when (this@okOr) {
        is KrsResult.Ok -> value
        is KrsResult.Err -> default
    }
}

/**
 * Returns the error if the result is [KrsResult.Err], otherwise null.
 *
 * ### Example:
 * ```
 * val result: MResult<Int, String> = err("error")
 * val error = result.err()?.let { println(it) }
 * ```
 */
public fun <T, E> KrsResult<T, E>.err(): E? = if (this.isErr()) {
    this.error
} else null

/**
 * Returns the value if the result is [KrsResult.Ok], otherwise null.
 *
 * ### Example:
 * ```
 * val result: MResult<Int, String> = ok(5)
 * val doubled = result.ok()?.let { it * 2 }
 *
 * assertEqual(10, doubled)
 *
 * val result: MResult<Int, String> = err("error")
 * val doubled = result.ok()?.let { it * 2 }
 *
 * assertEqual(null, doubled)
 * ```
 *
 * @return The value if the result is [KrsResult.Ok], otherwise null.
 */
public fun <T, E> KrsResult<T, E>.ok(): T? = if (this.isOk()) {
    this.value
} else null


/**
 * Unwraps the value of an [KrsResult.Ok] result, or throws a [RuntimeException] if the result is an [KrsResult.Err].
 *
 * ### Example:
 * ```
 * val result: MResult<Int, String> = ok(5)
 * val value = result.unwrap()
 * assertEqual(5, value)
 *
 * val result: MResult<Int, String> = err("error")
 * val value = result.unwrap() // throws RuntimeException
 * ```
 *
 * @return The value of the [KrsResult.Ok] result.
 * @throws ResultUnwrapException if the result is an [KrsResult.Err].
 */
@OptIn(ExperimentalContracts::class)
public fun <T, E> KrsResult<T, E>.unwrap(): T {
    contract { returns() implies (this@unwrap is KrsResult.Ok<T, E>) }
    return when (this@unwrap) {
        is KrsResult.Ok -> this.value
        is KrsResult.Err -> throw ResultUnwrapException()
    }
}

/**
 * Unwraps the error of an [KrsResult.Err] result, or throws a [RuntimeException] if the result is an [KrsResult.Ok].
 *
 * ### Example:
 * ```
 * val result: MResult<Int, String> = err("error")
 * val error = result.unwrapErr()
 * assertEqual("error", error)
 *
 * val result: MResult<Int, String> = ok(5)
 * val error = result.unwrapErr() // throws RuntimeException
 * ```
 *
 * @return The error of the [KrsResult.Err] result.
 * @throws ResultUnwrapErrException if the result is an [KrsResult.Ok].
 */
@OptIn(ExperimentalContracts::class)
public fun <T, E> KrsResult<T, E>.unwrapErr(): E {
    contract { returns() implies (this@unwrapErr is KrsResult.Err<T, E>) }
    return when (this) {
        is KrsResult.Err -> error
        is KrsResult.Ok -> throw ResultUnwrapErrException()
    }
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> KrsResult<T, E>.isOk(): Boolean {
    contract {
        returns(true) implies (this@isOk is KrsResult.Ok<T, E>)
        returns(false) implies (this@isOk is KrsResult.Err<T, E>)
    }
    return this@isOk is KrsResult.Ok
}

@OptIn(ExperimentalContracts::class)
public fun <T, E> KrsResult<T, E>.isErr(): Boolean {
    contract {
        returns(true) implies (this@isErr is KrsResult.Err<T, E>)
        returns(false) implies (this@isErr is KrsResult.Ok<T, E>)
    }
    return this@isErr is KrsResult.Err
}

public fun <T, E, R> KrsResult<T, E>.mapOrElse(onOk: (T) -> R, onErr: (E) -> R): R {
    return when (this) {
        is KrsResult.Ok -> onOk(value)
        is KrsResult.Err -> onErr(error)
    }
}

public fun <T, E, R> KrsResult<T, E>.mapErrOrElse(onOk: (T) -> R, onErr: (E) -> R): R {
    return when (this) {
        is KrsResult.Ok -> onOk(value)
        is KrsResult.Err -> onErr(error)
    }
}

/**
 * Converts a MResult<T?, E> to a MResult<T, E> by replacing null values with the given error.
 */
public fun <T, E> KrsResult<T?, E>.toResultOr(error: E): KrsResult<T, E> {
    return when (this) {
        is KrsResult.Ok -> ok(value ?: return err(error))
        is KrsResult.Err -> err(error)
    }
}


public fun <T, E> ok(value: T): KrsResult<T, E> = KrsResult.Ok(value)
public fun <T, E> err(error: E): KrsResult<T, E> = KrsResult.Err(error)


public class ResultUnwrapException : RuntimeException("Called unwrap on an Err value")
public class ResultUnwrapErrException : RuntimeException("Called unwrapErr on an Ok value")

