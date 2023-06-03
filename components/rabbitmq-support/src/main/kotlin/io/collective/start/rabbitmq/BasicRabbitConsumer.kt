package io.collective.start.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * RabbitMQ consumer for consuming messages from a queue.
 *
 * @property queue The name of the queue to listen to.
 * @property consumerFactory The factory function for creating a consumer for message handling.
 * @property autoAck Specifies if automatic message acknowledgment is enabled (default: true).
 */
class BasicRabbitConsumer(
    private val queue: String,
    private val consumerFactory: (Channel) -> DefaultConsumer,
    private val autoAck: Boolean = true,
) {
    private var running = true
    private val connectionFactory = ConnectionFactory().apply { useBlockingIo() }

    /**
     * Starts the listener in a separate thread.
     * Creates a connection, channel, and consumes messages from the queue.
     */
    fun start() {
        thread {
            connectionFactory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    val consumer = consumerFactory(channel)
                    while (running) {
                        try {
                            // Start consuming messages from the queue with the specified callbacks
                            channel.basicConsume(queue, autoAck, consumer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        // Wait for a short period before consuming the next message
                        sleep(100)
                    }
                }
            }
        }
    }

    /**
     * Stops the listener.
     * Sets the running flag to false and waits for a short period.
     * Note: The wait period is added for testing purposes.
     */
    fun stop() {
        running = false
        sleep(100) // Wait for a cycle, needed for testing
    }
}
