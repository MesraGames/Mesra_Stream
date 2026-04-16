package com.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Document
import okhttp3.Interceptor
import okhttp3.Response

class NontonanimeindoProvider : MainAPI() {
    override var mainUrl = "https://nontonanimeindo.id"
    override var name = "NontonAnimeIndo"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse? {
        val document = app.get(mainUrl).document
        val home = mutableListOf<HomePageList>()

        // TODO: Sesuaikan selector CSS untuk section utama (Latest Update, Popular, dll)
        document.select("div.post-show").forEach { block ->
            val title = block.selectFirst("div.title-section h2")?.text() ?: "Terbaru"
            val items = block.select("article.post-item").mapNotNull {
                it.toSearchResult()
            }
            if (items.isNotEmpty()) home.add(HomePageList(title, items))
        }

        return newHomePageResponse(home, false)
    }

    private fun org.jsoup.nodes.Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2.entry-title, h3.title")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // TODO: Cek apakah query parameter pencarian sudah benar (?s=)
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("article.post-item, div.bs").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title")?.text() ?: ""
        val poster = document.selectFirst("div.thumb img")?.attr("src")
        val plot = document.selectFirst("div.entry-content p, div.description")?.text()

        // Logika pengecekan apakah ini Movie atau Series berdasarkan list episode
        val isMovie = document.select("div.eplister li, ul.episodelist li").isEmpty()

        return if (isMovie) {
            newMovieLoadResponse(title, url, TvType.AnimeMovie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            val episodes = document.select("div.eplister li, ul.episodelist li").mapNotNull {
                val a = it.selectFirst("a") ?: return@mapNotNull null
                val href = a.attr("href")
                val name = it.select(".epl-num").text().ifEmpty { it.text() }
                Episode(href, name)
            }.reversed()

            newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isDataJob: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        // TODO: Ekstrak link dari iframe atau tombol server
        document.select("select.mirror option, ul.m3u8-server li, div.player-embed iframe").forEach { 
            val rawUrl = it.attr("value").ifEmpty { it.attr("src") }
            if (rawUrl.isNotEmpty()) {
                // Jika link di-encode base64, gunakan base64Decode(rawUrl)
                val finalUrl = if (rawUrl.startsWith("http")) rawUrl else "https:$rawUrl"
                loadExtractor(finalUrl, data, subtitleCallback, callback)
            }
        }

        return true
    }
}

class NontonAnimeIndoInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://nontonanimeindo.id/")
            .build()
        return chain.proceed(request)
    }
}