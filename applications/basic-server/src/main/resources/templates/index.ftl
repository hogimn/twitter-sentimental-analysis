<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <p>
                An example application using Kotlin and Ktor.
            </p>
        </div>
    </section>

    <section>
        <div class="container tweet-table">
            <h2>Tweets with Sentimental Analysis</h2>

            <#if tweetInfoWithSentiments??>
                <div class="tweet-list">
                    <#list tweetInfoWithSentiments as tweetInfoWithSentiment>
                        <div class="tweet-item">
                            <div class="tweet-author">
                                ${tweetInfoWithSentiment.tweet.author}
                            </div>
                            <div class="tweet-pubdate">
                                ${tweetInfoWithSentiment.tweet.pubDate}
                            </div>
                            <div class="tweet-description">
                                ${tweetInfoWithSentiment.tweet.description}
                            </div>
                            <div class="tweet-sentiment">
                                ${tweetInfoWithSentiment.sentiment}
                            </div>
                        </div>
                    </#list>
                </div>
            </#if>
        </div>
    </section>

</@layout.noauthentication>