package io.collective.start.analyzer

import io.collective.start.tweets.TweetInfo

/**
 * Data class representing information about a tweet, including sentiment analysis.
 *
 * @property tweet The basic information of the tweet.
 * @property sentiment The sentiment analysis result of the tweet.
 */
data class TweetInfoWithSentiment(
    val tweet: TweetInfo,
    val sentiment: String
) {
    override fun toString(): String {
        return "TweetInfoWithSentiment(tweet=$tweet," +
                " sentiment='$sentiment')"
    }
}
