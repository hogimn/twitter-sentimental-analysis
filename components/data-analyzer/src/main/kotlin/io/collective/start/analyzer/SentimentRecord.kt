package io.collective.start.analyzer

/**
 * Data class representing a sentiment record.
 *
 * @property id The ID of the tweet, which is the hash of other properties.
 * @property sentiment The sentiment associated with the tweet.
 */
data class SentimentRecord(
    val id: String,
    val sentiment: String
)

