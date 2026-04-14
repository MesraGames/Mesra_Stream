package com.napus

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.SubtitleFile

class NapusRawExtractor : ExtractorApi() {
    override val name = "Napus Direct"
    override val mainUrl = "https://napus.org"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit): List<ExtractorLink> {
        val response = app.get(url, referer = referer).text
        val sources = mutableListOf<ExtractorLink>()

        // Regex untuk mencari file m3u8 atau mp4 dalam file/sources JS
        val m3u8Regex = "(https?.*?\\.m3u8)".toRegex()
        val mp4Regex = "(https?.*?\\.mp4)".toRegex()

        m3u8Regex.findAll(response).forEach { match ->
            sources.add(ExtractorLink(name, "Napus HLS", match.value, referer ?: "", Qualities.Unknown.value, true))
        }

        mp4Regex.findAll(response).forEach { match ->
            sources.add(ExtractorLink(name, "Napus MP4", match.value, referer ?: "", Qualities.Unknown.value, false))
        }

        return sources
    }
}

class StreamWishNapus : com.lagradost.cloudstream3.extractors.StreamWish() {
    override val name = "StreamWish Napus"
    override val mainUrl = "https://streamwish.to"
}

class FilemoonNapus : com.lagradost.cloudstream3.extractors.Filemoon() {
    override val name = "Filemoon Napus"
    override val mainUrl = "https://filemoon.sx"
}

class VidmolyNapus : com.lagradost.cloudstream3.extractors.Vidmoly() {
    override val name = "Vidmoly Napus"
    override val mainUrl = "https://vidmoly.to"
}