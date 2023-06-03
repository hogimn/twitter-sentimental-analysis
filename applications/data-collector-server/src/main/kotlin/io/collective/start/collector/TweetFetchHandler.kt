package io.collective.start.collector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rabbitmq.client.*
import io.collective.start.tweets.TweetService
import org.slf4j.LoggerFactory

/**
 * Handles incoming messages from RabbitMQ to provide collected data
 * This implements RPC Pattern of RabbitMQ to directly reply to original publisher.
 *
 * @param tweetService The [TweetService] instance used to perform business logic related to the received messages.
 * @param channel The RabbitMQ channel associated with the consumer.
 */
class TweetFetchHandler(private val tweetService: TweetService, channel: Channel) : DefaultConsumer(channel) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper = ObjectMapper().registerKotlinModule()

    /**
     * Handles the incoming message from RabbitMQ.
     *
     * @param consumerTag The consumer tag associated with the listener.
     * @param envelope The envelope containing metadata of the received message.
     * @param properties The properties of the received message.
     * @param body The body of the received message as a byte array.
     */
    override fun handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: AMQP.BasicProperties,
        body: ByteArray
    ) {
        // Set the correlation ID of the reply properties.
        val replyProps = AMQP.BasicProperties.Builder()
            .correlationId(properties.correlationId)
            .build()

        logger.info("TweetFetchHandler received event.")
        // Retrieve tweet information using the TweetService.
        val tweetInfos = tweetService.findAll()
        // Serialize the tweetInfos into a byte array
        val bodyToAnalyzer = mapper.writeValueAsString(tweetInfos).toByteArray()
        // Publish the converted tweet information to the analyzer.
        channel.basicPublish("", properties.replyTo, replyProps, bodyToAnalyzer)
        logger.info("TweetFetchHandler published data to analyzer.")
    }
}
