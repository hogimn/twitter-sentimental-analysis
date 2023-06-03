import io.collective.start.collector.TweetCollectorEndpointDataGateway
import io.collective.start.testsupport.testDataSource
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class TweetCollectorEndpointDateGatewayTest {
    @Test
    fun ready() {
        val gateway = TweetCollectorEndpointDataGateway(testDataSource())
        assertNotSame(0, gateway.findReady("data-collector").size)
        assertEquals(0, gateway.findReady("not-data-collector").size)
    }
}