package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import android.util.Base64
import java.nio.charset.StandardCharsets

class NontonAnimeIndoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("article.animepost").map {
            val title = it.selectFirst("h3")?.text() ?: ""
            val href = it.selectFirst("a")?.attr("href") ?: ""
            val poster = it.selectFirst("img")?.getImageAttr() ?: ""
            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1.entry-title")?.text() ?: ""
        val poster = doc.selectFirst("div.thumb img")?.getImageAttr() ?: doc.selectFirst("meta[property=og:image]")?.attr("content")
        
        // Memperbaiki fungsi newEpisode yang deprecated
        val episodes = doc.select("div.episodelist ul li").mapNotNull {
            val name = it.selectFirst("span.eps a")?.text() ?: ""
            val href = it.selectFirst("span.eps a")?.attr("href") ?: return@mapNotNull null
            newEpisode(href) {
                this.name = name
            }
        }.reversed()

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            // Memperbaiki error MutableMap saat memasukkan daftar episode
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    // Memperbaiki error signature overrides nothing dengan menambahkan subtitleCallback
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        
        // 1. Ekstraksi agresif dari elemen UI
        doc.select("ul#server_list li div, .player-embed iframe, div[data-link], li[data-video]").forEach { el ->
            val link = el.attr("data-link").ifEmpty { el.attr("data-src") }.ifEmpty { el.attr("data-video") }.ifEmpty { el.attr("src") }
            if (link.isNotEmpty()) {
                processVideoUrl(link, subtitleCallback, callback)
            }
        }

        // 2. Ekstraksi agresif dari Script block
        doc.select("script").forEach { script ->
            val content = script.data()
            if (content.contains("base64") || content.contains("eval")) {
                // Memperbaiki Regex Syntax Error pada Base64
                Regex("\"([A-Za-z0-9+/]{40,}=*)\"").findAll(content).forEach { match ->
                    try {
                        val decoded = String(Base64.decode(match.groupValues[1], Base64.DEFAULT), StandardCharsets.UTF_8)
                        if (decoded.contains("http")) processVideoUrl(decoded, subtitleCallback, callback)
                    } catch (e: Exception) { }
                }
            }
            
            // Scrape direct video sources
            Regex("(?i)(?:file|hls|src|url)\\s*[:=]\\s*[\"']([^\"']+\\.(?:m3u8|mp4)[^\"']*)[\"']").findAll(content).forEach { match ->
                val videoUrl = match.groupValues[1]
                try { 
                    // Memperbaiki ExtractorLink constructor yang deprecated ke newExtractorLink
                    callback.invoke(
                        newExtractorLink(
                            "Internal Player", 
                            "High Speed", 
                            videoUrl, 
                            "", 
                            Qualities.Unknown.value, 
                            videoUrl.contains("m3u8")
                        )
                    )
                } catch (e: Exception) { }
            }
        }
        return true
    }

    // Parameter di-update untuk menerima subtitleCallback
    private suspend fun processVideoUrl(url: String, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val cleanUrl = if (url.startsWith("//")) "https:$url" else url
        loadExtractor(cleanUrl, mainUrl, subtitleCallback, callback)
    }

    private fun Element.getImageAttr(): String? {
        return this.attr("data-src").ifEmpty { this.attr("src") }.ifEmpty { this.attr("data-lazy-src") }
    }
}