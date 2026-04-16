package com.rebahinn

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class RebahinnProvider : MainAPI() {
    override var mainUrl = "https://www.rebahinn.net"
    override var name = "Rebahinn"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "id"
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        "$mainUrl/" to "Beranda",
        "$mainUrl/movies/" to "Movies",
        "$mainUrl/tvseries/" to "TV Series",
        "$mainUrl/genre/action/" to "Action",
        "$mainUrl/genre/horror/" to "Horror"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page <= 1) request.data else "${request.data}page/$page/"
        val document = app.get(url).document
        // Selector untuk item film/series biasanya berada di dalam class .items atau article
        val items = document.select("div.items article, div.result-item article").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        // 1. Cari judul. Jika tidak ada di teks <a>, coba cari di alt gambar
        val title = this.selectFirst("h3 a, .title a")?.text() 
            ?: this.selectFirst("img")?.attr("alt") 
            ?: return null

        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        
        // 2. Trik Lazy Load: Cari di data-src dulu, kalau kosong baru ambil src
        val imgElement = this.selectFirst("img")
        val posterUrl = fixUrl(
            imgElement?.attr("data-src")?.ifEmpty { imgElement.attr("src") } 
            ?: imgElement?.attr("src") 
            ?: ""
        )
        
        val type = if (href.contains("/tvseries/")) TvType.TvSeries else TvType.Movie

        return if (type == TvType.TvSeries) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.result-item article").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1, .data h1")?.text() ?: return null
        val poster = fixUrl(document.selectFirst(".poster img")?.attr("src") ?: "")
        val plot = document.selectFirst(".wp-content p, .resumen")?.text()
        val year = document.selectFirst(".date")?.text()?.takeLast(4)?.toIntOrNull()

        val isTv = url.contains("/tvseries/")

        if (isTv) {
            // Ambil list episode dari elemen ul.episodios atau sejenisnya
            val episodes = document.select("ul.episodios li, .list-episodes li").mapNotNull {
                val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst("a")?.text() ?: "Episode"
                val season = it.selectFirst(".season")?.text()?.toIntOrNull() ?: 1
                val episode = it.selectFirst(".episode")?.text()?.toIntOrNull()
                newEpisode(link) {
                    this.name = name
                    this.season = season
                    this.episode = episode
                }
            }
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
            }
        } else {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                addTrailer(document.selectFirst("iframe[src*=youtube]")?.attr("src"))
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
        // Cari iframe player (biasanya di dalam .player-iframe atau source script)
        document.select("iframe, .dooplay_player_option").forEach { element ->
            val iframeUrl = fixUrl(element.attr("src").ifEmpty { element.attr("data-url") })
            if (iframeUrl.isNotEmpty()) {
                loadExtractor(iframeUrl, data, subtitleCallback, callback)
            }
        }
        return true
    }
}