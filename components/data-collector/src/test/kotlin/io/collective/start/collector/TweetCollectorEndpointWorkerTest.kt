package io.collective.start.collector

import io.collective.start.restsupport.RestTemplate
import io.collective.start.testsupport.testDataSource
import io.collective.start.tweets.TweetDataGateway
import io.collective.start.tweets.TweetService
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class TweetCollectorEndpointWorkerTest {
    private val dataSource = testDataSource()

    @Test
    fun finder() {
        val xml = String(javaClass.getResourceAsStream("/twitter.xml").readAllBytes())
        val mockRestTemplate = mock(RestTemplate::class.java)
        `when`(mockRestTemplate.get("https://rsshub.app/twitter/user/twitter", "application/xml")).thenReturn(xml)
        val service = TweetService(TweetDataGateway(dataSource))

        val worker = TweetCollectorEndpointWorker(restTemplate = mockRestTemplate, tweetService = service)
        worker.execute(TweetCollectorEndpointTask("https://rsshub.app/twitter/user/twitter"))

        val tweets = service.findAll()
        assertEquals(20, service.findAll().size)
        tweets.forEach {
            assertEquals(it, service.findBy(it.id))
        }
    }
}