package com.grab.grazel.android.sample

import org.junit.Assert.assertEquals
import org.junit.Test

class FoolTest {

    @Test
    fun method1() {
        println("Class 1 test 1")
        assertEquals(4, 2 + 2)
    }

    @Test
    fun method2() {
        println("Class 1 test 2")
        assertEquals(6, 4 + 2)
    }
}