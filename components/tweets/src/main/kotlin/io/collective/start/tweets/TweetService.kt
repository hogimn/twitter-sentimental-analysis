package io.collective.start.tweets

/**
 * Service class for managing tweet-related operations.
 *
 * @property dataGateway The data gateway for accessing tweet data.
 */
class TweetService(private val dataGateway: TweetDataGateway) {
    /**
     * Retrieves all tweets.
     *
     * @return The list of all tweets.
     */
    fun findAll(): List<TweetInfo> {
        return dataGateway.findAll().map { TweetInfo(it.id, it.title, it.description, it.pubDate, it.link, it.author) }
    }

    /**
     * Retrieves a tweet by its ID.
     *
     * @param id The ID of the tweet.
     * @return The tweet with the specified ID.
     */
    fun findBy(id: String): TweetInfo {
        val record = dataGateway.findBy(id)!!
        return TweetInfo(record.id, record.title, record.description, record.pubDate, record.link, record.author)
    }

    /**
     * Updates the information of a tweet.
     *
     * @param tweetInfo The updated tweet information.
     * @return The updated tweet information.
     */
    fun update(tweetInfo: TweetInfo): TweetInfo {
        val record = dataGateway.findBy(tweetInfo.id)!!
        dataGateway.update(record)
        return findBy(record.id)
    }

    /**
     * Creates a new tweet.
     *
     * @param tweetInfo The information of the new tweet.
     * @return The created tweet information, or null if creation fails.
     */
    fun create(tweetInfo: TweetInfo): TweetInfo? {
        val record = dataGateway.create(
            tweetInfo.title,
            tweetInfo.description,
            tweetInfo.pubDate,
            tweetInfo.link,
            tweetInfo.author
        )
            ?: return null
        return findBy(record.id)
    }
}
