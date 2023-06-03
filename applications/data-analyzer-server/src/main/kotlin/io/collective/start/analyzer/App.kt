package io.collective.start.analyzer

import com.theokanning.openai.service.OpenAiService
import io.collective.start.database.createDatasource
import io.collective.start.rabbitmq.BasicRabbitConfiguration
import io.collective.start.rabbitmq.BasicRabbitConsumer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.*

/**
 * Configures and sets up the server module.
 *
 * @param jdbcUrl The JDBC URL for the database connection.
 * @param username The username for the database connection.
 * @param password The password for the database connection.
 */
fun Application.module(jdbcUrl: String, username: String, password: String) {
    // Create a data source using the JDBC URL, username, and password
    val dataSource = createDatasource(jdbcUrl, username, password)

    // Create an instance of the SentimentService, providing it with a SentimentDataGateway and OpenAiService
    val sentimentService = SentimentService(
        SentimentDataGateway(dataSource),
        OpenAiService(BuildConfig.API_KEY)
    )

    // Install the DefaultHeaders feature, which adds default headers to each response
    install(DefaultHeaders)

    // Install the CallLogging feature, which logs each call made to the server
    install(CallLogging)

    // Install the Routing feature to define routes and handlers for the server
    install(Routing) {
        // Define a GET route for the root path ("/")
        get("/") {
            // Respond with a simple text response of "hi!"
            call.respondText("hi!", ContentType.Text.Html)
        }
    }

    // Set up basic RabbitMQ configuration for communication with the analyzer
    BasicRabbitConfiguration(
        exchange = "webapp-to-analyzer-exchange",
        queue = "webapp-to-analyzer",
        routingKey = "auto"
    ).setUp()

    // Start a basic RabbitMQ consumer that listens to the "webapp-to-analyzer" queue
    // It uses the TweetAnalyzeHandler to process the incoming messages
    // The consumer automatically acknowledges the messages after processing
    BasicRabbitConsumer(
        queue = "webapp-to-analyzer",
        consumerFactory = { channel -> TweetAnalyzeHandler(sentimentService, channel) },
        autoAck = true,
    ).start()
}

/**
 * Start of the program
 */
fun main() {
    // Set the default time zone to UTC
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // Get the port number from the environment variable "PORT" and convert it to an integer
    // If the "PORT" variable is not set, default to port 8889
    val port = System.getenv("PORT")?.toInt() ?: 8889

    // Get the JDBC URL, username, and password from the environment variables
    val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
    val username = System.getenv("JDBC_DATABASE_USERNAME")
    val password = System.getenv("JDBC_DATABASE_PASSWORD")

    // Start an embedded server using Netty as the server engine
    // The server will listen on the specified port
    // It will watch for changes in the "data-analyzer-server" directory
    // When any file within those directories is modified, added, or removed, the server automatically restarts.
    // The server will use the module defined in the `module()` function, passing the JDBC URL, username, and password
    embeddedServer(
        Netty,
        port,
        watchPaths = listOf("data-analyzer-server"),
        module = { module(jdbcUrl, username, password) }
    ).start()
}
