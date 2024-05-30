package com.pieterhuizenga.result

public fun <T, E> Iterable<KrsResult<T, E>>.collect(): KrsResult<List<T>, E> {
    return KrsResult.Ok(buildList {
        for (result in this@collect) {
            when (result) {
                is KrsResult.Ok -> add(result.value)
                is KrsResult.Err -> return KrsResult.Err(result.error)
            }
        }
    })
}

public class ResultMapOkIterator<T, E, R>(
    private val iterator: Iterator<KrsResult<T, E>>,
    private val transform: (T) -> R
) : Iterator<KrsResult<R, E>> {
    override fun hasNext(): Boolean = iterator.hasNext()

    override fun next(): KrsResult<R, E> {
        return when (val next = iterator.next()) {
            is KrsResult.Ok -> KrsResult.Ok(transform(next.value))
            is KrsResult.Err -> KrsResult.Err(next.error)
        }
    }
}

public fun <T, E, R> Iterator<KrsResult<T, E>>.mapOk(transform: (T) -> R): Iterator<KrsResult<R, E>> {
    return ResultMapOkIterator(this, transform)
}

public class ResultMapErrIterator<T, E, F>(
    private val iterator: Iterator<KrsResult<T, E>>,
    private val transform: (E) -> F
) : Iterator<KrsResult<T, F>> {
    override fun hasNext(): Boolean = iterator.hasNext()

    override fun next(): KrsResult<T, F> {
        return when (val next = iterator.next()) {
            is KrsResult.Ok -> KrsResult.Ok(next.value)
            is KrsResult.Err -> KrsResult.Err(transform(next.error))
        }
    }
}

public fun <T, E, F> Iterator<KrsResult<T, E>>.mapErr(transform: (E) -> F): Iterator<KrsResult<T, F>> {
    return ResultMapErrIterator(this, transform)
}

public fun <T, E, R> Iterable<KrsResult<T, E>>.mapOk(transform: (T) -> R): Iterable<KrsResult<R, E>> {
    return map { result ->
        when (result) {
            is KrsResult.Ok -> KrsResult.Ok(transform(result.value))
            is KrsResult.Err -> KrsResult.Err(result.error)
        }
    }
}

public fun <T, E, F> Iterable<KrsResult<T, E>>.mapErr(transform: (E) -> F): List<KrsResult<T, F>> {
    return map { result ->
        when (result) {
            is KrsResult.Ok -> KrsResult.Ok(result.value)
            is KrsResult.Err -> KrsResult.Err(transform(result.error))
        }
    }
}