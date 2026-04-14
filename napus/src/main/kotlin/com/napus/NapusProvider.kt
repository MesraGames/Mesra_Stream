package com.napus

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import android.content.Context
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class NapusProvider : MainAPI() {
    override var mainUrl = "https://napus.org"
    override var name = "Napus"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "id"
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        "trending/" to "Sedang Tren",
        "movies/" to "Film Terbaru",
        "tvshows/" to "Serial TV Terbaru",
        "genre/action/" to "Aksi",
        "genre/drama/" to "Drama"
    )

    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}/page/$page/").document
        val items = document.select("div.items article, div.result-item article").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3 a, .title a")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrl(this.selectFirst("img")?.attr("src") ?: "")
        val type = if (href.contains("/movies/")) TvType.Movie else TvType.TvSeries

        return if (type == TvType.Movie) {
            newMovieSearchResponse(title, href, type) { this.posterUrl = posterUrl }
        } else {
            newTvSeriesSearchResponse(title, href, type) { this.posterUrl = posterUrl }
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
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = fixUrl(document.selectFirst(".poster img")?.attr("src") ?: "")
        val plot = document.selectFirst(".wp-content p, #info .description")?.text()
        val year = document.selectFirst(".date")?.text()?.filter { it.isDigit() }?.toIntOrNull()
        val isMovie = url.contains("/movies/")

        if (isMovie) {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                addTrailer(document.selectFirst("iframe[src*='youtube']")?.attr("src"))
            }
        } else {
            val episodes = document.select("ul.episodios li").mapNotNull {
                val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst(".episodiotitle")?.text() ?: ""
                val seasonNum = it.selectFirst(".episodiotitle")?.text()?.substringBefore("x")?.toIntOrNull()
                val episodeNum = it.selectFirst(".episodiotitle")?.text()?.substringAfter("x")?.toIntOrNull()
                Episode(href, name, seasonNum, episodeNum)
            }
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isDataJob: Boolean,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        // TODO: Sesuaikan selector ini dengan struktur iframe atau tombol server di napus.org
        document.select("ul#playeroptionsul li, .dooplay_player_option").forEach { option ->
            val type = option.attr("data-type")
            val post = option.attr("data-post")
            val nume = option.attr("data-nume")
            
            val res = app.post(
                url = "$mainUrl/wp-admin/admin-ajax.php",
                data = mapOf("action" to "doo_player_ajax", "post" to post, "nume" to nume, "type" to type)
            ).document
            
            val embedUrl = res.selectFirst("iframe")?.attr("src")?.let { fixUrl(it) }
            if (embedUrl != null) {
                loadExtractor(embedUrl, data, callback)
            }
        }
        return true
    }
}