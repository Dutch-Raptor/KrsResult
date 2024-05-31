package com.pieterhuizenga.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Represents an error created in a [CatchingResultScope].
 *
 * This can be either a failure value [E] or an exception [Throwable].
 *
 * @param E The type of the error.
 *
 *
 */
public sealed class CatchingResultScopeError<E> {
    /**
     * Represents a failure value.
     *
     * @param value The value of the failure.
     */
    public data class Failure<E>(val value: E) : CatchingResultScopeError<E>()

    /**
     * Represents an exception.
     *
     * @param exception The exception that was thrown.
     */
    public data class Exception<E>(val exception: Throwable) : CatchingResultScopeError<E>()
}

public fun <E> CatchingResultScopeError<E>.valueOr(default: E): E {
    return when (this@valueOr) {
        is CatchingResultScopeError.Failure -> this.value
        is CatchingResultScopeError.Exception -> default
    }
}

/**
 * Returns true if the error is an [Exception], otherwise false.
 *
 * @return True if the error is an [Exception], otherwise false.
 */
@OptIn(ExperimentalContracts::class)
public fun <E> CatchingResultScopeError<E>.isException(): Boolean {
    contract {
        returns(true) implies (this@isException is CatchingResultScopeError.Exception)
        returns(false) implies (this@isException is CatchingResultScopeError.Failure)
    }
    return this is CatchingResultScopeError.Exception
}

/**
 * Returns true if the error is a [CatchingResultScopeError.Failure], otherwise false.
 *
 * @return True if the error is a [CatchingResultScopeError.Failure], otherwise false.
 */
@OptIn(ExperimentalContracts::class)
public fun <E> CatchingResultScopeError<E>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is CatchingResultScopeError.Failure)
        returns(false) implies (this@isFailure is CatchingResultScopeError.Exception)
    }
    return this is CatchingResultScopeError.Failure
}

/**
 * Returns the exception if the error is an [CatchingResultScopeError.Exception], otherwise null.
 *
 * @return The exception if the error is an [CatchingResultScopeError.Exception], otherwise null.
 */
public fun <E> CatchingResultScopeError<E>.exception(): Throwable? = if (this@exception is CatchingResultScopeError.Exception) {
    this.exception
} else null

/**
 * Returns the value if the error is a [CatchingResultScopeError.Failure], otherwise null.
 *
 * @return The value if the error is a [CatchingResultScopeError.Failure], otherwise null.
 */
public fun <E> CatchingResultScopeError<E>.failure(): E? = if (this@failure is CatchingResultScopeError.Failure) {
    this.value
} else null

public fun <T, E> KrsResult<T, CatchingResultScopeError<E>>.mapException(transform: (Throwable) -> E): KrsResult<T, E> {
    return when (this) {
        is KrsResult.Ok -> KrsResult.Ok(this.value)
        is KrsResult.Err -> {
            when (val error = this@mapException.error) {
                is CatchingResultScopeError.Failure -> KrsResult.Err(error.value)
                is CatchingResultScopeError.Exception -> KrsResult.Err(transform(error.exception))
            }
        }

    }
}