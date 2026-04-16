package com.hexated

<<<<<<< HEAD
import android.util.Log
=======
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addImdbId
import com.lagradost.cloudstream3.LoadResponse.Companion.addTMDbId
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchQuality
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SearchResponseList
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.USER_AGENT
import com.lagradost.cloudstream3.addDate
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.getQualityFromString
import com.lagradost.cloudstream3.mainPageOf
<<<<<<< HEAD
import com.lagradost.cloudstream3.network.WebViewResolver
=======
import com.lagradost.cloudstream3.network.CloudflareKiller
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.toNewSearchResponseList
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
<<<<<<< HEAD
import org.json.JSONObject
=======
import org.jsoup.nodes.Document
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
import java.security.MessageDigest
import java.text.Normalizer

class IdlixProvider : MainAPI() {
    override var mainUrl = base64Decode("aHR0cHM6Ly96MS5pZGxpeGt1LmNvbQ==")
<<<<<<< HEAD
    override var name = "Idlix🐍"
=======
    override var name = "Idlix"
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
    override val hasMainPage = true
    override var lang = "id"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime,
        TvType.AsianDrama
    )
<<<<<<< HEAD
=======
    private val cloudflareInterceptor by lazy { CloudflareKiller() }
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c

    override val mainPage = mainPageOf(
        "$mainUrl/api/movies?page=%d&limit=36&sort=createdAt" to "Movie Terbaru",
        "$mainUrl/api/series?page=%d&limit=36&sort=createdAt" to "TV Series Terbaru",
        "$mainUrl/api/browse?page=%d&limit=36&sort=latest&network=prime-video" to "Amazon Prime",
        "$mainUrl/api/browse?page=%d&limit=36&sort=latest&network=apple-tv-plus" to "Apple TV+",
        "$mainUrl/api/browse?page=%d&limit=36&sort=latest&network=disney-plus" to "Disney+",
        "$mainUrl/api/browse?page=%d&limit=36&sort=latest&network=hbo" to "HBO",
        "$mainUrl/api/browse?page=%d&limit=36&sort=latest&network=netflix" to "Netflix",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val url = if (request.data.contains("%d")) request.data.format(page) else request.data
<<<<<<< HEAD
        val res = app.get(url, timeout = 10000L).parsedSafe<ApiResponse>() ?: return newHomePageResponse(request.name, emptyList())
=======
        warmupSession()
        val res = app.get(url, timeout = 10000L, interceptor = cloudflareInterceptor).parsedSafe<ApiResponse>()
            ?: return newHomePageResponse(request.name, emptyList())
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        val home = res.data.map { item ->
            val title = item.title ?: "UnKnown"
            val poster = item.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
            if (item.contentType == "movie") {
                val movieurl = "$mainUrl/api/movies/${item.slug}"
                newMovieSearchResponse(title, movieurl, TvType.Movie) {
                    this.posterUrl = poster
                    this.year = item.releaseDate?.substringBefore("-")?.toIntOrNull()
                    this.quality = getSearchQuality(item.quality)
                    this.score = Score.from10(item.voteAverage)
                }
            } else {
                val seriesurl = "$mainUrl/api/series/${item.slug}"
                newTvSeriesSearchResponse(title, seriesurl, TvType.TvSeries) {
                    this.posterUrl = poster
                    this.year = item.releaseDate?.substringBefore("-")?.toIntOrNull()
                    this.score = Score.from10(item.voteAverage)
                    this.quality = getSearchQuality(item.quality)
                }
            }
        }

        return newHomePageResponse(request.name, home)
    }

<<<<<<< HEAD
    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query,1)?.items

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val url = "$mainUrl/api/search?q=$query&page=$page&limit=8"
        val res = app.get(url).parsedSafe<SearchApiResponse>() ?: return null
