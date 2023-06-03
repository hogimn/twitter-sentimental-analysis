package io.collective.start.collector

/**
 * Represents a task for collecting data from an endpoint.
 *
 * @param endpoint The URL endpoint to fetch the data from.
 * @param accept The value of the "Accept" header for the HTTP request. The default value is "application/xml".
 */
data class TweetCollectorEndpointTask(
    val endpoint: String,
    val accept: String = "application/xml"
)
