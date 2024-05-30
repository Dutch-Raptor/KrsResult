package com.pieterhuizenga.result

/**
 * Maps the [KrsResult.Ok] value to a new value provided by the [transform] function.
 *
 * If the result is an [KrsResult.Err], the error is propagated.
 *
 * ### Example:
 * ```
 * val result: KrsResult<Int, String> = ok(5)
 * val mappedResult = result.map { it * 2 }
 *
 * assert(mappedResult is KrsResult.Ok)
 * assert((mappedResult as KrsResult.Ok).value == 10)
 * ```
 *
 * ### Example with error:
 * ```
 * val result: KrsResult<Int, String> = err("error")
 * val mappedResult = result.map { it * 2 }
 *
 * assert(mappedResult is KrsResult.Err)
 * assert((mappedResult as KrsResult.Err).error == "error")
 * ```
 *
 * @param transform The function to transform the value of the result.
 * @return A new [KrsResult] with the transformed value.
 * @see mapErr
 */
public fun <T, E, R> KrsResult<T, E>.map(transform: (T) -> R): KrsResult<R, E> {
    return when (this) {
        is KrsResult.Ok -> ok(transform(value))
        is KrsResult.Err -> err(error)
    }
}

/**
 * Maps the [KrsResult.Err] error to a new error provided by the [transform] function.
 *
 * If the result is an [KrsResult.Ok], the value is propagated.
 *
 * ### Example with [KrsResult.Err]:
 * ```
 * val result: KrsResult<Int, String> = err("error")
 * val mappedResult = result.mapErr { "new error" }
 *
 * assert(mappedResult is KrsResult.Err)
 * assert((mappedResult as KrsResult.Err).error == "new error")
 * ```
 *
 * ### Example an [KrsResult.Ok]:
 * ```
 * val result: KrsResult<Int, String> = ok(5)
 * val mappedResult = result.mapErr { "new error" }
 *
 * assert(mappedResult is KrsResult.Ok)
 * assert((mappedResult as KrsResult.Ok).value == 5)
 * ```
 *
 * @param transform The function to transform the error of the result.
 * @return A new [KrsResult] with the transformed error.
 * @see map
 */
public fun <T, E, F> KrsResult<T, E>.mapErr(transform: (E) -> F): KrsResult<T, F> {
    return when (this) {
        is KrsResult.Ok -> ok(value)
        is KrsResult.Err -> err(transform(error))
    }
}

/**
 * Maps the [KrsResult.Ok] value to a new [KrsResult]<[R], [E]> provided by the [transform] function.
 *
 * If the result is an [KrsResult.Err], the error is propagated.
 *
 * This function is useful when you need to chain multiple operations that return a [KrsResult].
 *
 * See also [resultScope][com.pieterhuizenga.budgetfriend.core.lib.result.result_scope.resultScope] for a more powerful way to chain operations.
 *
 * ### Example:
 * ```
 * val result: KrsResult<Int, String> = ok(5)
 * val mappedResult = result.andThen { ok(it * 2) }
 *
 * assert(mappedResult is KrsResult.Ok)
 * assert((mappedResult as KrsResult.Ok).value == 10)
 * ```
 *
 * ### Example with error:
 * ```
 * val result: KrsResult<Int, String> = err("error")
 * val mappedResult = result.andThen { ok(it * 2) }
 *
 * assert(mappedResult is KrsResult.Err)
 * assert((mappedResult as KrsResult.Err).error == "error")
 * ```
 */
public fun <T, E, R> KrsResult<T, E>.andThen(transform: (T) -> KrsResult<R, E>): KrsResult<R, E> {
    return when (this) {
        is KrsResult.Ok -> transform(value)
        is KrsResult.Err -> err(error)
    }
}

/**
 * Maps the [KrsResult.Err] error to a new [KrsResult]<[T], [F]> provided by the [transform] function.
 *
 * If the result is an [KrsResult.Ok], the value is propagated.
 *
 * This function is useful when you need to chain multiple operations that return a [KrsResult].
 *
 * See also [resultScope][com.pieterhuizenga.budgetfriend.core.lib.result.result_scope.resultScope] for a more powerful way to chain operations.
 *
 */
public fun <T, E, F> KrsResult<T, E>.andThenErr(transform: (E) -> KrsResult<T, F>): KrsResult<T, F> {
    return when (this) {
        is KrsResult.Ok -> ok(value)
        is KrsResult.Err -> transform(error)
    }
}
