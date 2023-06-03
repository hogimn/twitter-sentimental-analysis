package io.collective.start.analyzer

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import io.collective.start.tweets.TweetInfo
import org.slf4j.LoggerFactory

/**
 * Service class for managing tweet-related operations.
 *
 * @property sentimentDataGateway The data gateway for accessing tweet data.
 * @property openAiService The service for interacting with the OpenAI API.
 */
class SentimentService(
    private val sentimentDataGateway: SentimentDataGateway,
    private val openAiService: OpenAiService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Retrieves a sentiment information of a tweet by its ID.
     *
     * @param id The ID of the tweet.
     * @return The sentiment information of the tweet with the specified ID, or null if not found.
     */
    fun findSentimentBy(id: String): SentimentInfo? {
        val record = sentimentDataGateway.findBy(id) ?: return null
        return SentimentInfo(record.id, record.sentiment)
    }


    /**
     * Updates the sentiment information of a tweet.
     *
     * @param sentimentInfo The updated sentiment information of the tweet.
     * @return The updated sentiment information of the tweet, or null if update fails.
     */
    fun update(sentimentInfo: SentimentInfo): SentimentInfo? {
        val record = sentimentDataGateway.findBy(sentimentInfo.id) ?: return null
        sentimentDataGateway.update(record)
        return findSentimentBy(record.id)
    }

    /**
     * Creates a new sentiment information for a tweet.
     *
     * @param sentimentInfo The sentiment information of the new tweet.
     * @return The created sentiment information of the tweet, or null if creation fails.
     */
    fun create(sentimentInfo: SentimentInfo): SentimentInfo? {
        val record = sentimentDataGateway.create(sentimentInfo.id, sentimentInfo.sentiment) ?: return null
        return findSentimentBy(record.id)
    }

    /**
     * Analyzes the sentiment of a list of tweets.
     *
     * @param tweetInfos The list of tweets to analyze.
     * @return A list of [TweetInfoWithSentiment] objects representing the analyzed tweets with sentiment information.
     */
    fun analyze(tweetInfos: List<TweetInfo>): List<TweetInfoWithSentiment> {
        // Create an empty list to store the analyzed tweets with sentiment information
        val tweetInfoWithSentiments = mutableListOf<TweetInfoWithSentiment>()
        // Create an empty list to store the tweets that have not been analyzed yet
        val notYetAnalyzedTweetInfo = mutableListOf<TweetInfo>()

        // Iterate over each tweet in the list
        tweetInfos.map {
            // Check if sentiment information exists for the tweet
            val sentimentInfo = findSentimentBy(it.id)
            if (sentimentInfo == null) {
                // If sentiment information does not exist, add the tweet to the list of not yet analyzed tweets
                notYetAnalyzedTweetInfo.add(it)
            } else {
                // If sentiment information exists, create a new TweetInfoWithSentiment object and add it to the list
                tweetInfoWithSentiments.add(TweetInfoWithSentiment(it, sentimentInfo.sentiment))
                logger.info("SentimentInfo already exists. $it")
            }
        }

        // Analyze the sentiment of the not yet analyzed tweets using a mock implementation
        val newTweetInfoWithSentiments = analyzeSentiment(notYetAnalyzedTweetInfo)

        // Iterate over each newly analyzed tweet
        newTweetInfoWithSentiments.forEach {
            // Create sentiment information for the tweet and add it to the data gateway
            create(SentimentInfo(it.tweet.id, it.sentiment))
            logger.info("SentimentInfo inserted. $it")
        }

        // Add the newly analyzed tweets to the list of analyzed tweets
        tweetInfoWithSentiments.addAll(newTweetInfoWithSentiments)

        // Return the list of analyzed tweets with sentiment information
        return tweetInfoWithSentiments
    }

    /**
     * Analyzes the sentiment of a list of tweets using a mock implementation.
     *
     * @param tweetInfos The list of tweets to analyze.
     * @return A list of [TweetInfoWithSentiment] objects representing the analyzed tweets with mock sentiment information.
     */
    fun analyzeSentimentMock(tweetInfos: List<TweetInfo>): List<TweetInfoWithSentiment> {
        // Create an empty list to store the tweets with mock sentiment information
        val tweetInfosWithSentiment = mutableListOf<TweetInfoWithSentiment>()

        // Iterate over each tweet in the list
        tweetInfos.map { tweetInfo ->
            // Create a new TweetInfoWithSentiment object with mock sentiment information
            tweetInfosWithSentiment.add(TweetInfoWithSentiment(tweetInfo, "I'm Happy"))
        }

        // Return the list of tweets with mock sentiment information
        return tweetInfosWithSentiment
    }

    /**
     * Analyzes the sentiment of a list of tweets using an external sentiment analysis service.
     *
     * @param tweetInfos The list of tweets to analyze.
     * @return A list of [TweetInfoWithSentiment] objects representing the analyzed tweets with sentiment information.
     */
    fun analyzeSentiment(tweetInfos: List<TweetInfo>): List<TweetInfoWithSentiment> {
        // Create an empty list to store the tweets with sentiment information
        val tweetInfosWithSentiment = mutableListOf<TweetInfoWithSentiment>()

        // Iterate over each tweet in the list
        tweetInfos.map { tweetInfo ->
            // Generate prompt messages based on the tweet information
            val messages = makePrompt(tweetInfo)
            // Send the prompt messages and retrieve the completion result
            val ccRequest = getResult(messages)
            // Extract the sentiment result from the completion result
            val sentimentResult = ccRequest.choices[0].message.content
            // Create a new TweetInfoWithSentiment object with the tweet information and sentiment result
            tweetInfosWithSentiment.add(TweetInfoWithSentiment(tweetInfo, sentimentResult))
        }

        // Return the list of tweets with sentiment information
        return tweetInfosWithSentiment
    }

    /**
     * Creates a list of chat messages containing a system message with the generated prompt for sentiment analysis.
     *
     * @param tweetInfo The tweet information.
     * @return A list of chat messages.
     */
    private fun makePrompt(tweetInfo: TweetInfo): List<ChatMessage> {
        // Create a list containing a single chat message
        return listOf(
            ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                generatePrompt(tweetInfo)
            )
        )
    }

    /**
     * Generates a prompt message for sentiment analysis based on the tweet information.
     *
     * @param tweetInfo The tweet information.
     * @return The generated prompt message as a string.
     */
    private fun generatePrompt(tweetInfo: TweetInfo): String {
        // Construct a prompt message by combining the tweet description with additional instructions
        return "Please analyze the sentiment of this tweet '" +
                tweetInfo.description +
                "' in one paragraph with no more than three sentences."
    }

    /**
     * Sends a list of chat messages to the OpenAI service and retrieves the completion result.
     *
     * @param messages The list of chat messages to send.
     * @return The completion result from the OpenAI service.
     */
    private fun getResult(messages: List<ChatMessage>): ChatCompletionResult {
        // Build a ChatCompletionRequest with the specified model, messages, temperature, and n values
        val ccRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .temperature(0.2)
            .n(1)
            .build()

        // Send the ChatCompletionRequest to the OpenAI service and return the completion result
        return openAiService.createChatCompletion(ccRequest)
    }
}
