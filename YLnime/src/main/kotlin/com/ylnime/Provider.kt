package com.ylnime
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class YLNime : MainAPI() {
    override var mainUrl = "https://ylnime.com/"
    override var name = "YLNime"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.Movie, TvType.TvSeries)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        val doc = app.get(mainUrl + "index.php?terbaru=1").document
        val items = ArrayList<HomePageList>()
        val allItems = doc.select("div.box")
        for (item in allItems) {
            val title = item.selectFirst("div.information > h2 > a")?.text()
            val url = item.selectFirst("div.information > h2 > a")?.attr("href")
            val posterUrl = item.selectFirst("div.thumbnail > img")?.attr("src")
            if (title != null && url != null && posterUrl != null) {
                items.add(newAnimeSearchResponse(title, url, TvType.Anime) {
                    this.posterUrl = posterUrl
                })
            }
        }
        return newHomePageResponse(items, false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get(mainUrl + "?s=$query").document
        val items = ArrayList<SearchResponse>()
        val allItems = doc.select("div.box")
        for (item in allItems) {
            val title = item.selectFirst("div.information > h2 > a")?.text()
            val url = item.selectFirst("div.information > h2 > a")?.attr("href")
            val posterUrl = item.selectFirst("div.thumbnail > img")?.attr("src")
            if (title != null && url != null && posterUrl != null) {
                items.add(newAnimeSearchResponse(title, url, TvType.Anime) {
                    this.posterUrl = posterUrl
                })
            }
        }
        return items
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("div.post-title > h1")?.text()
        val posterUrl = doc.selectFirst("div.post-thumb > img")?.attr("src")
        val episodes = ArrayList<Episode>()
        val allEpisodes = doc.select("div.server-block > ul > li")
        for (episode in allEpisodes) {
            val episodeUrl = episode.selectFirst("a")?.attr("href")
            val episodeName = episode.selectFirst("a")?.text()
            if (episodeUrl != null && episodeName != null) {
                episodes.add(newEpisode(episodeUrl) {
                    this.name = episodeName
                })
            }
        }
        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = posterUrl
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        val link = doc.selectFirst("iframe")?.attr("src")
        if (link != null) {
            callback.invoke(newExtractorLink(name, name, link, ExtractorLinkType.VIDEO) {
                this.referer = mainUrl
                this.quality = Qualities.Unknown.value
            })
        }
        return true
    }
}