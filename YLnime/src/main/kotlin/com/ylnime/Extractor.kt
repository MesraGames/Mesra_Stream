package com.ylnime
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class YLNimeExtractor : Extractor {
    override val name: String = "YLNime"
    override val mainUrl: String = "https://ylnime.com"
    
    override suspend fun urlValid(url: String): Boolean {
        return true
    }
    
    override suspend fun extractMetadata(element: Element): Metadata {
        val title = element.selectFirst("h2")?.text()
        return Metadata(
            title = title ?: "",
            plot = element.selectFirst("div Plot")?.text() ?: "",
            coverUrl = element.selectFirst("div.thumbnail > img")?.attr("src") ?: "",
        )
    }
    
    override suspend fun extractEpisodeLinks(element: Element): List<ExtractorLink> {
        return element.select("div.eplister > ul > li").map { link ->
            ExtractorLink(
                link.attr("data-id"),
                link.selectFirst("a")?.attr("href") ?: "",
                link.selectFirst("span.status")?.text() ?: "",
                link.selectFirst("img")?.attr("src") ?: ""
            )
        }
    }
}