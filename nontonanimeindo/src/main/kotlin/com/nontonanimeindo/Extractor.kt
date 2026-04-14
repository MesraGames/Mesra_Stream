package com.lagradost.cloudstream3.plugins.nontonanimeindo

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*

// Extractor Bawaan
class StreamWishCustom : StreamWish() {
    override val name = "StreamWish"
    override val mainUrl = "https://streamwish.to"
}

class FilemoonCustom : Filemoon() {
    override val name = "Filemoon"
    override val mainUrl = "https://filemoon.sx"
}

// Extractor Kustom Khusus NontonAnimeIndo
class NontonAnimeIndoExtractor : ExtractorApi() {
    override val name = "NontonAnimeIndo Direct"
    override val mainUrl = "https://nontonanimeindo.id"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val response = app.get(url, referer = referer).text
        
        // TODO: Regex untuk mencari m3u8 di dalam player
        val m3u8Regex = "(http[^\\"']+\\.m3u8[^\\"']*)".toRegex()
        m3u8Regex.findAll(response).forEach { match ->
            val videoUrl = match.groupValues[1].replace("\\", "")
            callback.invoke(
                ExtractorLink(
                    this.name,
                    "HLS",
                    videoUrl,
                    referer ?: "",
                    getQualityFromName("Auto"),
                    true
                )
            )
        }

        // Ekstrak Subtitle jika ada di script
        val subRegex = "(?:vtt|srt)\\s*:\\s*\\"(http[^\"]+)\\"".toRegex()
        subRegex.findAll(response).forEach { match ->
            subtitleCallback.invoke(SubtitleFile("Indonesian", match.groupValues[1]))
        }
    }
}