package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "Nontonanimeindo"
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
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title, .thumb-title")?.text()?.trim() ?: "No Title"
        val poster = document.selectFirst("img.wp-post-image, .thumb img")?.fixPoster()
        val plot = document.selectFirst(".entry-content p, .sinopsis p")?.text()

        val episodes = document.select("div.episodelist li, .eplister li").mapNotNull {
            val epATag = it.selectFirst("a") ?: return@mapNotNull null
            val epHref = epATag.attr("href")
            val epTitle = epATag.text().trim()
            newEpisode(epHref) {
                this.name = epTitle
            }
        }

        return if (episodes.isEmpty()) {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select("iframe, .video-content iframe, .player-embed iframe").forEach { iframe ->
            val src = iframe.getIframeAttr() ?: return@forEach
            loadExtractor(src, data, subtitleCallback, callback)
        }
        return true
    }

    private fun Element?.fixPoster(): String? {
        if (this == null) return null
        val dataSrc = this.attr("data-lazy-src").takeIf { it.isNotEmpty() } ?: this.attr("data-src").takeIf { it.isNotEmpty() } ?: this.attr("src")
        return fixUrl(dataSrc.fixImageQuality())
    }

    private fun String?.fixImageQuality(): String {
        if (this == null) return ""
        return this.replace(Regex("-\\d+x\\d+(?=\\.(webp|jpg|jpeg|png))", RegexOption.IGNORE_CASE), "")
    }

    private fun Element?.getIframeAttr(): String? {
        return this?.attr("data-litespeed-src").takeIf { !it.isNullOrEmpty() } ?: this?.attr("src")
    }
}