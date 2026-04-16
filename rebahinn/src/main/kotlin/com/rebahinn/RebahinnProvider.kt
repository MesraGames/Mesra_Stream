package com.rebahinn

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.utils.AppUtils.toJson

class RebahinnProvider : MainAPI() {
    override var mainUrl = "https://www.rebahinn.net"
    override var name = "Rebahinn"
    override val hasMainPage = true
    override var lang = "id"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "$mainUrl/trending/" to "Trending",
        "$mainUrl/movies/" to "Movies",
        "$mainUrl/tv-series/" to "TV Series",
        "$mainUrl/genre/action/" to "Action",
        "$mainUrl/genre/horror/" to "Horror"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        // Sesuaikan selector dengan struktur HTML Rebahinn (biasanya .ml-item atau .item)
        val document = app.get(request.data + if (page > 1) "page/$page/" else "").document
        val home = document.select("div.ml-item, div.item, article").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, h3, .entry-title")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src") ?: this.selectFirst("img")?.attr("data-src"))
        val quality = this.select(".quality, .status").text()
        
        // Logika sederhana untuk menentukan Movie atau TV
        val type = if (href.contains("/tv-series/") || href.contains("/tv/")) TvType.TvSeries else TvType.Movie

        return if (type == TvType.Movie) {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                addQuality(quality)
            }
        } else {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                addQuality(quality)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.ml-item, div.item, article").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1, .entry-title")?.text()?.trim() ?: return null
        val poster = fixUrlNull(document.selectFirst(".poster img, .mvic-thumb img")?.attr("src"))
        val plot = document.selectFirst(".description, .entry-content p")?.text()
        val type = if (url.contains("/tv-series/") || url.contains("/tv/")) TvType.TvSeries else TvType.Movie

        if (type == TvType.TvSeries) {
            // Selector episode untuk TV Series (biasanya dalam list atau dropdown)
            val episodes = document.select(".les-content a, .list-episode a").mapIndexed { index, el ->
                val href = el.attr("href")
                newEpisode(href) {
                    this.name = el.text().trim()
                    this.episode = index + 1
                }
            }
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        // Mencari iframe player
        document.select("iframe, .player-embed iframe").forEach { iframe ->
            val iframeUrl = fixUrl(iframe.attr("src"))
            if (iframeUrl.isNotEmpty()) {
                loadExtractor(iframeUrl, data, subtitleCallback, callback)
            }
        }
        
        return true
    }
}