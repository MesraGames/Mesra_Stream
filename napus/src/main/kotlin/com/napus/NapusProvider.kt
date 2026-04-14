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
        // Mengambil halaman utama (Home)
        val document = app.get(mainUrl).document
        val items = ArrayList<HomePageList>()

        // Selector untuk section: Latest Movies, Series, dsb.
        document.select("div.list-update_items-wrapper").forEach { section ->
            val title = section.selectFirst("div.list-update_items-header h2")?.text() ?: "Terbaru"
            val elements = section.select("div.list-update_item").mapNotNull { element ->
                element.toSearchResult()
            }
            if (elements.isNotEmpty()) {
                items.add(HomePageList(title, elements))
            }
        }

        return newHomePageResponse(items, false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        // Selector untuk judul, link, dan poster pada grid item
        val title = this.selectFirst("a")?.attr("title") ?: this.selectFirst("h3")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        
        // Cek apakah konten adalah TV Series atau Movie berdasarkan label atau icon
        val type = if (this.select(".type-series").isNotEmpty() || href.contains("/series/")) TvType.TvSeries else TvType.Movie

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
        // Melakukan pencarian menggunakan parameter ?s=
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.list-update_item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        // Selector metadata detail (Judul, Poster, Plot)
        val title = document.selectFirst("h1.entry-title")?.text()?.trim() ?: ""
        val poster = fixUrlNull(document.selectFirst(".poster img")?.attr("src"))
        val plot = document.selectFirst(".entry-content p")?.text()?.trim()
        
        val isSeries = url.contains("/series/") || document.select(".episodios").isNotEmpty()

        return if (isSeries) {
            val episodes = document.select(".episodios li").mapNotNull { episode ->
                val epHref = episode.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val epName = episode.selectFirst("a")?.text() ?: "Episode"
                
                // Menggunakan newEpisode untuk menghindari error deprecation
                newEpisode(epHref) {
                    this.name = epName
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

        // Mencari URL embed dari player options
        document.select(".dooplay_player_option").forEach { option ->
            val embedUrl = option.attr("data-url") // Biasanya base64 atau direct link
            if (embedUrl.isNotBlank()) {
                // Gunakan 3 parameter sesuai instruksi
                loadExtractor(embedUrl, subtitleCallback, callback)
            }
        }

        // Mencari iframe di dalam konten jika player option tidak ditemukan
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src")
            if (src.isNotBlank()) {
                loadExtractor(src, subtitleCallback, callback)
            }
        }

        return true
    }
}