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
        val doc = app.get(mainUrl + "/index.php?terbaru=" + page.toString()).document
        val items = ArrayList<HomePageList>()
        val elements = doc.select("div.post")
        for (element in elements) {
            val title = element.select("h2.title").text().trim()
            val url = element.select("a").attr("href")
            val posterUrl = element.select("img").attr("src")
            items.add(HomePageList(title, listOf(newAnimeSearchResponse(title, url, TvType.Anime) { this.posterUrl = posterUrl })))
        }
        return newHomePageResponse(items, true)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get(mainUrl + "/?s=" + query).document
        val items = ArrayList<SearchResponse>()
        val elements = doc.select("div.search-result")
        for (element in elements) {
            val title = element.select("h2").text().trim()
            val url = element.select("a").attr("href")
            val posterUrl = element.select("img").attr("src")
            items.add(newAnimeSearchResponse(title, url, TvType.Anime) { this.posterUrl = posterUrl })
        }
        return items
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.select("h1").text().trim()
        val posterUrl = doc.select("imgattachment").attr("src")
        val episodes = ArrayList<Episode>()
        val elements = doc.select("div.episode")
        for (element in elements) {
            val episodeUrl = element.select("a").attr("href")
            episodes.add(newEpisode(episodeUrl) { this.name = element.select("a").text().trim() })
        }
        return newAnimeLoadResponse(title, url, TvType.Anime) { 
            this.posterUrl = posterUrl
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        val elements = doc.select("div.video-player")
        for (element in elements) {
            val link = element.select("source").attr("src")
            callback.invoke(newExtractorLink(name, name, link, mainUrl, Qualities.Unknown.value, false))
        }
        return true
    }
}