=======
    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query, 1)?.items

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val url = "$mainUrl/api/search?q=$query&page=$page&limit=8"
        warmupSession()
        val res = app.get(url, interceptor = cloudflareInterceptor).parsedSafe<SearchApiResponse>() ?: return null
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        val items = res.results
        val results = items.mapNotNull { item ->
            val title = item.title
            val poster = item.posterPath.let { "https://image.tmdb.org/t/p/w342$it" }
            val year = (item.releaseDate ?: item.firstAirDate)?.substringBefore("-")?.toIntOrNull()

            val link = when (item.contentType) {
                "movie" -> "$mainUrl/api/movies/${item.slug}"
                "tv_series", "series" -> "$mainUrl/api/series/${item.slug}"
                else -> return@mapNotNull null
            }

            val rating = item.voteAverage

            if (item.contentType == "movie") {
                newMovieSearchResponse(title, link, TvType.Movie) {
                    this.posterUrl = poster
                    this.year = year
                    this.quality = getQualityFromString(item.quality)
                    this.score = rating.let { Score.from10(it) }
                }
            } else {
                newTvSeriesSearchResponse(title, link, TvType.TvSeries) {
                    this.posterUrl = poster
                    this.year = year
                    this.score = rating.let { Score.from10(it) }
                }
            }
        }

        return results.toNewSearchResponseList()
    }

    override suspend fun load(url: String): LoadResponse {
<<<<<<< HEAD
        val response = app.get(url, timeout = 10000L)
=======
        warmupSession()
        val response = app.get(url, timeout = 10000L, interceptor = cloudflareInterceptor)
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c

        val data = response.parsedSafe<DetailResponse>()
            ?: throw ErrorLoadingException("Invalid JSON")

        val title = data.title ?: "Unknown"
        val poster = data.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
        val backdrop = data.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

        val year = (data.releaseDate ?: data.firstAirDate)
            ?.substringBefore("-")
            ?.toIntOrNull()

        val tags = data.genres?.mapNotNull { it.name } ?: emptyList()
<<<<<<< HEAD
        val logourl = "https://image.tmdb.org/t/p/w500"+data.logoPath
=======
        val logourl = "https://image.tmdb.org/t/p/w500" + data.logoPath
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        val actors = data.cast?.map {
            Actor(it.name ?: "", it.profilePath?.let { p -> "https://image.tmdb.org/t/p/w185$p" })
        } ?: emptyList()

        val trailer = data.trailerUrl
        val rating = data.voteAverage

        val relatedUrl = if (data.seasons != null) {
            "$mainUrl/api/series/${data.slug}/related"
        } else {
            "$mainUrl/api/movies/${data.slug}/related"
        }

        val recommendations = try {
<<<<<<< HEAD
            app.get(relatedUrl, referer = mainUrl)
                .parsedSafe<ApiResponse>()?.data?.mapNotNull { item ->

                    val title = item.title ?: return@mapNotNull null
                    val poster = item.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
=======
            app.get(relatedUrl, referer = mainUrl, interceptor = cloudflareInterceptor)
                .parsedSafe<ApiResponse>()?.data?.mapNotNull { item ->
                    val recTitle = item.title ?: return@mapNotNull null
                    val recPoster = item.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c

                    val link = if (item.contentType == "movie") {
                        "$mainUrl/api/movies/${item.slug}"
                    } else {
                        "$mainUrl/api/series/${item.slug}"
                    }

                    if (item.contentType == "movie") {
<<<<<<< HEAD
                        newMovieSearchResponse(title, link, TvType.Movie) {
                            this.posterUrl = poster
=======
                        newMovieSearchResponse(recTitle, link, TvType.Movie) {
                            this.posterUrl = recPoster
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
                            this.year = (item.releaseDate ?: item.firstAirDate)
                                ?.substringBefore("-")
                                ?.toIntOrNull()
                        }
                    } else {
<<<<<<< HEAD
                        newTvSeriesSearchResponse(title, link, TvType.TvSeries) {
                            this.posterUrl = poster
=======
                        newTvSeriesSearchResponse(recTitle, link, TvType.TvSeries) {
                            this.posterUrl = recPoster
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
                            this.year = (item.releaseDate ?: item.firstAirDate)
                                ?.substringBefore("-")
                                ?.toIntOrNull()
                        }
                    }
<<<<<<< HEAD

                } ?: emptyList()

=======
                } ?: emptyList()
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        } catch (_: Exception) {
            emptyList()
        }

        return if (data.seasons != null) {
            val episodes = mutableListOf<Episode>()

            data.firstSeason?.episodes?.forEach { ep ->
                episodes.add(
<<<<<<< HEAD
                    newEpisode( LoadData(
                        id = ep.id ?: return@forEach,
                        type = "episode"
                    ).toJson()) {
=======
                    newEpisode(
                        LoadData(
                            id = ep.id ?: return@forEach,
                            type = "episode"
                        ).toJson()
                    ) {
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
                        this.name = ep.name
                        this.season = data.firstSeason.seasonNumber
                        this.episode = ep.episodeNumber
                        this.description = ep.overview
                        this.runTime = ep.runtime
                        this.score = Score.from10(ep.voteAverage?.toString())
                        addDate(ep.airDate)
                        this.posterUrl = ep.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" }
                    }
                )
            }

            data.seasons.forEach { season ->
                val seasonNum = season.seasonNumber ?: return@forEach
                if (seasonNum == data.firstSeason?.seasonNumber) return@forEach
                val seasonUrl = "$mainUrl/api/series/${data.slug}/season/$seasonNum"

                val seasonData = try {
<<<<<<< HEAD
                    val res = app.get(seasonUrl, referer = mainUrl)
=======
                    val res = app.get(seasonUrl, referer = mainUrl, interceptor = cloudflareInterceptor)
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
                    res.parsedSafe<SeasonWrapper>()?.season
                } catch (_: Exception) {
                    null
                }

                seasonData?.episodes?.forEach { ep ->
                    episodes.add(
<<<<<<< HEAD
                        newEpisode( LoadData(
                            id = ep.id ?: return@forEach,
                            type = "episode"
                        ).toJson()) {
=======
                        newEpisode(
                            LoadData(
                                id = ep.id ?: return@forEach,
                                type = "episode"
                            ).toJson()
                        ) {
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
                            this.name = ep.name
                            this.season = seasonNum
                            this.episode = ep.episodeNumber
                            this.description = ep.overview
                            this.runTime = ep.runtime
                            this.score = Score.from10(ep.voteAverage?.toString())
                            addDate(ep.airDate)
                            this.posterUrl = ep.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" }
                        }
                    )
                }
            }

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.backgroundPosterUrl = backdrop
                this.logoUrl = logourl
                this.year = year
                this.plot = data.overview
                this.tags = tags
                this.score = Score.from10(rating?.toString())
                addActors(actors)
                addTrailer(trailer)
                addTMDbId(data.tmdbId)
                addImdbId(data.imdbId)
                this.recommendations = recommendations
            }
        } else {
<<<<<<< HEAD
            newMovieLoadResponse(title, url, TvType.Movie,  LoadData(
                id = data.id ?: "",
                type = "movie"
            ).toJson()) {
=======
            newMovieLoadResponse(
                title,
                url,
                TvType.Movie,
                LoadData(
                    id = data.id ?: "",
                    type = "movie"
                ).toJson()
            ) {
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
                this.posterUrl = poster
                this.backgroundPosterUrl = backdrop
                this.logoUrl = logourl
                this.year = year
                this.plot = data.overview
                this.tags = tags
                this.score = Score.from10(rating?.toString())
                addActors(actors)
                addTrailer(trailer)
                addTMDbId(data.tmdbId)
                addImdbId(data.imdbId)
                this.recommendations = recommendations
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
<<<<<<< HEAD

=======
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        val parsed = try {
            AppUtils.parseJson<LoadData>(data)
        } catch (_: Exception) {
            null
        } ?: return false

        val contentId = parsed.id
        val contentType = parsed.type
<<<<<<< HEAD

        val ts = System.currentTimeMillis()
        val aclrRes = app.get("$mainUrl/pagead/ad_frame.js?_=$ts").text
=======
        warmupSession()

        val ts = System.currentTimeMillis()
        val aclrRes = runCatching {
            app.get(
                "$mainUrl/pagead/ad_frame.js?_=$ts",
                referer = "$mainUrl/",
                interceptor = cloudflareInterceptor
            ).text
        }.getOrNull().orEmpty()
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        val aclr = Regex("""__aclr\s*=\s*"([a-f0-9]+)"""")
            .find(aclrRes)
            ?.groupValues?.getOrNull(1)

<<<<<<< HEAD

=======
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        val challengejson = """
{
    "contentType": "$contentType",
    "contentId": "$contentId"${if (aclr != null) ",\n    \"clearance\": \"$aclr\"" else ""}
}
""".trimIndent()

        val headers = mapOf(
            "accept" to "*/*",
            "content-type" to "application/json",
            "origin" to mainUrl,
            "referer" to mainUrl,
            "user-agent" to USER_AGENT,
        )

        val challengeRes = app.post(
            "$mainUrl/api/watch/challenge",
            requestBody = challengejson.toRequestBody("application/json".toMediaType()),
<<<<<<< HEAD
            headers = headers
=======
            headers = headers,
            referer = "$mainUrl/",
            interceptor = cloudflareInterceptor
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        ).parsedSafe<ChallengeResponse>() ?: return false

        val nonce = solvePow(
            challengeRes.challenge,
            challengeRes.difficulty
        )

        val solvejson = """
        {
        "challenge": "${challengeRes.challenge}",
        "signature": "${challengeRes.signature}",
        "nonce": $nonce
        }
        """.trimIndent()

        val solveRes = app.post(
            "$mainUrl/api/watch/solve",
            requestBody = solvejson.toRequestBody("application/json".toMediaType()),
            headers = mapOf(
                "accept" to "*/*",
                "content-type" to "application/json",
                "origin" to mainUrl,
                "referer" to mainUrl,
                "user-agent" to USER_AGENT,
            )
<<<<<<< HEAD
        ).text

        val json = JSONObject(solveRes)

        val embedUrl = when {
            json.has("embedUrl") -> json.optString("embedUrl")
            json.has("url") -> json.optString("url")
            else -> null
        } ?: return false

        val iframeurl = WebViewResolver(
            interceptUrl = Regex("""/video/"""),
            additionalUrls = listOf(Regex("""/video/""")),
            useOkhttp = false,
            timeout = 15_000L
        )

        val finalUrl = app.get("$mainUrl${embedUrl}", interceptor = iframeurl).url
        loadExtractor(finalUrl, mainUrl, subtitleCallback, callback)
        return true
    }

=======
            ,
            referer = "$mainUrl/",
            interceptor = cloudflareInterceptor
        ).parsedSafe<SolveResponse>() ?: return false
        val embedUrl = solveRes.embedUrl?.let { if (it.startsWith("http")) it else "$mainUrl$it" } ?: return false
        val finalUrl = unwrapIframeUrl(embedUrl) ?: return false
        if (finalUrl.contains("jeniusplay", true)) {
            Jeniusplay().getUrl(finalUrl, mainUrl, subtitleCallback, callback)
        } else {
            loadExtractor(finalUrl, mainUrl, subtitleCallback, callback)
        }
        return true
    }

    private suspend fun warmupSession() {
        runCatching {
            app.get(
                "$mainUrl/",
                referer = "$mainUrl/",
                interceptor = cloudflareInterceptor
            )
        }
        runCatching {
            app.get(
                "$mainUrl/api/homepage/ads/surface/preroll",
                referer = "$mainUrl/",
                interceptor = cloudflareInterceptor
            )
        }
    }

    private suspend fun unwrapIframeUrl(url: String): String? {
        var current = url
        repeat(5) {
            val response = app.get(
                current,
                referer = "$mainUrl/",
                interceptor = cloudflareInterceptor
            )
            val next = extractIframeUrl(response.document) ?: return current
            current = if (next.startsWith("http")) next else "$mainUrl$next"
        }
        return current
    }

    private fun extractIframeUrl(document: Document): String? {
        val iframe = document.selectFirst("iframe[src], iframe[data-src]") ?: return null
        return iframe.attr("src").ifBlank { iframe.attr("data-src") }.takeIf { it.isNotBlank() }
    }

>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
    fun solvePow(challenge: String, difficulty: Int): Int {
        val target = "0".repeat(difficulty)

        var nonce = 0
        while (true) {
            val hash = sha256(challenge + nonce)

            if (hash.startsWith(target)) {
                return nonce
            }

            nonce++
        }
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

fun getSearchQuality(check: String?): SearchQuality? {
    val s = check ?: return null
    val u = Normalizer.normalize(s, Normalizer.Form.NFKC).lowercase()
    val patterns = listOf(
        Regex("\\b(4k|ds4k|uhd|2160p)\\b", RegexOption.IGNORE_CASE) to SearchQuality.FourK,
<<<<<<< HEAD

        // CAM / THEATRE SOURCES FIRST
        Regex("\\b(hdts|hdcam|hdtc)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HdCam,
        Regex("\\b(camrip|cam[- ]?rip)\\b", RegexOption.IGNORE_CASE) to SearchQuality.CamRip,
        Regex("\\b(cam)\\b", RegexOption.IGNORE_CASE) to SearchQuality.Cam,

        // WEB / RIP
        Regex("\\b(web[- ]?dl|webrip|webdl)\\b", RegexOption.IGNORE_CASE) to SearchQuality.WebRip,

        // BLURAY
        Regex("\\b(bluray|bdrip|blu[- ]?ray)\\b", RegexOption.IGNORE_CASE) to SearchQuality.BlueRay,

        // RESOLUTIONS
        Regex("\\b(1440p|qhd)\\b", RegexOption.IGNORE_CASE) to SearchQuality.BlueRay,
        Regex("\\b(1080p|fullhd)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HD,
        Regex("\\b(720p)\\b", RegexOption.IGNORE_CASE) to SearchQuality.SD,

        // GENERIC HD LAST
        Regex("\\b(hdrip|hdtv)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HD,

=======
        Regex("\\b(hdts|hdcam|hdtc)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HdCam,
        Regex("\\b(camrip|cam[- ]?rip)\\b", RegexOption.IGNORE_CASE) to SearchQuality.CamRip,
        Regex("\\b(cam)\\b", RegexOption.IGNORE_CASE) to SearchQuality.Cam,
        Regex("\\b(web[- ]?dl|webrip|webdl)\\b", RegexOption.IGNORE_CASE) to SearchQuality.WebRip,
        Regex("\\b(bluray|bdrip|blu[- ]?ray)\\b", RegexOption.IGNORE_CASE) to SearchQuality.BlueRay,
        Regex("\\b(1440p|qhd)\\b", RegexOption.IGNORE_CASE) to SearchQuality.BlueRay,
        Regex("\\b(1080p|fullhd)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HD,
        Regex("\\b(720p)\\b", RegexOption.IGNORE_CASE) to SearchQuality.SD,
        Regex("\\b(hdrip|hdtv)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HD,
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
        Regex("\\b(dvd)\\b", RegexOption.IGNORE_CASE) to SearchQuality.DVD,
        Regex("\\b(hq)\\b", RegexOption.IGNORE_CASE) to SearchQuality.HQ,
        Regex("\\b(rip)\\b", RegexOption.IGNORE_CASE) to SearchQuality.CamRip
    )

<<<<<<< HEAD

=======
>>>>>>> 7ff19bb0652272b395e3f987a630dd3b09b3e32c
    for ((regex, quality) in patterns) if (regex.containsMatchIn(u)) return quality
    return null
}
