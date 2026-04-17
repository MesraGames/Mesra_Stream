package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

/* =========================================================================
   PROVIDER: Nontonanimeindo
   STYLE: Modular, Klikxxi-inspired logic
   ========================================================================= */
class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "Nontonanimeindo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    override val mainPage = mainPageOf(
        "$mainUrl/anime-terbaru/" to "Anime Terbaru",
        "$mainUrl/anime-ongoing/" to "Anime Ongoing",
        "$mainUrl/movie/" to "Anime Movie"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page <= 1) request.data else "${request.data}page/$page/"
        val document = app.get(url).document
        val home = document.select(".listupd .bs, .listupd .article").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select(".listupd .bs").mapNotNull {
            it.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst(".tt, h2")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixPoster(this.selectFirst("img")?.attr("src") ?: this.selectFirst("img")?.attr("data-src"))
        val quality = this.selectFirst(".typez")?.text() ?: ""
        
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
            addQuality(quality)
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title")?.text()?.replace("Subtitle Indonesia", "")?.trim() ?: ""
        val poster = fixPoster(document.selectFirst(".thumb img")?.attr("src"))
        val plot = document.selectFirst(".entry-content p")?.text()
        
        val episodes = document.select(".eplister li").mapNotNull {
            val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val num = it.selectFirst(".epl-num")?.text() ?: ""
            newEpisode(link) {
                this.name = "Episode $num"
                this.episode = num.toIntOrNull()
            }
        }.reversed()

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = plot
            this.episodes = episodes
        }
    }

    override suspend fun loadLinks(data: String, isCdn: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select("select.mirror option").forEach {
            val iframeBase64 = it.attr("value")
            // Website ini sering menggunakan base64 di value option
            val decodedIframe = getIframeAttr(iframeBase64)
            if (decodedIframe.isNotEmpty()) {
                loadExtractor(decodedIframe, "$mainUrl/", subtitleCallback, callback)
            }
        }
        
        // Fallback ke iframe biasa jika tidak ada di select
        document.select("iframe").forEach {
            val src = it.attr("src")
            if (src.isNotEmpty()) loadExtractor(src, "$mainUrl/", subtitleCallback, callback)
        }
        
        return true
    }

    /* ======================= HELPER FUNCTIONS ======================= */
    private fun fixPoster(url: String?): String? {
        if (url == null) return null
        if (url.startsWith("data:image")) return null
        return fixUrl(url)
    }

    private fun getIframeAttr(data: String): String {
        return try {
            if (data.startsWith("http")) data
            else base64Decode(data)
        } catch (e: Exception) {
            ""
        }
    }
}