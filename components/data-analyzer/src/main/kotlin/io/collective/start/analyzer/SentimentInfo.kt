package io.collective.start.analyzer

/**
 * Data class representing sentiment information for a tweet.
 *
 * @property id The ID of the tweet.
 * @property sentiment The sentiment associated with the tweet.
 */
data class SentimentInfo(
    val id: String,
    val sentiment: String
)
