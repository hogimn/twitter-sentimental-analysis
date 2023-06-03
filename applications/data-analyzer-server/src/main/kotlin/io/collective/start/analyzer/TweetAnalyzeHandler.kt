package io.collective.start.analyzer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rabbitmq.client.*
import io.collective.start.tweets.TweetInfo
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Handles incoming messages from RabbitMQ to analyze tweets.
 * This implements RPC Pattern of RabbitMQ to directly reply to original publisher.
 *
 * @param sentimentService The SentimentService instance used to perform business logic related to the received messages.
 * @param channel The RabbitMQ channel for communication.
 */
class TweetAnalyzeHandler(private val sentimentService: SentimentService, channel: Channel) : DefaultConsumer(channel) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper = ObjectMapper().registerKotlinModule()

    /**
     * Handles the incoming message from RabbitMQ.
     *
     * @param consumerTag The consumer tag associated with the listener.
     * @param envelope The Envelope object representing the received message.
     * @param properties The BasicProperties of the received message.
     * @param body The byte array of the received message.
     */
    override fun handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: AMQP.BasicProperties,
        body: ByteArray
    ) {
        val replyProps = AMQP.BasicProperties.Builder()
            .correlationId(properties.correlationId)
            .build()

        val bodyToCollector = "data-request".toByteArray()
        val correlationId = UUID.randomUUID().toString()
        val replyQueueName = channel.queueDeclare().queue
        val props = AMQP.BasicProperties.Builder()
            .correlationId(correlationId)
            .replyTo(replyQueueName)
            .build()

        // Publish a "data-request" event to the "analyzer-to-collector-exchange" with the body as "data-request".
        channel.basicPublish(
            "analyzer-to-collector-exchange",
            "auto",
            props,
            bodyToCollector
        )
        logger.info("data-request event published")

        // Consume the reply from the replyQueueName
        channel.basicConsume(replyQueueName, true, object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                propertiesCollector: AMQP.BasicProperties,
                body: ByteArray
            ) {
                // Check if the received correlationId matches the expected correlationId
                if (propertiesCollector.correlationId == correlationId) {
                    // Deserialize the body into a list of TweetInfo objects
                    val tweetInfos = mapper.readValue<List<TweetInfo>>(body)
                    tweetInfos.forEach {
                        logger.info("received event: $it")
                    }
                    // Perform sentiment analysis on the first 3 tweetInfo objects
                    // Why 3? Free-trial usage of gpt 3.5 turbo API cannot exceed 3 questions per 1 minute
                    val tweetInfoWithSentiments = sentimentService.analyze(tweetInfos.subList(0, 3))
//                    val tweetInfoWithSentiments = sentimentService.analyze(tweetInfos)

                    // Serialize the tweetInfoWithSentiments into a byte array
                    val bodyToWebApp = mapper.writeValueAsString(tweetInfoWithSentiments).toByteArray()

                    // Publish the response to the replyTo queue specified in the properties
                    channel.basicPublish("", properties.replyTo, replyProps, bodyToWebApp)
                    logger.info("Reply to web app is published. The size of the data sent is ${tweetInfoWithSentiments.size}")
                }
            }
        })
    }
}
