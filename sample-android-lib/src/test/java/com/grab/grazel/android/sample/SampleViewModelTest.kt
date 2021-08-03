package com.grab.grazel.android.sample

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleViewModelTest {

    @Test
    fun testParcelable() {
        val viewModel = SampleViewModel()
        assertEquals("tom", viewModel.user.firstName)
    }
}