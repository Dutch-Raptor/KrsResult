package com.pieterhuizenga
public fun assertUnreachable() {
    throw AssertionError("This code should be unreachable")
}

public fun assertUnreachableInferred(): Nothing = throw AssertionError("This code should be unreachable")