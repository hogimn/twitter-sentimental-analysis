package io.collective.start.database

import java.security.MessageDigest
import java.sql.*
import java.time.LocalDate
import javax.sql.DataSource

/**
 * The DatabaseTemplate class provides utility methods for interacting with a database using JDBC.
 *
 * @property dataSource The DataSource object representing the database connection.
 */
class DatabaseTemplate(private val dataSource: DataSource) {

    /**
     * Executes an SQL INSERT statement with the given parameters and returns the generated ID.
     *
     * @param sql The SQL INSERT statement.
     * @param mapper The function to convert the result set to appropriate type.
     * @param params The parameters to bind to the SQL statement.
     * @return The generated ID.
     */
    fun <T> create(sql: String, mapper: () -> T, vararg params: Any) =
        // Acquire a database connection using the data source
        dataSource.connection.use { connection ->
            // Delegate to the overloaded create function with the connection
            create(connection, sql, mapper, *params)
        }

    /**
     * Executes an SQL INSERT statement with the given connection, parameters, and returns the generated ID.
     *
     * @param connection The database connection.
     * @param sql The SQL INSERT statement.
     * @param mapper The function to convert the result set to appropriate type.
     * @param params The parameters to bind to the SQL statement.
     * @return The generated ID.
     */
    fun <T> create(connection: Connection, sql: String, mapper: () -> T, vararg params: Any): T {
        // Prepare the SQL statement with generated keys support
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
            // Set the parameters on the statement
            setParameters(params, statement)
            // Execute the SQL statement
            statement.executeUpdate()
            // Convert the result set to appropriate type
            mapper()
        }
    }

    /**
     * Executes an SQL SELECT statement and maps the result set to a list of objects using the provided mapper function.
     *
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> findAll(sql: String, mapper: (ResultSet) -> T): List<T> {
        // Call the query function with empty parameters and the provided mapper
        return query(sql, {}, mapper)
    }

    /**
     * Executes an SQL SELECT statement with the given connection and maps the result set to a list of objects
     * using the provided mapper function.
     *
     * @param connection The database connection.
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> findAll(connection: Connection, sql: String, mapper: (ResultSet) -> T): List<T> {
        // Call the query function with the provided connection,
        // SQL statement, empty parameters, and the provided mapper
        return query(connection, sql, {}, mapper)
    }

    /**
     * Executes an SQL SELECT statement with the given SQL, mapper function, and ID, and returns the corresponding object
     * or null if no object is found.
     *
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @param id The ID used to query the object.
     * @return The corresponding object or null if not found.
     */
    fun <T> findBy(sql: String, mapper: (ResultSet) -> T, id: String): T? {
        dataSource.connection.use { connection ->
            // Call the overloaded findBy function with the provided connection, SQL statement, mapper, and ID
            return findBy(connection, sql, mapper, id)
        }
    }

    /**
     * Executes an SQL SELECT statement with the given connection, SQL, mapper function, and ID,
     * and returns the corresponding object or null if no object is found.
     *
     * @param connection The database connection.
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @param id The ID used to query the object.
     * @return The corresponding object or null if not found.
     */
    fun <T> findBy(connection: Connection, sql: String, mapper: (ResultSet) -> T, id: String): T? {
        // Execute a query with the provided connection, SQL, parameter setter, and mapper function
        val list = query(connection, sql, { ps -> ps.setString(1, id) }, mapper)
        return when {
            // If the list is empty, return null indicating no object found
            list.isEmpty() -> null

            // Otherwise, return the first object in the list
            else -> list.first()
        }
    }

    /**
     * Executes an SQL UPDATE statement with the given SQL and parameters.
     *
     * @param sql The SQL UPDATE statement.
     * @param params The parameters to bind to the SQL statement.
     */
    fun update(sql: String, vararg params: Any) {
        // Obtain a connection from the dataSource
        dataSource.connection.use { connection ->
            // Execute the update function with the obtained connection, SQL statement, and parameters
            update(connection, sql, *params)
        }
    }

    /**
     * Executes an SQL UPDATE statement with the given connection, SQL, and parameters.
     *
     * @param connection The database connection.
     * @param sql The SQL UPDATE statement.
     * @param params The parameters to bind to the SQL statement.
     */
    fun update(connection: Connection, sql: String, vararg params: Any) {
        // Prepare the statement using the provided connection
        return connection.prepareStatement(sql).use { statement ->
            // Set the parameters on the prepared statement
            setParameters(params, statement)
            // Execute the update and obtain the number of affected rows
            statement.executeUpdate()
        }
    }

    /**
     * Executes an SQL SELECT statement with the given SQL, parameter binding function, and result set mapper,
     * and returns a list of mapped objects.
     *
     * @param sql The SQL SELECT statement.
     * @param params The function to set the parameters on the prepared statement.
     * @param mapper The function to map the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> query(sql: String, params: (PreparedStatement) -> Unit, mapper: (ResultSet) -> T): List<T> {
        // Use the data source's connection and execute the query
        dataSource.connection.use { connection ->
            // Delegate to the overloaded query function with the connection
            return query(connection, sql, params, mapper)
        }
    }

    /**
     * Executes an SQL query with the given connection, SQL statement, parameter binding function, and result set mapper,
     * and returns a list of mapped objects.
     *
     * @param connection The database connection.
     * @param sql The SQL SELECT statement.
     * @param params The function to set the parameters on the prepared statement.
     * @param mapper The function to map each row of the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> query(
        connection: Connection,
        sql: String,
        params: (PreparedStatement) -> Unit,
        mapper: (ResultSet) -> T
    ): List<T> {
        // Create an empty list to store the mapped objects
        val results = ArrayList<T>()
        // Prepare the statement and execute the query
        connection.prepareStatement(sql).use { statement ->
            // Set the parameters on the prepared statement
            params(statement)
            // Execute the query and process the result set
            statement.executeQuery().use { rs ->
                // Iterate over the result set and map each row to an object
                while (rs.next()) {
                    results.add(mapper(rs))
                }
            }
        }
        // Return the list of mapped objects
        return results
    }

    /**
     * Sets the parameters on the given prepared statement based on the provided parameter values.
     *
     * @param params The array of parameter values.
     * @param statement The prepared statement to set the parameters on.
     */
    private fun setParameters(params: Array<out Any>, statement: PreparedStatement) {
        for (i in params.indices) {
            // Retrieve the current parameter value and index
            val param = params[i]
            val parameterIndex = i + 1

            // Set the parameter value based on its type
            when (param) {
                is String -> statement.setString(parameterIndex, param)
                is Int -> statement.setInt(parameterIndex, param)
                is Long -> statement.setLong(parameterIndex, param)
                is Boolean -> statement.setBoolean(parameterIndex, param)
                is LocalDate -> statement.setDate(parameterIndex, Date.valueOf(param))
                is Timestamp -> statement.setTimestamp(parameterIndex, param)
            }
        }
    }

    /**
     * Executes an SQL DELETE statement with the given SQL and ID.
     *
     * @param sql The SQL DELETE statement.
     * @param id The ID used to delete the object.
     * @return The number of affected rows.
     */
    fun deleteBy(sql: String, id: String): Int {
        // Use the data source's connection and execute the delete
        dataSource.connection.use { connection ->
            // Delegate to the overloaded deleteBy function with the connection
            return deleteBy(connection, sql, id)
        }
    }

    /**
     * Executes an SQL DELETE statement with the given connection, SQL statement, and ID.
     *
     * @param connection The database connection.
     * @param sql The SQL DELETE statement.
     * @param id The ID used to delete the object.
     * @return The number of affected rows.
     */
    fun deleteBy(connection: Connection, sql: String, id: String): Int {
        // Prepare the statement and set the ID parameter
        return connection.prepareStatement(sql).use { statement ->
            statement.setString(1, id)
            // Execute the delete and obtain the number of affected rows
            statement.executeUpdate()
        }
    }

    /**
     * Calculates the MD5 hash value for the given input strings.
     *
     * @param title The title of the content.
     * @param description The description of the content.
     * @param pubDate The publication date of the content.
     * @param link The link associated with the content.
     * @param author The author of the content.
     * @return The MD5 hash value as a hexadecimal string.
     */
    fun calculateHash(title: String, description: String, pubDate: String, link: String, author: String): String {
        // Concatenate the input strings to form the content
        val content = "$title$description$pubDate$link$author"
        // Convert the content to a byte array using UTF-8 encoding
        val bytes = content.toByteArray(Charsets.UTF_8)
        // Create an instance of the MD5 hashing algorithm
        val md = MessageDigest.getInstance("MD5")
        // Compute the hash value by applying the algorithm to the byte array
        val hashBytes = md.digest(bytes)
        // Convert the hash bytes to a hexadecimal string representation
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
