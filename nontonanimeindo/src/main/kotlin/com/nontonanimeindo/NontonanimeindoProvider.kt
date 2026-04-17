package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
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
        val urlPath = if (request.data.isEmpty()) {
            if (page == 1) mainUrl else "$mainUrl/page/$page/"
        } else {
            if (page == 1) "$mainUrl${request.data}" else "$mainUrl${request.data}page/$page/"
        }

        val document = app.get(urlPath).document
        val home = document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block, ul.latest li, div.excstf article, .flw-item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block, ul.latest li, div.excstf article, .flw-item").mapNotNull {
            it.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val aTag = this.selectFirst("a") ?: (if (this.tagName() == "a") this else null)
        val href = fixUrlNull(aTag?.attr("href")) ?: return null
        val imgTag = this.selectFirst("img")

        val title = this.selectFirst("h2, h3, h4, h5, h6, .title, .tt h4, .post-title, [itemprop=name], .name")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: aTag?.attr("title")?.trim()?.takeIf { it.isNotBlank() }
            ?: imgTag?.attr("title")?.trim()?.takeIf { it.isNotBlank() }
            ?: imgTag?.attr("alt")?.trim()?.takeIf { it.isNotBlank() }
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
        val title = document.selectFirst("h1.entry-title, .infoname, .item-name")?.text()?.trim() ?: "Judul Tidak Diketahui"
        val poster = document.selectFirst("img[itemprop=image], .poster img, .thumb img").fixPoster()
        val plot = document.select("div.entry-content p, .desc, .sinopsis p").text().trim()
        val episodes = document.select(".eplists ul li, .listeps ul li, .episodelist ul li").mapNotNull {
            val a = it.selectFirst("a") ?: return@mapNotNull null
            val href = fixUrl(a.attr("href"))
            val name = a.select(".epl-num, .eps").text().trim().ifEmpty { "Episode " + it.select(".num").text() }
            newEpisode(href) { this.name = name }
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
        document.select("select.mirror option, .mirror-item, iframe").forEach { 
            val src = it.getIframeAttr() ?: it.attr("value")
            if (src.contains("http")) {
                loadExtractor(src, data, subtitleCallback, callback)
            }
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