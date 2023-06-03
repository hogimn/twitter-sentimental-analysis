package io.collective.start.testsupport

import io.collective.start.database.createDatasource
import javax.sql.DataSource

/**
 * JDBC URL for the test database.
 */
const val testJdbcUrl = "jdbc:postgresql://localhost:5432/tweet_test"

/**
 * Username for the test database.
 */
const val testDbUsername = "tweet"

/**
 * Password for the test database.
 */
const val testDbPassword = "tweet"

/**
 * Creates a DataSource for the test database.
 *
 * @return The created DataSource.
 */
fun testDataSource(): DataSource {
    return createDatasource(
        jdbcUrl = testJdbcUrl,
        username = testDbUsername,
        password = testDbPassword
    )
}
