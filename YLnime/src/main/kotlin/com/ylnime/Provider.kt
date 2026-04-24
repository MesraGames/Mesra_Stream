package com.ylnime
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class YLNimeProvider : MainAPI() {
    override val name: String = "YLNime"
    override val mainUrl: String = "https://ylnime.com"
    override val lang: String = "id"
    override val hasMainPage: Boolean = true
    override val hasChapters: Boolean = true
    
    override suspend fun getMainPage(): HomePageResponse {
        val document = app.get(mainUrl).document
        val allItems = document.select("div.eplister > ul > li").map { item ->
            val title = item.selectFirst("h2")?.text() ?: ""
            val link = item.selectFirst("a")?.attr("href") ?: ""
            AnimeSearchResponse(
                title = title,
                link = link,
                id = link
            )
        }
        return HomePageResponse(allItems)
    }
    
    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrl = "$mainUrl/index.php?search=$query"
        val document = app.get(searchUrl).document
        return document.select("div.eplister > ul > li").map { item ->
            val title = item.selectFirst("h2")?.text() ?: ""
            val link = item.selectFirst("a")?.attr("href") ?: ""
            AnimeSearchResponse(
                title = title,
                link = link,
                id = link
            )
        }
    }
    
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val extractor = YLNimeExtractor()
        val metadata = extractor.extractMetadata(document)
        return LoadResponse(
            name = metadata.title,
            url = url,
            type = TvType.Anime,
            data = extractor.extractEpisodeLinks(document)
        )
    }
    
    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val extractor = YLNimeExtractor()
        val links = extractor.extractEpisodeLinks(app.get(data).document)
        for (link in links) {
            callback.invoke(link)
        }
        return true
    }
}