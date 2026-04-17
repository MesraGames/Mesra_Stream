package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    override val mainPage = mainPageOf(
        "$mainUrl/" to "Anime Terbaru",
        "$mainUrl/anime-ongoing/" to "Anime Ongoing",
        "$mainUrl/anime-movie/" to "Anime Movie"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + if (page > 1) "page/$page/" else "").document
        val items = document.select(".rel-post ul li, .post-item, .item").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select(".rel-post ul li, .post-item, .item").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, .title, a")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.fixPoster()
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst(".entry-title, h1")?.text() ?: ""
        val poster = document.selectFirst(".thumb img, .entry-content img")?.fixPoster()
        val plot = document.selectFirst(".entry-content p, .description")?.text()

        val episodes = document.select(".episodelist ul li").mapNotNull {
            val a = it.selectFirst("a") ?: return@mapNotNull null
            val epUrl = a.attr("href")
            val epName = a.text()
            newEpisode(epUrl) {
                this.name = epName
            }
        }.reversed()

        return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = poster
            this.plot = plot
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select(".video-content iframe, .embed-container iframe, iframe").forEach {
            val iframe = it.getIframeAttr() ?: return@forEach
            loadExtractor(iframe, data, subtitleCallback, callback)
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