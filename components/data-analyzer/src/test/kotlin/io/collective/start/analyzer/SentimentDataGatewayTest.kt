package io.collective.start.analyzer

import io.collective.start.testsupport.testDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class SentimentDataGatewayIntegrationTest {
    private lateinit var gateway: SentimentDataGateway

    @Before
    fun setUp() {
        // Create the gateway instance
        gateway = SentimentDataGateway(testDataSource())
        gateway.clear()
    }

    @After
    fun tearDown() {
        gateway.clear()
    }

    @Test
    fun testCreateAndFindByID() {
        // Create a new sentiment record
        val id = "123"
        val sentiment = "positive"
        val createdRecord = gateway.create(id, sentiment)

        assertNotNull(createdRecord)
        assertEquals(id, createdRecord!!.id)
        assertEquals(sentiment, createdRecord.sentiment)

        // Find the record by ID
        val foundRecord = gateway.findBy(id)

        assertNotNull(foundRecord)
        assertEquals(id, foundRecord!!.id)
        assertEquals(sentiment, foundRecord.sentiment)
    }

    @Test
    fun testFindAll() {
        // Create some sentiment records
        val record1 = SentimentRecord("1", "positive")
        val record2 = SentimentRecord("2", "negative")
        val record3 = SentimentRecord("3", "neutral")

        gateway.create(record1.id, record1.sentiment)
        gateway.create(record2.id, record2.sentiment)
        gateway.create(record3.id, record3.sentiment)

        // Retrieve all sentiment records
        val allRecords = gateway.findAll()

        assertEquals(3, allRecords.size)
        assertTrue(allRecords.contains(record1))
        assertTrue(allRecords.contains(record2))
        assertTrue(allRecords.contains(record3))
    }

    @Test
    fun testUpdate() {
        // Create a new sentiment record
        val id = "123"
        val initialSentiment = "positive"
        gateway.create(id, initialSentiment)

        // Find the record by ID
        val recordToUpdate = gateway.findBy(id)
        assertNotNull(recordToUpdate)

        // Update the sentiment
        val updatedSentiment = SentimentRecord(recordToUpdate!!.id, "negative")
        gateway.update(updatedSentiment)

        // Find the record again
        val updatedRecord = gateway.findBy(id)
        assertNotNull(updatedRecord)
        assertEquals(updatedSentiment.sentiment, updatedRecord!!.sentiment)
    }
}
