package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.VidStack
import org.jsoup.nodes.Element

class NontonanimeindoProvider : MainAPI() {
    /* ======================= Variables ======================= */
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    /* ======================= Main Page & Search ======================= */
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val items = document.select(".listupd .bs").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select(".listupd .bs").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst(".tt")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = this.selectFirst("img")?.fixPoster()
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    /* ======================= Load Details & Episodes ======================= */
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst(".entry-title")?.text() ?: ""
        val poster = document.selectFirst(".thumb img")?.fixPoster()
        val description = document.selectFirst(".entry-content")?.text()

        val episodes = document.select(".eplister li").map {
            val epUrl = fixUrl(it.selectFirst("a")?.attr("href") ?: "")
            val epName = it.selectFirst(".epl-num")?.text() ?: ""
            newEpisode(epUrl) {
                this.name = epName
            }
        }.reversed()

        return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    /* ======================= Links & Extractors ======================= */
    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select(".mirror option").forEach {
            val iframeUrl = it.attr("value")
            if (iframeUrl.isNotBlank()) {
                loadExtractor(fixUrl(iframeUrl), data, subtitleCallback, callback)
            }
        }
        return true
    }

    /* ======================= Helper Functions ======================= */
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