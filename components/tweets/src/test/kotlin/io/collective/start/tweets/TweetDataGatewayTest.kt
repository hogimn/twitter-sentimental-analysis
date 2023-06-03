package io.collective.start.tweets

import io.collective.start.testsupport.testDataSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class TweetDataGatewayTest {

    private lateinit var tweetDataGateway: TweetDataGateway

    @Before
    fun setUp() {
        tweetDataGateway = TweetDataGateway(testDataSource())
        tweetDataGateway.clear()
    }

    @After
    fun tearDown() {
        tweetDataGateway.clear()
    }

    @Test
    fun testCreateAndFindByID() {
        // Create a tweet record
        val title = "Test Tweet"
        val description = "This is a test tweet"
        val pubDate = "2023-06-05"
        val link = "https://example.com/tweet123"
        val author = "Test User"
        val createdTweet = tweetDataGateway.create(title, description, pubDate, link, author)

        // Verify that the tweet was created successfully
        assertNotNull(createdTweet)
        assertNotNull(createdTweet.id)
        assertEquals(title, createdTweet.title)
        assertEquals(description, createdTweet.description)
        assertEquals(pubDate, createdTweet.pubDate)
        assertEquals(link, createdTweet.link)
        assertEquals(author, createdTweet.author)

        // Find the tweet by ID
        val foundTweet = tweetDataGateway.findBy(createdTweet.id ?: "")

        // Verify that the found tweet matches the created tweet
        assertNotNull(foundTweet)
        assertEquals(createdTweet.id, foundTweet.id)
        assertEquals(createdTweet.title, foundTweet.title)
        assertEquals(createdTweet.description, foundTweet.description)
        assertEquals(createdTweet.pubDate, foundTweet.pubDate)
        assertEquals(createdTweet.link, foundTweet.link)
        assertEquals(createdTweet.author, foundTweet.author)
    }

    @Test
    fun testFindAll() {
        val allTweets = tweetDataGateway.findAll()
        assertEquals(0, allTweets.size)
    }

    @Test
    fun testUpdate() {
        // Create a tweet record
        val title = "Test Tweet"
        val description = "This is a test tweet"
        val pubDate = "2023-06-05"
        val link = "https://example.com/tweet123"
        val author = "Test User"
        val createdTweet = tweetDataGateway.create(title, description, pubDate, link, author)


        assertNotNull(createdTweet)
        val updatedTweet = tweetDataGateway.update(
            TweetRecord(
                createdTweet.id,
                createdTweet.title,
                "Updated",
                createdTweet.pubDate,
                createdTweet.link,
                createdTweet.author,
                createdTweet.timestamp
            )
        )

        assertEquals(updatedTweet.id, createdTweet.id)
        assertEquals(updatedTweet.title, createdTweet.title)
        assertEquals(updatedTweet.description, "Updated")
        assertEquals(updatedTweet.pubDate, createdTweet.pubDate)
        assertEquals(updatedTweet.link, createdTweet.link)
        assertEquals(updatedTweet.author, createdTweet.author)
    }
}