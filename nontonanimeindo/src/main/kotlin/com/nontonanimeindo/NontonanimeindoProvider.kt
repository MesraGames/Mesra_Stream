package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addAniListId
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.nodes.Element

class NontonAnimeIndo : MainAPI() {
    /* =========================================================================== */
    /*                                  PROPERTIES                                 */
    /* =========================================================================== */
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    override val mainPage = mainPageOf(
        "/" to "Update Terbaru",
        "/anime-terpopuler/" to "Populer",
        "/daftar-anime-movie/" to "Anime Movie"
    )

    /* =========================================================================== */
    /*                               CORE FUNCTIONS                                */
    /* =========================================================================== */
    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse {
        val doc = app.get("$mainUrl${request.data}page/$page/").document
        val home = doc.select("article.animpost").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("article.animpost").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h4")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val poster = fixPoster(this.selectFirst("img")?.attr("src") ?: "")
        val quality = this.selectFirst(".mvic-quality")?.text() ?: ""

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = poster
            addQuality(quality)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1.entry-title")?.text() ?: ""
        val poster = fixPoster(doc.selectFirst(".thumb img")?.attr("src") ?: "")
        val description = doc.selectFirst(".entry-content p")?.text()

        // Cek apakah ini halaman series atau episode tunggal
        val isEpisode = url.contains("/episode/")
        val episodes = if (isEpisode) {
            listOf(newEpisode(url) { 
                this.name = "Episode 1"
                this.episode = 1
            })
        } else {
            doc.select(".list-episode li, .eplister li").mapNotNull { li ->
                val epHref = li.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val epTitle = li.selectFirst("a")?.text() ?: ""
                val epNum = Regex("(?:E|Episode|Ep)\\s*(\\d+)").find(epTitle)?.groupValues?.get(1)?.toIntOrNull()
                
                newEpisode(epHref) {
                    this.name = epTitle
                    this.episode = epNum
                }
            }.reversed()
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            addEpisodes(if (isEpisode) NavType.OnlyEpisodes else NavType.Normal, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isDataJob: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        // Cari semua iframe di dalam player
        doc.select(".player-embed iframe, #player_embed iframe, .video-content iframe").forEach { iframe ->
            val src = getIframeAttr(iframe)
            if (src.isNotEmpty()) {
                loadExtractor(src, "$mainUrl/", subtitleCallback, callback)
            }
        }
        return true
    }

    /* =========================================================================== */
    /*                               HELPER FUNCTIONS                              */
    /* =========================================================================== */
    private fun fixPoster(url: String): String {
        if (url.isEmpty()) return ""
        return if (url.startsWith("//")) "https:$url" else url
    }

    private fun getIframeAttr(element: Element): String {
        return element.attr("src").takeIf { it.isNotEmpty() } 
            ?: element.attr("data-src").takeIf { it.isNotEmpty() } 
            ?: ""
    }
}