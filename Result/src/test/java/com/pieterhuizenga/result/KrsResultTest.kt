package com.pieterhuizenga.result

import com.pieterhuizenga.result.CatchingResultScopeError
import com.pieterhuizenga.result.catchingResultScope
import com.pieterhuizenga.result.collect
import com.pieterhuizenga.result.err
import com.pieterhuizenga.result.exception
import com.pieterhuizenga.result.isErr
import com.pieterhuizenga.result.isOk
import com.pieterhuizenga.result.map
import com.pieterhuizenga.result.mapErr
import com.pieterhuizenga.result.mapException
import com.pieterhuizenga.result.mapOk
import com.pieterhuizenga.result.ok
import com.pieterhuizenga.result.unwrap
import com.pieterhuizenga.result.unwrapErr
import com.pieterhuizenga.assertUnreachable
import org.junit.Assert.*
import org.junit.Test

public class KrsResultTest {

    @Test
    public fun testResultVariants() {
        val ok = ok<Int, Unit>(5)
        val err = err<Int, String>("error")

        assertTrue(ok.isOk())
        assertFalse(ok.isErr())

        assertFalse(err.isOk())
        assertTrue(err.isErr())

        assertEquals(5, ok.unwrap())
        assertEquals("error", err.unwrapErr())
    }

    @Test
    public fun testResultIsErrSmartCasts() {
        val okResult = ok<Int, Unit>(5)
        if (okResult.isErr()) {
            // result is smart-cast to Err<Int, Unit> and the error property is available
            okResult.error
            // is unreachable code in this case
            assertUnreachable()
            return
        }

        // from here on, result is smart-cast to Ok<Int, Unit> and the value property is available
        assertEquals(5, okResult.value)

        val errResult = err<Int, String>("error")
        if (errResult.isErr()) {
            // result is smart-cast to Err<Int, String> and the error property is available
            assertEquals("error", errResult.error)
            return
        }

        // from here on, result is smart-cast to Ok<Int, String> and the value property is available
        errResult.value

        // is unreachable code in this case
        assertUnreachable()
    }

    @Test
    public fun testResultIsOkSmartCast() {
        val okResult = ok<Int, Unit>(5)
        if (!okResult.isOk()) {
            // result is smart-cast to Err<Int, Unit> and the error property is available
            okResult.error
            // is unreachable code in this case as the result is Ok
            assertUnreachable()
            return
        }

        // from here on, result is smart-cast to Ok<Int, Unit> and the value property is available
        assertEquals(5, okResult.value)

        val errResult = err<Int, String>("error")
        if (errResult.isOk()) {
            // result is smart-cast to Ok<Int, String> and the value property is available
            errResult.value
            // is unreachable code in this case
            assertUnreachable()
            return
        }

        // from here on, result is smart-cast to Err<Int, String> and the error property is available
        assertEquals("error", errResult.error)
    }

    @Test
    public fun testResultUnwrapSmartCast() {
        val okResult = ok<Int, Unit>(5)
        val unwrapped = okResult.unwrap()
        assertEquals(5, unwrapped)

        // now the result is smart-cast to Ok<Int, Unit> and the value property is available
        assertEquals(5, okResult.value)
    }

    @Test
    public fun testResultCollect() {
        val oks = listOf(ok<Int, Unit>(5), ok(6), ok(7))

        val collected = oks.collect()
        assertTrue(collected.isOk())

        val values = collected.unwrap()
        assertEquals(3, values.size)
        assertEquals(5, values[0])
        assertEquals(6, values[1])
        assertEquals(7, values[2])

        val errs = listOf(ok<Int, String>(5), err("error"), ok(7))

        val collectedErr = errs.collect()
        assertTrue(collectedErr.isErr())

        val error = collectedErr.unwrapErr()
        assertEquals("error", error)
    }

