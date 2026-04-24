package com.ylnime
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class YLNime : MainAPI() {
    override var mainUrl = "https://ylnime.com"
    override var name = "YLNime"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.Movie, TvType.TvSeries)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("https://ylnime.com/index.php?terbaru=1").document
        val home = document.select("article.box-article").mapNotNull {
            val title = it.selectFirst("h2")?.text()?.trim() ?: return@mapNotNull null
            val href = it.selectFirst("a")?.attr("href") ?: ""
            val img = it.selectFirst("img")
            val posterUrl = fixUrl(img?.attr("data-src")?.takeIf { it.isNotBlank() } ?: img?.attr("src") ?: "")
            
            newAnimeSearchResponse(title, fixUrl(href), TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }
        return newHomePageResponse(listOf(HomePageList("Home", home)), false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return emptyList()
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text()?.trim() ?: ""
        val img = document.selectFirst("img")
        val poster = fixUrl(img?.attr("data-src")?.takeIf { it.isNotBlank() } ?: img?.attr("src") ?: "")
        
        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            // addEpisodes(DubStatus.Subbed, listOf(newEpisode("data") { this.name = "Ep 1" }))
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        callback.invoke(YLNimeExtractor().getUrl(data, mainUrl)?.first()!!)
        return true
    }
}