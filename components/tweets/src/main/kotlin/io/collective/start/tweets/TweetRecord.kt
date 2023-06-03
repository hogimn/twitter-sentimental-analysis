package io.collective.start.tweets

import java.sql.Timestamp

/**
 * Data class representing a tweet record.
 *
 * @property id The ID of the tweet, which is the hash of other properties.
 * @property title The title or headline of the tweet.
 * @property description The description or content of the tweet.
 * @property pubDate The publication date of the tweet.
 * @property link The link or URL associated with the tweet.
 * @property author The author or creator of the tweet.
 * @property timestamp The timestamp indicating when the tweet record was created.
 */
data class TweetRecord(
    val id: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val author: String,
    val timestamp: Timestamp
)
