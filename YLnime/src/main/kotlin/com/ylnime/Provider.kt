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
        val doc = app.get("https://ylnime.com/index.php?terbaru=1").document
        val items = ArrayList<HomePageList>()
        val lists = doc.select("div.lsm")
        for (list in lists) {
            val title = list.select("a.title").text().trim()
            val href = list.select("a.title").attr("href")
            val image = list.select("img.lazy").attr("data-src")
            items.add(HomePageList(title, listOf(newAnimeSearchResponse(title, href, TvType.Anime) { this.posterUrl = image })))
        }
        return newHomePageResponse(items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("https://ylnime.com/index.php?search=$query").document
        val lists = doc.select("div.lsm")
        val items = ArrayList<SearchResponse>()
        for (list in lists) {
            val title = list.select("a.title").text().trim()
            val href = list.select("a.title").attr("href")
            val image = list.select("img.lazy").attr("data-src")
            items.add(newAnimeSearchResponse(title, href, TvType.Anime) { this.posterUrl = image })
        }
        return items
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.select("h1.entry-title").text().trim()
        val type = if (doc.select("span.type").text().contains("Movie")) TvType.Movie else TvType.Anime
        val episodes = ArrayList<Episode>()
        val eps = doc.select("div.eplist").select("li")
        for (ep in eps) {
            val episode = ep.select("a").attr("href")
            episodes.add(Episode(ep.select("a").text().trim(), episode))
        }
        return newAnimeLoadResponse(title, url, type) { 
            this.posterUrl = doc.select("img.featuredimg").attr("src")
            addEpisodes(episodes)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val extractor = YLNimeExtractor()
        extractor.getUrl(data, null)?.let { links ->
            for (link in links) {
                callback.invoke(link)
            }
        }
        return true
    }
}