package io.collective.start.tweets

import io.collective.start.database.DatabaseTemplate
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

/**
 * TweetDataGateway is responsible for interacting with the tweets table in the database.
 *
 * @param dataSource The data source used to obtain database connections.
 */
class TweetDataGateway(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val template = DatabaseTemplate(dataSource)

    /**
     * Creates a new tweet with the given information in the database.
     *
     * @param title The title of the tweet.
     * @param description The description of the tweet.
     * @param pubDate The publication date of the tweet.
     * @param link The link or URL associated with the tweet.
     * @param author The author or creator of the tweet.
     * @return The created TweetRecord object, or null if creation fails.
     */
    fun create(title: String, description: String, pubDate: String, link: String, author: String): TweetRecord? {
        val timestamp = Timestamp.from(Instant.now())
        val id = template.calculateHash(title, description, pubDate, link, author)
        if (findBy(id) != null) {
            return null
        }
        return template.create(
            "insert into tweets (id, title, description, pubDate, link, author, timestamp)" +
                    " values (?, ?, ?, ?, ?, ?, ?)",
            { TweetRecord(id, title, description, pubDate, link, author, timestamp) },
            id, title, description, pubDate, link, author, timestamp
        )
    }

    /**
     * Retrieves all tweet records from the database.
     *
     * @return The list of all tweet records.
     */
    fun findAll(): List<TweetRecord> {
        return template.findAll(
            "select id, title, description, pubDate, link, author, timestamp from tweets order by timestamp"
        ) { rs ->
            TweetRecord(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getTimestamp(7)
            )
        }
    }

    /**
     * Retrieves a tweet record from the database by its ID.
     *
     * @param id The ID of the tweet to retrieve.
     * @return The tweet record with the specified ID, or null if not found.
     */
    fun findBy(id: String): TweetRecord? {
        return template.findBy(
            "select id, title, description, pubDate, link, author, timestamp from tweets where id = ?", { rs ->
                TweetRecord(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getTimestamp(7)
                )
            }, id
        )
    }

    /**
     * Updates the specified tweet record in the database.
     *
     * @param tweet The tweet record to update.
     * @return The updated tweet record.
     */
    fun update(tweet: TweetRecord): TweetRecord {
        val timestamp = Timestamp.from(Instant.now())
        template.update(
            "update tweets" +
                    " set title = ?, description = ?, pubDate = ?, link = ?, author = ?, timestamp = ?" +
                    " where id = ?",
            tweet.title, tweet.description, tweet.pubDate, tweet.link, tweet.author, timestamp, tweet.id
        )
        return tweet
    }

    /**
     * Clears all rows from the `tweets` table.
     */
    fun clear() {
        template.update("delete from tweets")
    }
}
