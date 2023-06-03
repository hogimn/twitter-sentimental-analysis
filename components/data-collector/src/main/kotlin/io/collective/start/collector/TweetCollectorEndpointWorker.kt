package io.collective.start.collector

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.collective.start.restsupport.RestTemplate
import io.collective.start.rss.RSS
import io.collective.start.tweets.TweetInfo
import io.collective.start.tweets.TweetService
import io.collective.start.workflow.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Implementation of the Worker interface for executing data collection tasks from endpoints.
 *
 * @param name The name of the TweetCollectorEndpointWorker.
 * @param restTemplate The RestTemplate used for making HTTP requests.
 * @param tweetService The TweetService used for creating tweet entries in the database.
 */
class TweetCollectorEndpointWorker(
    override val name: String = "data-collector",
    private val restTemplate: RestTemplate,
    private val tweetService: TweetService
) : Worker<TweetCollectorEndpointTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Executes the given endpoint task.
     *
     * @param task The EndpointTask to execute.
     */
    override fun execute(task: TweetCollectorEndpointTask) {
        val block: suspend CoroutineScope.() -> Unit = {
            // Log message indicating the start of data collection
            logger.info("Starting data collection.")

            // todo - data collection happens here
            // Make an HTTP GET request to the specified endpoint and retrieve the response
            val response = restTemplate.get(task.endpoint, task.accept)
            // Parse the response as an RSS object using XmlMapper
            val rss = XmlMapper().readValue(response, RSS::class.java)
            // Log the string representation of the item list
            logger.info(rss.channel.item.toString())
            // Iterate over each item in the RSS channel
            rss.channel.item.forEach {
                // Log message indicating the received item
                logger.info("Received item: $it")
                // Create a tweet entry in the database using the tweetService
                val result = tweetService.create(
                    TweetInfo("", it.title, it.description, it.pubDate, it.link, it.author)
                )
                // Log the result of the tweet creation or indicate if it already exists in the database
                logger.info(result?.toString() ?: "Already exists in DB")
            }

            // Log message indicating the completion of data collection
            logger.info("Completed data collection.")
        }
        // Run the block of code in a blocking manner
        runBlocking(block = block)
    }
}
