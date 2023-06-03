package io.collective.start.collector

/**
 * Represents a record of a tweet collection endpoint.
 *
 * @param id The unique identifier of the endpoint.
 * @param link The URL of the endpoint.
 */
data class TweetCollectorEndpointRecord(
    val id: String,
    val link: String
)
