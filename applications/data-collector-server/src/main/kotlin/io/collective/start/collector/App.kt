package io.collective.start.collector

import io.collective.start.database.createDatasource
import io.collective.start.rabbitmq.BasicRabbitConfiguration
import io.collective.start.rabbitmq.BasicRabbitConsumer
import io.collective.start.restsupport.RestTemplate
import io.collective.start.tweets.TweetDataGateway
import io.collective.start.tweets.TweetService
import io.collective.start.workflow.WorkScheduler
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.*

/**
 * Main module of the application.
 *
 * @param jdbcUrl The JDBC URL for the database connection.
 * @param username The username for the database connection.
 * @param password The password for the database connection.
 */
fun Application.module(jdbcUrl: String, username: String, password: String) {
    // Install default headers feature
    install(DefaultHeaders)
    // Install call logging feature
    install(CallLogging)
    // Install routing feature
    install(Routing) {
        // Define a GET route for the root path "/"
        get("/") {
            call.respondText("hi!", ContentType.Text.Html)
        }
    }

    val tweetService = TweetService(TweetDataGateway(createDatasource(jdbcUrl, username, password)))

    // Create a new instance of the WorkScheduler
    val scheduler = WorkScheduler(
        TweetCollectorEndpointWorkFinder(
            TweetCollectorEndpointDataGateway(
                createDatasource(
                    jdbcUrl,
                    username,
                    password
                )
            )
        ),
        mutableListOf(
            TweetCollectorEndpointWorker(
                restTemplate = RestTemplate(),
                tweetService = tweetService
            )
        ),
        30
    )
    // Start the scheduler
    scheduler.start()

    // Set up the RabbitMQ configuration for the exchange, queue, and routing key.
    BasicRabbitConfiguration(
        exchange = "analyzer-to-collector-exchange",
        queue = "analyzer-to-collector",
        routingKey = "auto"
    ).setUp()

    // Start the RabbitMQ consumer with the specified queue, consumer factory, and auto-acknowledgement.
    BasicRabbitConsumer(
        queue = "analyzer-to-collector",
        consumerFactory = { channel -> TweetFetchHandler(tweetService, channel) },
        autoAck = true,
    ).start()
}

/**
 * Start of the program
 */
fun main() {
    // Set the default time zone to UTC
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // Get the port number from the environment variable or use the default value 8888
    val port = System.getenv("PORT")?.toInt() ?: 8890

    // Get the JDBC URL, username, and password from the environment variables
    val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
    val username = System.getenv("JDBC_DATABASE_USERNAME")
    val password = System.getenv("JDBC_DATABASE_PASSWORD")

    // Start an embedded server using Netty as the server engine
    // The server will listen on the specified port
    // It will watch for changes in the "data-collector-server" directory
    // When any file within those directories is modified, added, or removed, the server automatically restarts.
    // The server will use the module defined in the `module()` function, passing the JDBC URL, username, and password
    embeddedServer(
        Netty,
        port,
        watchPaths = listOf("data-collector-server"),
        module = { module(jdbcUrl, username, password) }
    ).start()
}
