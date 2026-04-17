package com.rebahinn

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class RebahinnProvider : MainAPI() {
    override var mainUrl = "https://www.rebahinn.net"
    override var name = "Rebahinn"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime, TvType.AsianDrama)
    
    override val mainPage = mainPageOf(
        "" to "Terbaru",
        "/anime/" to "Anime",
        "/movies/" to "Movie"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse { 
        val path = request.data
        val urlPath = if (path.isEmpty() || path == "/") {
            if (page == 1) mainUrl else "$mainUrl/page/$page/"
        } else if (path.startsWith("http")) {
            if (page == 1) path else "${path.removeSuffix("/")}/page/$page/"
        } else {
            if (page == 1) "$mainUrl$path" else "$mainUrl${path.removeSuffix("/")}/page/$page/"
        }
        
        val document = app.get(urlPath).document
        var home = document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block, ul.latest li, div.excstf article, .flw-item").mapNotNull { it.toSearchResult() }
        
        if (home.isEmpty()) {
            home = document.select("a").filter { it.selectFirst("img") != null && it.attr("href").length > 5 }.mapNotNull { it.toSearchResult() }
        }

        return newHomePageResponse(request.name, home.distinctBy { it.url })
    }
    
    override suspend fun search(query: String): List<SearchResponse> { 
        val document = app.get("$mainUrl/?s=$query").document
        var results = document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block, ul.latest li, div.excstf article, .flw-item").mapNotNull { it.toSearchResult() }
        
        if (results.isEmpty()) {
            results = document.select("a").filter { it.selectFirst("img") != null && it.attr("href").length > 5 }.mapNotNull { it.toSearchResult() }
        }
        return results.distinctBy { it.url }
    }
    
    private fun Element.toSearchResult(): SearchResponse? { 
        val aTag = if (this.tagName() == "a") this else this.selectFirst("a")
        val href = fixUrlNull(aTag?.attr("href")) ?: return null
        if (href.contains("javascript:") || href.contains("login") || href.contains("register")) return null
        
        val imgTag = this.selectFirst("img") ?: return null

        val title = this.selectFirst("h2, h3, h4, h5, h6, .title, .tt h4, .post-title, [itemprop=name], .name")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: aTag?.attr("title")?.trim()?.takeIf { it.isNotBlank() }
            ?: imgTag.attr("title").trim().takeIf { it.isNotBlank() }
            ?: imgTag.attr("alt").trim().takeIf { it.isNotBlank() }
            ?: "Judul Tidak Diketahui"
            
        val posterUrl = imgTag.fixPoster()
        
        return if (href.contains("/anime/") || href.contains("/series/") || href.contains("/tv/") || href.contains("episode")) {
            newAnimeSearchResponse(title, href, TvType.Anime) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .entry-title")?.text()?.trim() ?: ""
        val poster = doc.selectFirst("img.wp-post-image, .thumb img")?.fixPoster()
        val isTv = url.contains("/series/") || url.contains("/tv/") || doc.select(".eplister").isNotEmpty()
        
        return if (isTv) {
            val episodes = doc.select(".eplister li a, .list-episode li a").map {
                newEpisode(it.attr("href")) {
                    name = it.selectFirst(".epl-num")?.text() ?: it.text()
                }
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        doc.select("iframe, .video-content iframe").forEach { iframe ->
            val src = iframe.getIframeAttr() ?: return@forEach
            loadExtractor(src, data, subtitleCallback, callback)
        }
        return true
    }

    private fun Element?.fixPoster(): String? {
        if (this == null) return null
        if (this.hasAttr("srcset")) {
            val srcset = this.attr("srcset").trim()
            val best = srcset.split(",").map { it.trim().split(" ")[0] }.lastOrNull()
            if (!best.isNullOrBlank()) return fixUrl(best.fixImageQuality())
        }
        val dataSrc = when {
            this.hasAttr("data-lazy-src") -> this.attr("data-lazy-src")
            this.hasAttr("data-src") -> this.attr("data-src")
            else -> null
        }
        if (!dataSrc.isNullOrBlank()) return fixUrl(dataSrc.fixImageQuality())
        val src = this.attr("src")
        if (!src.isNullOrBlank()) return fixUrl(src.fixImageQuality())
        return null
    }

    private fun String?.fixImageQuality(): String {
        if (this == null) return ""
        val regex = Regex("-\\d+x\\d+(?=\\.(webp|jpg|jpeg|png))", RegexOption.IGNORE_CASE)
        return this.replace(regex, "")
    }

    private fun Element?.getIframeAttr(): String? {
        return this?.attr("data-litespeed-src").takeIf { !it.isNullOrEmpty() } ?: this?.attr("src")
    }
}