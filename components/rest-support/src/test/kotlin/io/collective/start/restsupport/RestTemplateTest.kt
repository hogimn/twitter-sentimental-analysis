package io.collective.start.restsupport

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTemplateTest {

    @Test
    fun testGetRequest() {
        val restTemplate = RestTemplate()
        val endpoint = "https://www.google.com"
        val accept = "text/html"
        val response = restTemplate.get(endpoint, accept)

        // Perform assertions on the response
        assertNotNull(response)
        assertTrue("Expected response contains 'Google'", response.contains("Google"))
    }
}