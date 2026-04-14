package com.napus

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element
import okhttp3.Interceptor
import okhttp3.Response

class NapusProvider : MainAPI() {
    override var mainUrl = "https://napus.org"
    override var name = "Napus"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "/" to "Beranda",
        "/movies/" to "Film Terbaru",
        "/tvshows/" to "Serial TV",
        "/genre/anime/" to "Anime"
    )

    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse {
        val doc = app.get("$mainUrl${request.data}page/$page/").document
        // TODO: Sesuaikan selector item list (biasanya .items .item atau article)
        val home = doc.select("div.items > article, div.list-update > div.ue-item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3 > a, .entry-title a")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrl(this.selectFirst("img")?.attr("src") ?: "")
        val quality = this.selectFirst(".quality")?.text() ?: ""

        return if (this.selectFirst(".typepost")?.text()?.contains("TV", true) == true) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                this.quality = getQualityFromString(quality)
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                this.quality = getQualityFromString(quality)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("div.result-item, article").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .entry-title")?.text() ?: return null
        val poster = fixUrl(doc.selectFirst(".poster img, .thumb img")?.attr("src") ?: "")
        val description = doc.selectFirst(".wp-content p, .description")?.text()
        val type = if (url.contains("/tvshows/") || doc.selectFirst("#list-episodes") != null) TvType.TvSeries else TvType.Movie

        if (type == TvType.TvSeries) {
            val episodes = doc.select("ul.episodios li, .se-c .episodios li").mapNotNull {
                val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val name = it.selectFirst(".episodiotitle a")?.text() ?: "Episode"
                val season = it.parents().select(".title").firstOrNull()?.text()?.filter { char -> char.isDigit() }?.toIntOrNull() ?: 1
                val episode = it.selectFirst(".numerando")?.text()?.split("-")?.lastOrNull()?.trim()?.toIntOrNull()
                Episode(href, name, season, episode)
            }
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = description
            }
        } else {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = description
            }
        }
    }

    override suspend fun loadLinks(data: String, isDataJob: Boolean, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        // TODO: Cari iframe atau source player (biasanya di id 'player-option' atau script tags)
        doc.select("iframe, .dooplay_player_option, source").forEach { element ->
            var link = element.attr("src").ifEmpty { element.attr("data-url") }
            if (link.startsWith("//")) link = "https:$link"
            
            if (link.isNotEmpty()) {
                loadExtractor(link, data, callback)
            }
        }
        
        // Regex Agresif untuk link tersembunyi di Script
        val script = doc.select("script").html()
        val regex = "\"(?:https?:)?\\/\\/[^\\"\\s>]+".toRegex()
        regex.findAll(script).forEach { match ->
            val rawUrl = match.value.replace("\\/", "/").replace("\"", "")
            if (rawUrl.contains("google") || rawUrl.contains("m3u8") || rawUrl.contains("mp4")) {
                loadExtractor(rawUrl, data, callback)
            }
        }
        return true
    }
}

class NapusInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://napus.org/")
            .build()
        return chain.proceed(request)
    }
}