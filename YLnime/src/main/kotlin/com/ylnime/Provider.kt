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
        val elements = doc.select("div.post")
        for (element in elements) {
            val title = element.selectFirst("h2")?.text() ?: ""
            val url = element.selectFirst("a")?.attr("href") ?: ""
            val posterUrl = fixUrl(element.selectFirst("img")?.let { it.attr("data-src`).ifEmpty { it.attr("src") } } ?: "")
            items.add(HomePageList(title, listOf(newAnimeSearchResponse(title, url, TvType.Anime) {
                this.posterUrl = posterUrl
            })))
        }
        return newHomePageResponse(items, false)
    }
    
    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get(mainUrl + "?s=" + query).document
        val elements = doc.select("div.post")
        return elements.map { element ->
            val title = element.selectFirst("h2")?.text() ?: ""
            val url = element.selectFirst("a")?.attr("href") ?: ""
            val posterUrl = fixUrl(element.selectFirst("img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } } ?: "")
            newAnimeSearchResponse(title, url, TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }
    }
    
    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: ""
        val posterUrl = fixUrl(doc.selectFirst("div.post-thumb")?.selectFirst("img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } } ?: "")
        val episodes = ArrayList<Episode>()
        val elements = doc.select("div.eplist")
        for (element in elements) {
            val episodeUrl = element.selectFirst("a")?.attr("href") ?: ""
            val episodeName = element.selectFirst("a")?.text() ?: ""
            episodes.add(newEpisode(episodeUrl) {
                this.name = episodeName
            })
        }
        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = posterUrl
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }
    
    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        callback.invoke(newExtractorLink(name, name, data, ExtractorLinkType.VIDEO) {
            this.referer = mainUrl
            this.quality = Qualities.Unknown.value
        })
        return true
    }
}