    @Test
    public fun testResultsMapOk() {
        val oks = listOf(ok<Int, Unit>(5), ok(6), ok(7))

        val mapped = oks.mapOk { it * 2 }
        assertTrue(mapped.all { it.isOk() })

        val values = mapped.collect().unwrap()
        assertEquals(3, values.size)
        assertEquals(listOf(10, 12, 14), values)

        val withErrs = listOf(ok<Int, String>(5), err("error"), ok(7))

        val mappedWithErrs = withErrs.mapOk { it * 2 }
        assertTrue(mappedWithErrs.any { it.isErr() })
        assertTrue(mappedWithErrs.any { it.isOk() })

        val valuesWithErrs = mappedWithErrs.collect()
        assertTrue(valuesWithErrs.isErr())
    }

    @Test
    public fun testUseCatchingOk() {
        val result = catchingResultScope {
            // ErrorType must be the same for all calls to use()
            val a = ok<Int, String>(5).mapErr { "a failed" }.use()
            // Therefore the following line first maps the error type to string
            val b = ok<Int, List<String>>(6).mapErr { "b failed" }.use()
            val c = ok<Int, Unit>(7).mapErr { "c failed" }.use()

            // Ok type does not need to be the same T as within TryScope<T, E>
            // But error type must be the same as E
            val unrelatedButImportant = ok<String, Unit>("unrelated")
                .mapErr { "unrelated failed" }
                .use()

            println(unrelatedButImportant)

            return@catchingResultScope a + b + c
        }.map { it * 2 }
        assertTrue(result.isOk())
        assertEquals(36, result.unwrap())
    }

    @Test
    public fun testUseCatchingError() {
        val result = catchingResultScope {
            val a = ok<Int, Unit>(5).mapErr { "a failed" }.use()
            val b = err<Int, String>("error").use()
            val c = ok<Int, Unit>(7).mapErr { "c failed" }.use()
            return@catchingResultScope a + b + c
        }.mapException { "Oh no!" }
        assertTrue(result.isErr())
        assertEquals("error", result.unwrapErr())
    }

    @Suppress("ConstantConditionIf")
    @Test
    public fun testUseCatchingException() {
        val result = catchingResultScope {
            val a = ok<Int, Unit>(5).mapErr { "a failed" }.use()
            if (true) {
                throw RuntimeException("error")
            }
            // Unreachable code
            assertUnreachable()
            val b = 6

            val c = ok<Int, Unit>(7).mapErr { "c failed" }.use()
            return@catchingResultScope a + b + c
        }
        assertTrue(result.isErr())

        assert(result.err() is CatchingResultScopeError.Exception<String>)

        val exception = result.err()?.exception()!!

        assertEquals("error", exception.message)
    }

    @Suppress("ConstantConditionIf")
    @Test
    public fun testUseCatchingMapException() {
        val result = catchingResultScope {
            val a = ok<Int, Unit>(5).mapErr { "a failed" }.use()
            if (true) {
                throw RuntimeException("error")
            }
            val b = 6

            val c = ok<Int, Unit>(7).mapErr { "c failed" }.use()
            return@catchingResultScope a + b + c
        }.mapException { "Oh no!" }
        assertTrue(result.isErr())
        assertEquals("Oh no!", result.unwrapErr())
    }

    @Suppress("UNREACHABLE_CODE")
    @Test
    public fun testUseCatchingReturnErr() {
        val result = catchingResultScope {
            val a = ok<Int, Unit>(5).mapErr { "a failed" }.use()
            returnErr("error")
            // Unreachable code
            assertUnreachable()
            val b = 6
            val c = ok<Int, Unit>(7).mapErr { "c failed" }.use()
            return@catchingResultScope a + b + c
        }.mapException { "Oh no!" }
        assertTrue(result.isErr())
        assertEquals("error", result.unwrapErr())
    }

    @Suppress("UNREACHABLE_CODE")
    @Test
    public fun testUseCatchingReturnOk() {
        val result = catchingResultScope {
            val a = ok<Int, Unit>(5).mapErr { "a failed" }.use()
            returnOk(6)
            // Unreachable code
            assertUnreachable()
            val b = 6
            val c = ok<Int, Unit>(7).mapErr { "c failed" }.use()
            return@catchingResultScope a + b + c
        }.mapException { "Oh no!" }
        assertTrue(result.isOk())
        assertEquals(6, result.unwrap())
    }
}





































