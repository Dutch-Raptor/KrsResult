package com.pieterhuizenga.result

/**
 * Executes a block of code and catches any exceptions that are thrown.
 *
 * Also allows binding the results of multiple [KrsResult]s together.
 *
 * @param block The block of code to execute.
 * @return An [KrsResult]<[T], [CatchingResultScopeError]<[E]>> containing the result of the block, or an error if an error was returned or an exception was thrown.
 * Where [T] is the type of the value, and [E] is the type of the error.
 *
 * ### Example:
 * ```
 * val result = catchingResultScope {
 *   val a = MResult.Ok(5).use()
 *   val b = MResult.Ok(10).use()
 *   returnOk(a + b)
 * } // result is MResult.Ok(15)
 *
 * val result = catchingResultScope {
 *   val a = MResult.Ok(5).use()
 *   val b = MResult.Err("error").use() // returns MResult.Err(CatchingScopeError.Failure("error"))
 *   returnOk(a + b) // this line is not executed
 * } // result is MResult.Err(CatchingScopeError.Failure("error"))
 *
 * val result = catchingResultScope {
 *   val a = MResult.Ok(5).use()
 *   throw Exception("error") // returns MResult.Err(CatchingScopeError.Exception(Exception("error")))
 *   returnOk(a) // this line is not executed
 * } // result is MResult.Err(CatchingScopeError.Exception(Exception("error")))
 * ```
 */
public inline fun <T, E> catchingResultScope(block: CatchingResultScope<T, E>.() -> T): KrsResult<T, CatchingResultScopeError<E>> =
    try {
        resultScope(block).mapErr { CatchingResultScopeError.Failure(it) }
    } catch (e: Throwable) {
        KrsResult.Err(CatchingResultScopeError.Exception(e)) // Err!
    }

/**
 * A typealias for [ResultScope].
 */
public typealias CatchingResultScope<T, E> = ResultScope<T, E>

/**
 * Create a scope that allows binding of multiple [KrsResult]s into one [KrsResult]. Within the scope the
 * function [MResult.use][ResultScope.use] is available which either:
 * - Produces the [KrsResult.Ok.value] if the receiving [KrsResult] was [KrsResult.Ok]
 * - Terminates the block with the [KrsResult.Err.error] if it was [KrsResult.Err]
 *
 * The scope also provides two functions to return a value from the scope:
 * - [ResultScope.returnOk] to return an [KrsResult.Ok] value from the scope
 * - [ResultScope.returnErr] to return an [KrsResult.Err] value from the scope
 *
 * The scope can be used to chain multiple operations that return an [KrsResult].
 *
 * To also catch exceptions and return them as an error, use either [catchingResultScope] or [caught].
 *
 * ### Example:
 * ```
 * val result = catchingResultScope {
 *    val a = ok<Int, String>(5).mapErr { "a failed" }.use()
 *    val b = ok<Int, String>(10).mapErr { "b failed" }.use()
 *    returnOk(a + b)
 * } // result is MResult.Ok(15)
 * ```
 *
 * @param block The block of code to execute.
 *
 * @return An [KrsResult] containing the result of the block, or an error if one was returned.
 *
 * ### Example:
 * ```
 * val result = useBinding {
 *     val a = MResult.Ok(5).use()
 *     val b = MResult.Ok(10).use()
 *
 *     // Return an `ok` value from a useBinding block with
 *     returnOk(a + b)
 *     // or simply
 *     a + b
 *     // or explicitly
 *     return@useBinding a + b
 * } // result is MResult.Ok(15)
 *
 * val result2 = useBinding {
 *     val a = MResult.Ok(5).use()
 *     val b = MResult.Err("error").use() // terminates the block with MResult.Err("error")
 *     returnOk(a + b) // this line is not executed
 * } // result2 is MResult.Err("error")
 *
 * val result3 = useBinding {
 *     val a = MResult.Ok(5).use()
 *     // manually return an error with
 *     returnErr("error")
 * }
 * ```
 *
 * @see [ResultScope]
 */
