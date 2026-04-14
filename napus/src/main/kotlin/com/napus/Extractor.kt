package com.napus

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*

class NapusExtractor : ExtractorApi() {
    override var name = "Napus"
    override var mainUrl = "https://napus.org"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        // Logika untuk mengekstrak direct link jika Napus memiliki player sendiri
        // Jika hanya sebagai portal, biarkan loadExtractor di Provider yang bekerja mencari host yang didukung
        val doc = app.get(url, referer = referer).document
        val videoUrl = doc.selectFirst("source")?.attr("src") ?: ""
        
        if (videoUrl.isNotEmpty()) {
            callback.invoke(
                newExtractorLink(
                    this.name,
                    this.name,
                    videoUrl,
                    referer ?: "",
                    Qualities.Unknown.value,
                    videoUrl.contains(".m3u8")
                )
            )
        }
    }
}