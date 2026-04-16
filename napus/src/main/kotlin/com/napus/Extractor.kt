package com.napus

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*

class NapusExtractor : ExtractorApi() {
    override var name = "Napus"
    override var mainUrl = "https://napus.org"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        // Logika ekstraksi video dari Napus (Contoh: Menangani M3U8)
        val isM3u8 = url.contains(".m3u8")
        val type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO

        // WAJIB menggunakan newExtractorLink dengan lambda
        callback.invoke(newExtractorLink(this.name, this.name, url, type) {
            this.referer = referer ?: ""
            this.quality = Qualities.Unknown.value
        })
    }
}