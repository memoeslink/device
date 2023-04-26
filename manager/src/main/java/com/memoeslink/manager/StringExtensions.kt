@file:JvmName("StringExtensions")

package com.memoeslink.manager

import java.math.BigInteger
import java.security.MessageDigest

const val DEFAULT_VALUE = "?"

fun String.toMd5(): String =
    BigInteger(
        1, MessageDigest.getInstance("MD5").digest(this.toByteArray())
    ).toString(16).padStart(32, '0')

fun String.toSsidFormat(): String =
    when {
        this.isBlank() || this == "\"\"" -> DEFAULT_VALUE
        else -> this.removeSurrounding("\"")
    }
