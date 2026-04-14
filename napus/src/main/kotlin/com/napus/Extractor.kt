package com.napus

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import android.content.Context
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.network.CloudflareKiller

class NapusExtractor : ExtractorApi() {
    override var name = "NapusCustom"
    override var mainUrl = "https://napus.org"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, callback: (ExtractorLink) -> Unit) {
        // Scraping logic untuk server internal jika ada
        val doc = app.get(url, referer = referer).text
        val videoUrl = "" // Extract raw mp4 via regex
        if (videoUrl.isNotEmpty()) {
            callback.invoke(
                ExtractorLink(name, name, videoUrl, referer ?: "", Qualities.Unknown.value, false)
            )
        }
    }
}

class StreamWish : VidhideExtractor() {
    override var name = "StreamWish"
    override var mainUrl = "https://streamwish.to"
}

class Filemoon : Filesim() {
    override var name = "Filemoon"
    override var mainUrl = "https://filemoon.sx"
}

open class VidhideExtractor : ExtractorApi() {
    override var name = "Vidhide"
    override var mainUrl = "https://vidhide.com"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?, callback: (ExtractorLink) -> Unit) {
        loadExtractor(url, referer, callback)
    }
}