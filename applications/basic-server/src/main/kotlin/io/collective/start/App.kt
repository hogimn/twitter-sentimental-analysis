package io.collective.start

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import freemarker.cache.ClassTemplateLoader
import io.collective.start.analyzer.TweetInfoWithSentiment
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Configures and sets up the server module.
 */
fun Application.module() {
    // Create a connection factory and enable blocking I/O
    val connectionFactory = ConnectionFactory().apply { useBlockingIo() }

    // Create an instance of ObjectMapper to handle JSON serialization/deserialization
    val mapper = ObjectMapper().registerKotlinModule()

    // Create a logger instance for logging
    val logger = LoggerFactory.getLogger(this.javaClass)

    // Install the DefaultHeaders feature, which adds default headers to each response
    install(DefaultHeaders)

    // Install the CallLogging feature, which logs each call made to the server
    install(CallLogging)

    // Install the FreeMarker feature for rendering templates
    install(FreeMarker) {
        // Set the template loader to load templates from the "templates" directory
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    // Install the Routing feature to define routes and handlers for the server
    install(Routing) {
        // Define a GET route for the health check endpoint ("/health-check")
        get("/health-check") {
            // Respond with a simple text response of "hi!"
            call.respondText("hi!", ContentType.Text.Html)
        }

        // Define a GET route for the root path ("/")
        get("/") {
            // Create a new connection and channel within a connection
            connectionFactory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    // Create the body, correlation ID, reply queue name, and properties for the RabbitMQ message
                    val body = "analyze-request".toByteArray()
                    val correlationId = UUID.randomUUID().toString()
                    val replyQueueName = channel.queueDeclare().queue
                    val props = AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build()

                    // Publish the message to the "webapp-to-analyzer-exchange" exchange with routing key "auto"
                    channel.basicPublish(
                        "webapp-to-analyzer-exchange",
                        "auto",
                        props,
                        body
                    )

                    // Set up a CompletableFuture to receive the response from RabbitMQ
                    val messageReceived = CompletableFuture<List<TweetInfoWithSentiment>>()

                    // Set up a consumer to handle the RabbitMQ message
                    channel.basicConsume(replyQueueName, true, object : DefaultConsumer(channel) {
                        override fun handleDelivery(
                            consumerTag: String,
                            envelope: Envelope,
                            properties: AMQP.BasicProperties,
                            body: ByteArray
                        ) {
                            // Check if the received message has the expected correlation ID
                            if (properties.correlationId == correlationId) {
                                // Deserialize the message body into a List<TweetInfoWithSentiment>
                                val tweetInfoWithSentiments = body.let {
                                    mapper.readValue<List<TweetInfoWithSentiment>>(
                                        body
                                    )
                                }
                                // Complete the CompletableFuture with the received tweetInfoWithSentiments
                                messageReceived.complete(tweetInfoWithSentiments)
                            }
                        }
                    })

                    runBlocking {
                        // Wait for the messageReceived CompletableFuture to complete
                        messageReceived.await()

                        // Get the tweetInfoWithSentiments from the CompletableFuture
                        val tweetInfoWithSentiments = messageReceived.get()

                        // Log the size and contents of tweetInfoWithSentiments
                        logger.info("the size of received tweetInfoWithSentiments is ${tweetInfoWithSentiments.size}")
                        tweetInfoWithSentiments?.forEach {
                            logger.info(it.toString())
                        }

                        // Respond with the "index.ftl" template, passing the headers and tweetInfoWithSentiments
                        call.respond(
                            FreeMarkerContent(
                                "index.ftl",
                                mapOf(
                                    "headers" to headers(),
                                    "tweetInfoWithSentiments" to tweetInfoWithSentiments
                                )
                            )
                        )
                    }
                }
            }
        }

        // Serve static resources from the "images" directory
        static("images") { resources("images") }
        // Serve static resources from the "style" directory
        static("style") { resources("style") }
    }
}

/**
 * Extracts the headers from the current HTTP request and returns them as a mutable map.
 *
 * @return The headers extracted from the request as a mutable map, where each header name is a key and the header values are joined into a string.
 */
private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    // Create an empty mutable map to store the headers
    val headers = mutableMapOf<String, String>()

    // Iterate over the headers in the request and add them to the map
    call.request.headers.entries().forEach { entry ->
        // Join the header values into a string and assign it to the corresponding header name
        headers[entry.key] = entry.value.joinToString()
    }

    // Return the headers map
    return headers
}


/**
 * Start of the program
 */
fun main() {
    // Set the default time zone to UTC
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // Get the port number from the environment variable "PORT" and convert it to an integer
    val port = System.getenv("PORT")?.toInt() ?: 8888

    // Start an embedded server using Netty as the server engine
    // The server will listen on the specified port
    // It will watch for changes in the "basic-server" directory
    // When any file within those directories is modified, added, or removed, the server automatically restarts.
    // The server will use the module defined in the `module()` function
    embeddedServer(Netty, port, watchPaths = listOf("basic-server"), module = { module() }).start()
}
