package test.collective.start.collector

import io.collective.start.collector.module
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AppTest {
    private val jdbcUrl = ""
    private val username = ""
    private val password = ""

    @Test
    fun testEmptyHome() = testApp {
        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains("hi!"))
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ module(jdbcUrl, username, password) }) { callback() }
    }
}