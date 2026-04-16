package com.nontonanimeindo

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.extractors.Vidstack
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.Filemoon

class NontonAnimeIndoExtractor : ExtractorApi() {
    override val name = "NontonAnimeIndo Internal"
    override val mainUrl = "https://nontonanimeindo.id"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val response = app.get(url, referer = referer).text
        
        // Regex untuk mencari source m3u8 di dalam tag script
        val m3u8Regex = Regex("(file|src|url)\\s*:\\s*\"(.*?\\.m3u8.*?)\"")
        m3u8Regex.findAll(response).forEach { match ->
            val link = match.groupValues[2]
            callback.invoke(
                ExtractorLink(
                    name,
                    name,
                    link,
                    mainUrl,
                    Qualities.Unknown.value,
                    isM3u8 = true
                )
            )
        }

        // Regex untuk mencari subtitle vtt/srt
        val subRegex = Regex("\\"?label\\"?\\s*:\\s*\\"(.*?)\\".*?\\"?file\\"?\\s*:\\s*\\"(.*?\\.(vtt|srt).*?)\\"")
        subRegex.findAll(response).forEach { match ->
            subtitleCallback.invoke(
                SubtitleFile(match.groupValues[1], match.groupValues[2])
            )
        }
    }
}

class AlternativeVidHide : StreamWishExtractor() {
    override val name = "VidHide"
    override val mainUrl = "https://vidhidepro.com"
}

class AlternativeFilemoon : Filemoon() {
    override val name = "Filemoon-NAI"
}