public inline fun <T, E> resultScope(block: ResultScope<T, E>.() -> T): KrsResult<T, E> {
    val scope = ResultScopeImpl<T, E>()
    return try {
        KrsResult.Ok(scope.block())
    } catch (e: ResultScopeValueException) {
        KrsResult.Ok(scope.bindingScopeValue!!)
    } catch (e: ResultScopeException) {
        KrsResult.Err(scope.bindingScopeError!!)
    }
}


/**
 * A scope that allows binding of multiple [KrsResult]s into one [KrsResult]. Within the scope the
 * function [MResult.use][ResultScope.use] is available which either:
 * - Produces the [KrsResult.Ok.value] if the receiving [KrsResult] was [KrsResult.Ok]
 * - Terminates the block with the [KrsResult.Err.error] if it was [KrsResult.Err]
 *
 * The scope also provides two functions to return a value from the scope:
 * - [ResultScope.returnOk] to return an [KrsResult.Ok] value from the scope
 * - [ResultScope.returnErr] to return an [KrsResult.Err] value from the scope
 *
 * The scope can be used to chain multiple operations that return an [KrsResult].
 */
public interface ResultScope<T, E> {
    /**
     * Use the value of the result, or terminate the [ResultScope] with the error.
     *
     * @return The value of the result.
     *
     * ### Example:
     * ```
     * val result = resultScope {
     *    val resultA = MResult.Ok(5)
     *    val a = resultA.use() // a is 5
     *
     *    val resultB = MResult.Err("error")
     *    val b = resultB.use() // terminates the block with MResult.Err("error")
     *
     *    a
     * } // result is MResult.Err("error") as an MResult.Err was used in the block
     * ```
     */
    public fun <T> KrsResult<T, E>.use(): T

    /**
     * Return a value as a [KrsResult.Ok] from the [ResultScope].
     *
     * @param value The value to return.
     *
     * Is analogous to just returning the value from the block, but is more explicit.
     *
     * ### Example:
     * ```
     * val result = resultScope {
     *   val a = MResult.Ok(5).use()
     *   val b = MResult.Ok(10).use()
     *   returnOk(a + b)
     * } // result is MResult.Ok(15)
     * ```
     */
    public fun returnOk(value: T): Nothing

    /**
     * Return an error as a [KrsResult.Err] from the [ResultScope].
     *
     * @param error The error to return.
     *
     * ### Example:
     * ```
     * val result = resultScope {
     *   val fallibleOperation = MResult.Err("error")
     *
     *   if (fallibleOperation.isErr()) {
     *       returnErr(fallibleOperation.error)
     *   }
     *   // ...
     * } // result is MResult.Err("error")
     * ```
     *
     */
    public fun returnErr(error: E): Nothing
}

public class ResultScopeImpl<T, E> : ResultScope<T, E> {
    public var bindingScopeError: E? = null
    public var bindingScopeValue: T? = null

    /**
     * Use the value of the result, or terminate the [ResultScope] with the error.
     */
    override fun <T> KrsResult<T, E>.use(): T {
        if (this.isErr()) {
            bindingScopeError = this.error
            throw ResultScopeException
        }

        return this.value
    }

    /**
     * Return a value from the [ResultScope].
     */
    override fun returnOk(value: T): Nothing {
        bindingScopeValue = value
        throw ResultScopeValueException
    }

    override fun returnErr(error: E): Nothing {
        bindingScopeError = error
        throw ResultScopeException
    }
}

internal object ResultScopeException : Throwable() {}
internal object ResultScopeValueException : Throwable() {}

/**
 * Executes a block of code and catches any exceptions that are thrown.
 *
 * @param block The block of code to execute.
 * @return An [KrsResult] containing the result of the block, or an error if an exception was thrown.
 *
 */
public inline fun <T> caught(block: ResultScope<T, Throwable>.() -> T): KrsResult<T, Throwable> {
    val scope = ResultScopeImpl<T, Throwable>()
    return try {
        KrsResult.Ok(scope.block()) // Ok!
    } catch (e: ResultScopeValueException) {
        KrsResult.Ok(scope.bindingScopeValue!!) // Ok!
    } catch (e: ResultScopeException) {
        KrsResult.Err(scope.bindingScopeError!!) // Err!
    } catch (e: Throwable) {
        KrsResult.Err(e) // Err!
    }
}
