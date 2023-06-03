package io.collective.start.analyzer

import io.collective.start.database.DatabaseTemplate
import org.slf4j.LoggerFactory
import javax.sql.DataSource

/**
 * SentimentDataGateway is responsible for interacting with the sentiments table in the database.
 *
 * @param dataSource The data source used to obtain database connections.
 */
class SentimentDataGateway(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val template = DatabaseTemplate(dataSource)

    /**
     * Creates a new sentiment record with the given information in the database.
     *
     * @param id The ID of the tweet.
     * @param sentiment The sentiment associated with the tweet.
     * @return The created SentimentRecord object, or null if creation fails.
     */
    fun create(id: String, sentiment: String): SentimentRecord? {
        if (findBy(id) != null) {
            return null
        }
        return template.create(
            "insert into sentiments (id, sentiment) values (?, ?)",
            { SentimentRecord(id, sentiment) },
            id, sentiment
        )
    }

    /**
     * Retrieves all sentiment records from the database.
     *
     * @return The list of all sentiment records.
     */
    fun findAll(): List<SentimentRecord> {
        return template.findAll(
            "select id, sentiment from sentiments order by id"
        ) { rs ->
            SentimentRecord(
                rs.getString(1),
                rs.getString(2)
            )
        }
    }

    /**
     * Retrieves a sentiment record from the database by its ID.
     *
     * @param id The ID of the tweet to retrieve the sentiment record for.
     * @return The sentiment record with the specified ID, or null if not found.
     */
    fun findBy(id: String): SentimentRecord? {
        return template.findBy(
            "select id, sentiment from sentiments where id = ?", { rs ->
                SentimentRecord(
                    rs.getString(1),
                    rs.getString(2)
                )
            }, id
        )
    }

    /**
     * Updates the specified sentiment record in the database.
     *
     * @param sentimentRecord The sentiment record to update.
     * @return The updated sentiment record.
     */
    fun update(sentimentRecord: SentimentRecord): SentimentRecord {
        template.update(
            "update sentiments" +
                    " set sentiment = ?" +
                    " where id = ?",
            sentimentRecord.sentiment, sentimentRecord.id
        )
        return sentimentRecord
    }

    /**
     * Clears all rows from the `sentiments` table.
     */
    fun clear() {
        template.update("delete from sentiments")
    }
}
