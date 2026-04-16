package com.rebahinn

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*

class RebahinnExtractor : ExtractorApi() {
    override var name = "Rebahinn"
    override var mainUrl = "https://www.rebahinn.net"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        // Logika untuk mengekstrak direct link jika web menggunakan player custom
        val response = app.get(url, referer = referer).text
        
        // Regex sederhana untuk mencari file .m3u8 atau .mp4 di script
        val videoUrl = "(https?://.*?\\.(?:m3u8|mp4))".toRegex().find(response)?.value

        videoUrl?.let {
            val isM3u8 = it.contains(".m3u8")
            val type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
            
            // Menggunakan format newExtractorLink sesuai permintaan
            callback.invoke(newExtractorLink(this.name, this.name, it, type) {
                this.referer = referer ?: ""
                this.quality = Qualities.Unknown.value
            })
        }
    }
}