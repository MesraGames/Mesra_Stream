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
        for (element in doc.select(".list-update > .bs")) {
            val title = element.selectFirst("h3")?.text() ?: ""
            val url = element.selectFirst("a")?.attr("href") ?: ""
            items.add(HomePageList(title, listOf(newAnimeSearchResponse(title, url, TvType.Anime) {
                val img = element.selectFirst("img")
                val imgUrl = img?.attr("data-src")?.takeIf { it.isNotBlank() } ?: img?.attr("src") ?: ""
                this.posterUrl = fixUrl(imgUrl)
            })))
        }
        return newHomePageResponse(items, false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get(mainUrl + "?s=" + query).document
        return doc.select(".list-update > .bs").map { element ->
            val title = element.selectFirst("h3")?.text() ?: ""
            val url = element.selectFirst("a")?.attr("href") ?: ""
            newAnimeSearchResponse(title, url, TvType.Anime) {
                val img = element.selectFirst("img")
                val imgUrl = img?.attr("data-src")?.takeIf { it.isNotBlank() } ?: img?.attr("src") ?: ""
                this.posterUrl = fixUrl(imgUrl)
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst(".entry-title")?.text() ?: ""
        val posterUrl = doc.selectFirst(".wp-post-image")?.attr("src") ?: ""
        val episodes = ArrayList<Episode>()
        for (element in doc.select(".epliste > .epl")) {
            val episodeUrl = element.selectFirst("a")?.attr("href") ?: ""
            episodes.add(newEpisode(episodeUrl) {
                this.name = element.selectFirst("a")?.text() ?: ""
            })
        }
        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = fixUrl(posterUrl)
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