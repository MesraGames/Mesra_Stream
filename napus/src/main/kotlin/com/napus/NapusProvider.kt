package com.napus

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import android.content.Context
import org.jsoup.nodes.Element

class NapusProvider : MainAPI() {
    override var mainUrl = "https://napus.org"
    override var name = "Napus"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        // Selektor CSS untuk mengambil daftar item di halaman utama
        val items = ArrayList<HomePageList>()
        val urls = listOf(
            "$mainUrl/movies/" to "Movies",
            "$mainUrl/tvshows/" to "TV Series",
            "$mainUrl/trending/" to "Trending"
        )

        urls.forEach { (url, title) ->
            val doc = app.get(url).document
            // Mencari elemen article.item yang berisi informasi film
            val res = doc.select("div.items article").mapNotNull {
                it.toSearchResult()
            }
            items.add(HomePageList(title, res))
        }

        return newHomePageResponse(items, false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        // Mengambil judul, URL, dan poster dari elemen HTML
        val title = this.selectFirst("div.data h3 a")?.text() ?: return null
        val href = this.selectFirst("div.data h3 a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("div.poster img")?.attr("src")
        val type = if (href.contains("/tvshows/")) TvType.TvSeries else TvType.Movie

        return if (type == TvType.Movie) {
            newMovieSearchResponse(title, href, type) { this.posterUrl = posterUrl }
        } else {
            newTvSeriesSearchResponse(title, href, type) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // Melakukan pencarian menggunakan parameter ?s=
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("div.result-item article").mapNotNull {
            val title = it.selectFirst("div.details div.title a")?.text() ?: return@mapNotNull null
            val href = it.selectFirst("div.details div.title a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("div.image img")?.attr("src")
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = poster }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("div.data h1")?.text() ?: return null
        val poster = doc.selectFirst("div.poster img")?.attr("src")
        val plot = doc.selectFirst("div.wp-content p")?.text()
        val type = if (url.contains("/tvshows/")) TvType.TvSeries else TvType.Movie

        return if (type == TvType.TvSeries) {
            // Mengambil list episode untuk tipe TV Series
            val episodes = doc.select("ul.episodios li").mapNotNull {
                val href = it.selectFirst("div.episodiotitle a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst("div.episodiotitle a")?.text()
                val seasonNum = it.selectFirst("div.numerando")?.text()?.substringBefore("-")?.trim()?.toIntOrNull()
                val episodeNum = it.selectFirst("div.numerando")?.text()?.substringAfter("-")?.trim()?.toIntOrNull()
                newEpisode(href) {
                    this.name = name
                    this.season = seasonNum
                    this.episode = episodeNum
                }
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        // Mencari URL embed dari tab player atau iframe
        doc.select("ul#playeroptionsul li").forEach {
            val postId = it.attr("data-post")
            val nume = it.attr("data-nume")
            val type = it.attr("data-type")
            
            // Request AJAX untuk mendapatkan iframe source
            val res = app.post(
                url = "$mainUrl/wp-admin/admin-ajax.php",
                data = mapOf("action" to "doo_player_ajax", "post" to postId, "nume" to nume, "type" to type)
            ).parsed<ResponseSource>()
            
            val embedUrl = res.embed_url ?: ""
            if (embedUrl.isNotEmpty()) {
                loadExtractor(embedUrl, subtitleCallback, callback)
            }
        }
        return true
    }

    data class ResponseSource(val embed_url: String?)
}