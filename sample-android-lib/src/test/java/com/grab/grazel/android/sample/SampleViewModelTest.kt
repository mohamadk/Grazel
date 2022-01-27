package com.grab.grazel.android.sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class SampleViewModelTest {

    @Test
    fun testParcelable() {
        val viewModel = SampleViewModel()
        assertEquals("tom", viewModel.user.firstName)
    }

    @Test
    fun testLoadResources() {
        val testJson = read("test.json")
        assertTrue(testJson.isNotBlank())
        val mockResponse = read("mock-response.json")
        assertTrue(mockResponse.isNotBlank())
    }

    private fun read(resourceName: String): String {
        val sb = StringBuilder()
        try {
            val reader = BufferedReader(
                InputStreamReader(
                    this::class.java.classLoader!!.getResourceAsStream(resourceName), "UTF-8"
                )
            )
            var strLine = reader.readLine()
            while (strLine != null) {
                sb.append(strLine)
                strLine = reader.readLine()
            }
            reader.close()
        } catch (ex: IOException) {
            throw IllegalStateException(ex)
        }
        return sb.toString()
    }
}
