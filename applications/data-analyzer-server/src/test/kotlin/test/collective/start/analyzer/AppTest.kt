package test.collective.start.analyzer

import io.collective.start.analyzer.module
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AppTest {
    private val testJdbcUrl = ""
    private val testDbUsername = ""
    private val testDbPassword = ""

    @Test
    fun testEmptyHome() = testApp {
        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains("hi!"))
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ module(testJdbcUrl, testDbUsername, testDbPassword) }) { callback() }
    }
}