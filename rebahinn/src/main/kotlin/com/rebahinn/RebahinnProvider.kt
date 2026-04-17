package com.rebahinn

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.ExtractorLinkType.Companion.INFER_TYPE
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.base64Decode

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
        val title = doc.selectFirst("h1, h2.entry-title, .infox h1")?.text() ?: "Title"
        val poster = doc.selectFirst("div.thumb img, .poster img")?.fixPoster()
        val plot = doc.selectFirst(".entry-content, .desc, .sinopsis")?.text()
        val isAnime = url.contains("anime") || doc.select("div.eplister").isNotEmpty()
        
        if (isAnime) {
            val episodes = mutableListOf<Episode>()
            doc.select("div.eplister li a, div.episodelist ul li a, ul.episodes li a").forEachIndexed { index, element ->
                val epHref = element.attr("href")
                val epTitle = element.selectFirst(".epl-title, .epl-num")?.text() ?: "Episode ${index + 1}"
                episodes.add(newEpisode(epHref) { this.name = epTitle })
            }
            if(episodes.isEmpty()) {
                episodes.add(newEpisode(url) { this.name = "Nonton" })
            }
            return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes.reversed()) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        val htmlRaw = document.html()

        document.select("iframe").forEach { iframe ->
            val src = iframe.getIframeAttr()
            if (!src.isNullOrBlank()) {
                loadExtractor(fixUrl(src), data, subtitleCallback, callback)
            }
        }

        document.select(".server-list a, ul#player-list a, .mirror_link a, .btn-server, .play-btn, [data-video], [data-src], [data-link]").forEach { element ->
            var link = element.attr("href").takeIf { it.isNotBlank() && it != "#" }
                ?: element.attr("data-video").takeIf { it.isNotBlank() }
                ?: element.attr("data-src").takeIf { it.isNotBlank() }
                ?: element.attr("data-link")

            if (link != null) {
                if (!link.startsWith("http") && !link.startsWith("//") && link.length > 20) {
                    try { link = base64Decode(link) } catch (e: Exception) { }
                }
                if (link.isNotBlank() && link.startsWith("http")) {
                    loadExtractor(fixUrl(link), data, subtitleCallback, callback)
                }
            }
        }

        Regex("(https?:\\/\\/[^\"']+\\.(?:m3u8|mp4))").findAll(htmlRaw).forEach { match ->
            val vidUrl = match.groupValues[1]
            callback.invoke(
                newExtractorLink(
                    this.name,
                    this.name,
                    vidUrl,
                    type = if (vidUrl.contains(".m3u8")) INFER_TYPE else ExtractorLinkType.VIDEO
                ) { 
                    this.quality = Qualities.Unknown.value
                }
            )
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