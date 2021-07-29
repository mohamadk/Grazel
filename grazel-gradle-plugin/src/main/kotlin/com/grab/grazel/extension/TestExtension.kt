package com.grab.grazel.extension

import groovy.lang.Closure


private const val DEFAULT_TEST_SIZE = "medium"
private const val DEFAULT_ROBOLECTRIC_VERSION = "4.4"

data class RobolectricExtension(
    var version: String = DEFAULT_ROBOLECTRIC_VERSION
)

data class AndroidTestExtension (
    var robolectric: RobolectricExtension = RobolectricExtension()
) {

    fun robolectric(block: RobolectricExtension.() -> Unit) {
        robolectric.block()
    }

    fun robolectric(closure: Closure<*>) {
        closure.delegate = robolectric
        closure.call()
    }
}

data class TestExtension(
    var defaultTestSize: String = DEFAULT_TEST_SIZE,
    var enableTestMigration: Boolean = false,
    var androidTest: AndroidTestExtension = AndroidTestExtension()
) {
    fun androidTest(block: AndroidTestExtension.() -> Unit) {
        androidTest.block()
    }

    fun androidTest(closure: Closure<*>) {
        closure.delegate = androidTest
        closure.call()
    }
}

