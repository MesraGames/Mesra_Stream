package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.LoadResponse.Companion.addAniListId
import org.jsoup.nodes.Element

class NontonanimeindoProvider : MainAPI() {
    /* ======================= Variables ======================= */
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    /* ======================= Main Page & Search ======================= */
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val sections = document.select(".post-show").mapNotNull {
            val title = it.selectFirst(".title-section")?.text() ?: "Update Terbaru"
            val items = it.select("article").mapNotNull {
                it.toSearchResult()
            }
            HomePageList(title, items)
        }
        return HomePageResponse(sections, false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select(".listupd article").mapNotNull {
            it.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst(".title, .tt")?.text()?.trim() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img").fixPoster()

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    /* ======================= Load Details & Episodes ======================= */
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title")?.text()?.trim() ?: ""
        val poster = document.selectFirst(".thumb img").fixPoster()
        val plot = document.selectFirst(".entry-content p")?.text()
        
        val episodes = document.select(".episodelist ul li").mapNotNull {
            val epTitle = it.selectFirst(".epxtitle")?.text() ?: ""
            val epHref = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val date = it.selectFirst(".epxdate")?.text()
            Episode(epHref, epTitle, date = date)
        }.reversed()

        return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = poster
            this.plot = plot
        }
    }

    /* ======================= Links & Extractors ======================= */
    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        
        document.select(".mirroroption select option").forEach { 
            val base64Data = it.attr("value")
            if (base64Data.isNotEmpty()) {
                val decodedIframe = base64Data.base64Decode()
                val iframeUrl = Regex("src=\"([^\"]+)\"").find(decodedIframe)?.groupValues?.get(1)
                
                if (iframeUrl != null) {
                    loadExtractor(iframeUrl, data, subtitleCallback, callback)
                }
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

    private fun String.base64Decode(): String {
        return try {
            android.util.Base64.decode(this, android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}