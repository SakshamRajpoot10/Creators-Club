package com.creatorsclub.core.common

import com.creatorsclub.core.common.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun successResult_holdsDataCorrectly() {
        val result: Result<String> = Result.Success("Kreavilo Studio")
        assertTrue(result is Result.Success)
        assertEquals("Kreavilo Studio", (result as Result.Success).data)
    }

    @Test
    fun errorResult_holdsExceptionAndMessage() {
        val exception = IllegalStateException("Disk full")
        val result: Result<Nothing> = Result.Error(exception = exception, message = "Failed to save")
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals("Failed to save", error.message)
        assertEquals(exception, error.exception)
    }

    @Test
    fun loadingResult_isSingletonObject() {
        val result: Result<Int> = Result.Loading
        assertTrue(result is Result.Loading)
    }
}
