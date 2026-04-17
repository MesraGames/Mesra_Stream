package com.nontonanimeindo
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val supportedTypes = setOf(TvType.Anime)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val items = document.select(".listupd article").mapNotNull { it.toSearchResult() }
        return newHomePageResponse("Terbaru", items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst(".tt")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixPoster(this.selectFirst("img")?.attr("src"))
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select(".listupd article").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst(".entry-title")?.text() ?: ""
        val poster = fixPoster(document.selectFirst(".thumb img")?.attr("src"))
        val episodes = document.select(".eplister li a").map { 
            val href = fixUrl(it.attr("href"))
            val name = it.selectFirst(".epl-num")?.text() ?: ""
            newEpisode(href) {
                this.name = name
            }
        }
        return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = poster
        }
    }

    override suspend fun loadLinks(data: String, isDataJob: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select("iframe").forEach { 
            val src = fixUrl(it.attr("src"))
            loadExtractor(src, "$mainUrl/", subtitleCallback, callback)
        }
        return true
    }

    private fun fixPoster(url: String?): String? {
        if (url == null) return null
        if (url.startsWith("http")) return url
        return "$mainUrl$url"
    }
}