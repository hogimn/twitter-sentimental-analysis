package test.collective.rss;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.collective.start.rss.RSS;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RSSTest {

    @Test
    public void rss() throws IOException {
        String xml = new String(this.getClass().getResourceAsStream("/twitter.xml").readAllBytes());
        RSS rss = new XmlMapper().readValue(xml, RSS.class);
        assertEquals(20, rss.getChannel().getItem().size());
    }
}
