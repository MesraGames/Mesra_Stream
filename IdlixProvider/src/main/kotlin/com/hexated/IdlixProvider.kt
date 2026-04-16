package com.z1

import org.jsoup.nodes.Element
import org.json.JSONObject
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class Z1Provider : MainAPI() {
    override var mainUrl = "https://z1.idlixku.com"
    override var name = "Z1 IDLIX"
    override val hasMainPage = true
    override var lang = "id"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "$mainUrl/" to "Beranda",
        "$mainUrl/genre/movies/" to "Movies",
        "$mainUrl/genre/tv-series/" to "TV Series",
        "$mainUrl/genre/trending-movie/" to "Trending Movies"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(request.data + (if (page > 1) "page/$page/" else "")).document
        // Selector untuk item biasanya .ml-item atau div bertema MoviePress
        val items = doc.select("div.ml-item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("data-original") ?: this.selectFirst("img")?.attr("src")
        val quality = this.selectFirst(".mli-quality")?.text()
        
        return if (href.contains("/tv-series/")) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                this.quality = getQualityFromString(quality)
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                this.quality = getQualityFromString(quality)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("div.ml-item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1[itemprop=name]")?.text() ?: doc.selectFirst("h2")?.text() ?: ""
        val poster = doc.selectFirst("img[itemprop=image]")?.attr("src")
        val plot = doc.selectFirst("div[itemprop=description]")?.text()
        val year = doc.selectFirst(".mvic-info")?.select("a")?.filter { it.attr("href").contains("release-year") }?.firstOrNull()?.text()?.toIntOrNull()
        
        return if (url.contains("/tv-series/")) {
            val episodes = doc.select("div.les-content a").map { eps ->
                val href = eps.attr("href")
                val name = eps.text()
                // Ekstrak season dan episode dari text atau URL jika ada
                newEpisode(href) {
                    this.name = name
                }
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        // IDLIX sering menggunakan iframe di tab atau div pemutar
        doc.select("iframe").forEach { iframe ->
            var iframeUrl = iframe.attr("src")
            if (iframeUrl.startsWith("//")) iframeUrl = "https:" + iframeUrl
            loadExtractor(iframeUrl, data, subtitleCallback, callback)
        }
        return true
    }
}