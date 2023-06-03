package test.milk.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.MessageProperties
import io.collective.start.rabbitmq.BasicRabbitConfiguration
import io.collective.start.rabbitmq.BasicRabbitConsumer
import io.collective.start.rabbitmq.RabbitTestSupport
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class BasicRabbitConsumerTest {
    private val testSupport = RabbitTestSupport()
    private val factory = ConnectionFactory().apply { useNio() }

    @Before
    fun before() {
        BasicRabbitConfiguration("test-exchange", "test-queue", "test-key").setUp()
        testSupport.purge("test-queue")
    }

    @Test
    fun listener() {
        val single = AtomicInteger()

        val listener =
            BasicRabbitConsumer(
                "test-queue",
                { channel -> TestConsumer("single.1", { single.incrementAndGet() }, channel) },
            )
        listener.start()

        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                val body = "aBody".toByteArray()
                channel.basicPublish("test-exchange", "test-key", MessageProperties.BASIC, body)
            }
        }

        await().untilAtomic(single, equalTo(1))
        listener.stop()
    }

    @Test
    fun listenerMany() {
        val completed = AtomicInteger()

        val listeners = (1..4).map {
            BasicRabbitConsumer(
                "test-queue",
                { channel -> TestConsumer("many.$it", { completed.incrementAndGet() }, channel) },
            )
        }
        listeners.forEach { it.start() }

        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                (1..50).map {
                    val body = "aBody.$it".toByteArray()
                    channel.basicPublish("test-exchange", "test-key", MessageProperties.PERSISTENT_BASIC, body)
                }
            }
        }

        await().untilAtomic(completed, equalTo(50))
        listeners.forEach { it.stop() }
    }
}
