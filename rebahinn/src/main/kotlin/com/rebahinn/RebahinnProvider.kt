package com.rebahinn

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import android.content.Context
import org.jsoup.nodes.Element

class RebahinnProvider : MainAPI() {
    override var mainUrl = "https://www.rebahinn.net"
    override var name = "Rebahinn"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        // Mengambil halaman utama (Biasanya kategori Populer, Terbaru, dll)
        val document = app.get(mainUrl).document
        val items = ArrayList<HomePageList>()

        // Selector untuk section container (sesuaikan jika struktur web berubah)
        document.select(".items").forEach { section ->
            val title = section.previousElementSibling()?.text() ?: "Rekomendasi"
            // Selector untuk item kartu film
            val movies = section.select("article").mapNotNull {
                it.toSearchResult()
            }
            if (movies.isNotEmpty()) items.add(HomePageList(title, movies))
        }

        return newHomePageResponse(items, false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        // Selector: .data h3 a untuk judul, img untuk poster
        val title = this.selectFirst(".data h3 a")?.text() ?: return null
        val href = this.selectFirst(".data h3 a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")
        
        return if (href.contains("/tvshows/")) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // Endpoint pencarian standar WordPress /?s=query
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        // Selector .result-item untuk hasil pencarian
        return document.select("article").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        // Selector h1 untuk judul, .wp-content untuk sinopsis
        val title = document.selectFirst(".sheader .data h1")?.text() ?: return null
        val poster = document.selectFirst(".poster img")?.attr("src")
        val plot = document.selectFirst(".wp-content p")?.text()

        return if (url.contains("/tvshows/")) {
            // Logika untuk TV Series: ambil daftar episode dari selector .episodios
            val episodes = document.select(".episodios li").mapNotNull { element ->
                val epHref = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = element.selectFirst(".numerando")?.text() ?: "Episode"
                newEpisode(epHref) {
                    this.name = name
                }
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            // Logika untuk Movie
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        // Selector untuk mencari iframe atau link video (biasanya di dalam .source-box)
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src")
            if (src.isNotEmpty()) {
                // Menggunakan loadExtractor bawaan Cloudstream untuk host populer
                loadExtractor(src, subtitleCallback, callback)
            }
        }
        return true
    }
}