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
        val doc = app.get(mainUrl + "?terbaru=1").document
        val items = ArrayList<HomePageList>()
        for (element in doc.select("div.rapid-row > div")) {
            val title = element.select("h2 > a").text()
            val url = element.select("h2 > a").attr("href") ?: ""
            val posterUrl = element.select("img").attr("src") ?: ""
            items.add(HomePageList(title, listOf(newAnimeSearchResponse(title, url, TvType.Anime) { this.posterUrl = posterUrl })))
        }
        return newHomePageResponse(items, false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return emptyList()
    }

    override suspend fun load(url: String): LoadResponse? {
        return null
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        callback.invoke(newExtractorLink(name, name, data, ExtractorLinkType.VIDEO) { this.referer = mainUrl; this.quality = Qualities.Unknown.value })
        return true
    }
}