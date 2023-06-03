package io.collective.start.collector

import io.collective.start.database.DatabaseTemplate
import org.slf4j.LoggerFactory
import javax.sql.DataSource

/**
 * Gateway for retrieving and manipulating tweet collector endpoint data.
 *
 * @param dataSource The DataSource used for accessing the underlying data storage.
 */
class TweetCollectorEndpointDataGateway(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val template = DatabaseTemplate(dataSource)

    /**
     * Retrieves a list of ready endpoint records based on the given name.
     *
     * @param name The name used to filter the endpoint records.
     * @return A list of [TweetCollectorEndpointRecord] objects representing the endpoint records.
     */
    fun findReady(name: String): List<TweetCollectorEndpointRecord> {
        // Check if the name is "data-collector"
        if (name != "data-collector") {
            // If not, return an empty list
            return emptyList()
        }

        // If the name is "data-collector", return a list of endpoint records
        return template.findAll("select id, link from endpoints") { rs ->
            TweetCollectorEndpointRecord(rs.getString(1), rs.getString(2))
        }
    }

    /**
     * Creates a new tweet collector endpoint record with the given information in the database.
     *
     * @param id The ID of the endpoint.
     * @param link The name of the endpoint.
     * @return The created [TweetCollectorEndpointRecord] object, or null if creation fails.
     */
    fun create(id: String, link: String): TweetCollectorEndpointRecord? {
        if (findBy(id) != null) {
            return null
        }
        return template.create(
            "insert into endpoints (id, link) values (?, ?)",
            { TweetCollectorEndpointRecord(id, link) },
            id, link
        )
    }

    /**
     * Finds a tweet collector endpoint record by its ID.
     *
     * @param id The ID of the endpoint record to find.
     * @return The [TweetCollectorEndpointRecord] object representing the found record, or null if not found.
     */
    fun findBy(id: String): TweetCollectorEndpointRecord? {
        return template.findBy(
            "select id, link from endpoints where id = ?", { rs ->
                TweetCollectorEndpointRecord(
                    rs.getString(1),
                    rs.getString(2)
                )
            }, id
        )
    }

    /**
     * Deletes a tweet collector endpoint record by its ID.
     *
     * @param id The ID of the endpoint record to delete.
     * @return The number of records deleted.
     */
    fun deleteBy(id: String): Int {
        return template.deleteBy("delete from endpoints where id = ?", id)
    }
}
