package test.collective.start

import io.collective.start.module
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AppTest {

    @Test
    fun testEmptyHome() = testApp {
        handleRequest(HttpMethod.Get, "/health-check").apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains("hi!"))
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ module() }) { callback() }
    }
}
