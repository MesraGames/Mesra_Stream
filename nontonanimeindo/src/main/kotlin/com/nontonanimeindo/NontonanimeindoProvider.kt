package com.nontonanimeindo

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addEpisodes

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)
    override var lang = "id"
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        "$mainUrl/anime-terbaru/" to "Anime Terbaru",
        "$mainUrl/movie-terbaru/" to "Movie Terbaru",
        "$mainUrl/popular/" to "Populer"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(request.data + if (page > 1) "page/$page/" else "").document
        // Selector: .listupd article atau .post-show .clatest
        val home = doc.select(".listupd article, .post-show article").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, .entry-title")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")
        val type = if (href.contains("/movie/")) TvType.AnimeMovie else TvType.Anime

        return newAnimeSearchResponse(title, href, type) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select(".listupd article").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst(".entry-title")?.text() ?: ""
        val poster = doc.selectFirst(".thumb img")?.attr("src")
        val description = doc.selectFirst(".entry-content p")?.text()
        
        val isMovie = url.contains("/movie/")
        val type = if (isMovie) TvType.AnimeMovie else TvType.Anime

        if (isMovie) {
            return newMovieLoadResponse(title, url, TvType.AnimeMovie, url) {
                this.posterUrl = poster
                this.plot = description
            }
        } else {
            // Selector Episode: .eplister li atau .listeps li
            val episodes = doc.select(".eplister li, .listeps li").mapNotNull {
                val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst(".epl-num, .eps")?.text() ?: ""
                newEpisode(link) {
                    this.name = name
                    this.episode = name.filter { char -> char.isDigit() }.toIntOrNull()
                }
            }.reversed()

            return newAnimeLoadResponse(title, url, type) {
                this.posterUrl = poster
                this.plot = description
                addEpisodes(TvType.Anime, episodes)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        // Selector Iframe: .video-content iframe atau .player-embed iframe
        doc.select("select.mirror option, .mirror-item a").forEach {
            val embedData = it.attr("value").ifBlank { it.attr("data-link") }
            if (embedData.isNotEmpty()) {
                // Decode base64 jika diperlukan, atau langsung load
                val iframeUrl = if (embedData.startsWith("http")) embedData else "" // Logika extra jika butuh base64 decode
                if (iframeUrl.isNotEmpty()) {
                    loadExtractor(iframeUrl, data, subtitleCallback, callback)
                }
            }
        }
        
        doc.select("iframe[src]").forEach {
            val src = it.attr("src")
            loadExtractor(src, data, subtitleCallback, callback)
        }

        return true
    }
}