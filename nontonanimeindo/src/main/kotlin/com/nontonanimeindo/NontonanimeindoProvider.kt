package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.extractors.VidStack

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.TvSeries, TvType.AsianDrama)

    override val mainPage = mainPageOf(
        "$mainUrl/anime-terbaru/page/" to "Anime Terbaru",
        "$mainUrl/anime-movie/page/" to "Movie",
        "$mainUrl/daftar-anime-lengkap/page/" to "Daftar Anime"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.animepost, article.bs, article.item, div.post-item, div.result-item, div.item, div.venz, .video-block").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, h3, h4, .title, .tt h4, .post-title, [itemprop=name]")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = this.selectFirst("img")?.fixPoster()
        return newAnimeSearchResponse(title, href, TvType.Anime) { this.posterUrl = posterUrl }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title, .entry-title, .title, [itemprop=name]")?.text()?.trim() ?: ""
        val poster = document.selectFirst("div.thumb img, .poster img, img[itemprop=image]")?.fixPoster()
        val description = document.selectFirst("div.entry-content, .description, .entry-content p, [itemprop=description]")?.text()
        
        val episodes = document.select("div.eplister li, div.episodelist ul li, ul.episodes li, .ep-item").mapNotNull {
            val href = fixUrl(it.selectFirst("a")?.attr("href") ?: return@mapNotNull null)
            val name = it.selectFirst(".epl-num, .ep-num, .num")?.text() ?: it.selectFirst("a")?.text() ?: "Episode"
            newEpisode(href) { this.name = name }
        }.reversed()

        return newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select("ul#player-list a, .server-list a, .mirror_link a, .btn-server, .list-server li").forEach {
            val baseUrl = it.attr("data-url").takeIf { d -> d.isNotEmpty() } ?: it.attr("href")
            if (baseUrl.startsWith("http")) {
                loadExtractor(fixUrl(baseUrl), subtitleCallback, callback)
            }
        }
        document.select("iframe, .video-content iframe").forEach {
            val src = it.getIframeAttr() ?: return@forEach
            loadExtractor(fixUrl(src), subtitleCallback, callback)
        }
        return true
    }

    private fun Element?.fixPoster(): String? {
        if (this == null) return null
        val dataSrc = this.attr("data-lazy-src").takeIf { it.isNotEmpty() } ?: this.attr("data-src").takeIf { it.isNotEmpty() } ?: this.attr("src")
        if (dataSrc.isBlank()) return null
        return fixUrl(dataSrc).replace(Regex("-\\d+x\\d+(?=\\.(webp|jpg|jpeg|png))"), "")
    }

    private fun Element?.getIframeAttr(): String? = this?.attr("data-litespeed-src").takeIf { !it.isNullOrEmpty() } ?: this?.attr("src")
}