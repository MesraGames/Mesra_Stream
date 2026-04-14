package com.lagradost.cloudstream3.plugins.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import org.jsoup.nodes.Element
import android.util.Base64

class NontonAnimeIndoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse? {
        val document = app.get(mainUrl).document
        val home = mutableListOf<HomePageList>()

        // TODO: Sesuaikan selector CSS untuk section beranda
        document.select("div.listupd").forEach { block ->
            val title = block.selectFirst("h2, h3")?.text() ?: "Update Terbaru"
            val anime = block.select("article").mapNotNull { it.toSearchResult() }
            if (anime.isNotEmpty()) home.add(HomePageList(title, anime))
        }
        return HomePageResponse(home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("article").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val isEpisode = url.contains("/episode/")
        
        val title = document.selectFirst("h1.entry-title")?.text() ?: ""
        val poster = document.selectFirst("div.thumb img")?.attr("src")
        val plot = document.selectFirst("div.entry-content")?.text()

        return if (isEpisode) {
            val mainPage = document.selectFirst("div.all-episode a")?.attr("href")
            newAnimeLoadResponse(title, url, TvType.Anime) {
                this.posterUrl = poster
                this.plot = plot
                // Logika Episode tunggal
                this.episodes = listOf(Episode(url, "Episode Sekarang"))
            }
        } else {
            val episodes = document.select("div.eplister ul li").mapNotNull {
                val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst(".epl-num")?.text() ?: ""
                Episode(link, "Episode $name")
            }.reversed()
            newAnimeLoadResponse(title, url, TvType.Anime) {
                this.posterUrl = poster
                this.plot = plot
                this.episodes = episodes
            }
        }
    }

    override suspend fun loadLinks(data: String, isDataJob: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        
        // TODO: Cari semua iframe atau source m3u8
        document.select("select.mirroroption option, ul.mirror-nav li").forEach { 
            val rawData = it.attr("value").ifBlank { it.attr("data-url") }
            if (rawData.isNotEmpty()) {
                val decodedUrl = if (rawData.startsWith("http")) rawData else base64Decode(rawData)
                loadExtractor(decodedUrl, "$mainUrl/", subtitleCallback, callback)
            }
        }
        
        // Regex fallback untuk mencari link di script
        val script = document.select("script").html()
        val regex = "\"file\"\\s*:\\s*\"(http[^\"]+)\"".toRegex()
        regex.findAll(script).forEach { match ->
            val videoUrl = match.groupValues[1]
            callback.invoke(ExtractorLink(this.name, "Original", videoUrl, "", getQualityFromName(""), videoUrl.contains("m3u8")))
        }

        return true
    }

    private fun base64Decode(input: String): String {
        return try { String(Base64.decode(input, Base64.DEFAULT)) } catch (e: Exception) { "" }
    }
}