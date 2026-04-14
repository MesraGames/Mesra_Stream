package com.napus
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*

class NapusExtractor : ExtractorApi() {
    override var name = "Napus"
    override var mainUrl = "https://napus.org"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        // Logika ekstraksi manual jika diperlukan, di sini kita asumsikan link langsung bisa diputar atau dihandle oleh player internal
        callback.invoke(ExtractorLink(this.name, this.name, url, referer ?: "", Qualities.Unknown.value, url.contains(".m3u8")))
    }
}

// Extractor tambahan yang sering digunakan oleh situs movie Indonesia
class StreamWishExtractor : StreamWish() {
    override var mainUrl = "https://streamwish.to"
}

class VidhideExtractor : Vidhide() {
    override var mainUrl = "https://vidhidepro.com"
}