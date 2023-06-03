package io.collective.start.collector

import io.collective.start.workflow.WorkFinder
import org.slf4j.LoggerFactory

/**
 * Implementation of the WorkFinder interface for finding and marking completed endpoint tasks.
 *
 * @param gateway The EndpointDataGateway used for accessing endpoint task data.
 */
class TweetCollectorEndpointWorkFinder(
    private val gateway: TweetCollectorEndpointDataGateway
) : WorkFinder<TweetCollectorEndpointTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Finds requested endpoint tasks based on the provided name.
     *
     * @param name The name used for searching requested tasks.
     * @return A list of EndpointTask objects representing the requested tasks.
     */
    override fun findRequested(name: String): List<TweetCollectorEndpointTask> {
        logger.info("Finding work.")
        return gateway.findReady(name).stream()
            .map { TweetCollectorEndpointTask(it.link) }
            .toList()
    }

    /**
     * Marks the specified endpoint task as completed.
     *
     * @param info The EndpointTask object representing the completed task.
     */
    override fun markCompleted(info: TweetCollectorEndpointTask) {
        logger.info("Marking work complete.")
    }
}
