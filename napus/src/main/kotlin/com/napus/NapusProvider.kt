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
        val items = ArrayList<HomePageList>()
        // Mengambil halaman utama untuk mengekstrak daftar film dan series terbaru
        val document = app.get(mainUrl).document
        
        // Selector untuk item di homepage (biasanya dalam tag article atau div.item)
        document.select(".items article, .list-update .movie-item").let { elements ->
            val homeItems = elements.mapNotNull { it.toSearchResult() }
            if (homeItems.isNotEmpty()) {
                items.add(HomePageList("Terbaru", homeItems))
            }
        }
        return newHomePageResponse(items, false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        // Selector judul: biasanya h3 a atau div.title a
        val title = this.selectFirst(".title a, h3 a, .entry-title a")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        // Selector poster: mencari tag img dan mengambil attribute src atau data-src
        val posterUrl = this.selectFirst("img")?.let { 
            it.attr("data-src").ifEmpty { it.attr("src") } 
        }
        val isTv = href.contains("/tvseries/") || href.contains("/tvshows/") || this.select(".type-tv").isNotEmpty()

        return if (isTv) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // Endpoint pencarian standar WordPress /?s=query
        val document = app.get("$mainUrl/?s=$query").document
        return document.select(".result-item article, .items article, .list-update .movie-item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        // Selector untuk detail konten (judul, poster, deskripsi)
        val title = document.selectFirst(".data h1, .entry-title")?.text() ?: return null
        val poster = document.selectFirst(".poster img, .thumb img")?.let { 
            it.attr("data-src").ifEmpty { it.attr("src") } 
        }
        val plot = document.selectFirst(".wp-content p, .entry-content p, .resumo")?.text()
        val isTv = url.contains("/tvseries/") || url.contains("/tvshows/")

        return if (isTv) {
            // Selector untuk list episode pada series
            val episodes = document.select("ul.episodios li, .list-episode li").mapNotNull {
                val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst(".numerando, .ep-title")?.text() ?: "Episode"
                newEpisode(href) { 
                    this.name = name 
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
        val document = app.get(data).document
        // Mencari semua link embed dalam iframe atau player wrapper
        document.select("iframe, .video-content iframe, #player-option-1 iframe").forEach { 
            var embedUrl = it.attr("src")
            if (embedUrl.startsWith("//")) embedUrl = "https:" + embedUrl
            if (embedUrl.isNotEmpty()) {
                loadExtractor(embedUrl, subtitleCallback, callback)
            }
        }
        return true
    }
}