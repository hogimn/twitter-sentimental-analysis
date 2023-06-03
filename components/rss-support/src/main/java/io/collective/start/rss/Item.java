package io.collective.start.rss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Represents an item within the channel of an RSS feed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    @JacksonXmlProperty(isAttribute = true, localName = "title")
    private String title;

    /**
     * Retrieves the title of the item.
     *
     * @return The title of the item.
     */
    public String getTitle() {
        return title.trim();
    }

    @JacksonXmlProperty(isAttribute = true, localName = "description")
    private String description;

    /**
     * Retrieves the description of the item.
     *
     * @return The description of the item.
     */
    public String getDescription() {
        return description.trim();
    }

    @JacksonXmlProperty(isAttribute = true, localName = "pubDate")
    private String pubDate;

    /**
     * Retrieves the publishing date of the item.
     *
     * @return The publishing date of the item.
     */
    public String getPubDate() {
        return pubDate.trim();
    }

    @JacksonXmlProperty(isAttribute = true, localName = "link")
    private String link;

    /**
     * Retrieves the link of the item.
     *
     * @return The link of the item.
     */
    public String getLink() {
        return link.trim();
    }

    @JacksonXmlProperty(isAttribute = true, localName = "author")
    private String author;

    /**
     * Retrieves the author of the item.
     *
     * @return The author of the item.
     */
    public String getAuthor() {
        return author.trim();
    }

    @Override
    public String toString() {
        return "Item{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", link='" + link + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
