package io.collective.start.tweets

/**
 * Data class representing information about a tweet.
 *
 * @property id The ID of the tweet, which is the hash of other properties.
 * @property title The title or headline of the tweet.
 * @property description The description or content of the tweet.
 * @property pubDate The publication date of the tweet.
 * @property link The link or URL associated with the tweet.
 * @property author The author or creator of the tweet.
 */
data class TweetInfo(
    val id: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val author: String
) {
    override fun toString(): String {
        return "TweetInfo(id='$id'," +
                " title='$title'," +
                " description='$description'," +
                " pubDate='$pubDate'," +
                " link='$link'," +
                " author='$author')"
    }
}
