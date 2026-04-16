package com.rebahinn

import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8

// 1. Extractor Kustom untuk Dingtezuni
class DingtezuniExtractor : ExtractorApi() {
    override var name = "Dingtezuni"
    override var mainUrl = "https://dingtezuni.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val response = app.get(url, referer = referer).text
        val unpacked = if (response.contains("eval(function(p,a,c,k,e,d)")) {
            getAndUnpack(response) ?: response
        } else response

        val m3u8 = Regex("file\\s*:\\s*\"(.*?m3u8.*?)\"").find(unpacked)?.groupValues?.get(1)
            ?: Regex("source\\s*:\\s*\"(.*?)\"").find(unpacked)?.groupValues?.get(1)

        return if (m3u8 != null) {
            generateM3u8(name, m3u8, referer ?: mainUrl)
        } else null
    }
}

// 2. Extractor untuk StreamWish
class RebahinStreamWish : StreamWishExtractor() {
    override var name = "Rebahin Wish"
    override var mainUrl = "https://streamwish.to"
}

// 3. Extractor untuk Vidstack/Vidhide
class RebahinVidstack : VidhideExtractor() { 
    override var name = "Rebahin Vidstack"
    override var mainUrl = "https://vidstack.icu"
}

// 4. Extractor untuk Hxfile
class RebahinHxfile : Hxfile() {
    override var name = "Rebahin Hxfile"
    override var mainUrl = "https://hxfile.co"
}

// 5. Extractor untuk DoodStream
class RebahinDood : DoodLaExtractor() {
    override var name = "Rebahin Dood"
    override var mainUrl = "https://dood.li"
}