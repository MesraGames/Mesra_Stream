package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "Nontonanimeindo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "" to "Terbaru",
        "/anime/" to "Anime List",
        "/movies/" to "Movie",
        "/genre/movie/" to "Bioskop"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val urlPath = if (request.data.isEmpty()) {
            if (page == 1) mainUrl else "$mainUrl/page/$page/"
        } else {
            val suffix = if (request.data.endsWith("/")) "" else "/"
            if (page == 1) "$mainUrl${request.data}" else "$mainUrl${request.data}${suffix}page/$page/"
        }
        
        val document = app.get(urlPath).document
        val home = document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block, ul.latest li, div.excstf article").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block, ul.latest li, div.excstf article").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val title = this.selectFirst("h2, h3, h4, .title, .tt h4, .post-title, [itemprop=name]")?.text()?.trim()
            ?: this.selectFirst("a")?.attr("title")?.trim()
            ?: this.selectFirst("img")?.attr("title")?.trim()
            ?: this.selectFirst("img")?.attr("alt")?.trim() ?: return null
        val posterUrl = this.selectFirst("img").fixPoster()

        return if (href.contains("/anime/") || href.contains("/series/")) {
            newAnimeSearchResponse(title, href, TvType.Anime) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title, .entry-title")?.text()?.trim() ?: ""
        val poster = document.selectFirst(".thumb img, .poster img, [itemprop=image]").fixPoster()
        val description = document.select(".entry-content p, .desc, .summary").text().trim()

        val episodes = document.select("div.eplister ul li, div.listue ul li, .cl-list li").mapNotNull {
            val epHref = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val epTitle = it.selectFirst(".epl-title")?.text() ?: it.selectFirst(".epl-num")?.text() ?: "Episode"
            newEpisode(epHref) { this.name = epTitle }
        }.reversed()

        return if (episodes.isEmpty()) {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = description
            }
        } else {
            newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
                this.posterUrl = poster
                this.plot = description
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select("select.mirror option, div.mvepe-list a, .player-embed iframe, iframe").forEach {
            val iframeUrl = it.getIframeAttr() ?: it.attr("value").takeIf { v -> v.startsWith("http") }
            if (!iframeUrl.isNullOrEmpty()) {
                loadExtractor(fixUrl(iframeUrl), data, subtitleCallback, callback)
            }
        }
        return true
    }

    private fun Element?.fixPoster(): String? {
        if (this == null) return null
        val dataSrc = this.attr("data-lazy-src").takeIf { it.isNotEmpty() } 
            ?: this.attr("data-src").takeIf { it.isNotEmpty() } 
            ?: this.attr("src")
        return fixUrl(dataSrc).replace(Regex("-\\d+x\\d+(?=\\.(webp|jpg|jpeg|png))", RegexOption.IGNORE_CASE), "")
    }

    private fun Element?.getIframeAttr(): String? {
        return this?.attr("data-litespeed-src").takeIf { !it.isNullOrEmpty() } ?: this?.attr("src")
    }
}