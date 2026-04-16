package com.rebahinn

import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8

// Pola Agresif ala Dingtezuni
class DingtezuniExtractor : ExtractorApi() {
    override val name = "Dingtezuni"
    override val mainUrl = "https://dingtezuni.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val response = app.get(url, referer = referer).text
        // Handle Packed script
        val unpacked = if (response.contains("eval(function(p,a,c,k,e,d)")) {
            getAndUnpack(response)
        } else response

        val m3u8 = Regex("file\\s*:\\s*\"(.*?m3u8.*?)\"").find(unpacked)?.groupValues?.get(1)
            ?: Regex("source\\s*:\\s*\"(.*?)\"").find(unpacked)?.groupValues?.get(1)

        return if (m3u8 != null) {
            generateM3u8(name, m3u8, referer ?: mainUrl).map { it }
        } else null
    }
}

class RebahinStreamWish : StreamWishExtractor() {
    override val name = "Rebahin Wish"
    override val mainUrl = "https://streamwish.to"
}

class RebahinVidstack : Vidstack() {
    override val name = "Rebahin Vidstack"
    override val mainUrl = "https://vidstack.icu"
}

class RebahinGdrive : GdrivePlayer() {
    override val name = "Rebahin GDrive"
    override val mainUrl = "https://gdriveplayer.to"
}

class RebahinHxfile : Hxfile() {
    override val name = "Rebahin Hxfile"
    override val mainUrl = "https://hxfile.co"
}

class RebahinDood : DoodLaExtractor() {
    override val name = "Rebahin Dood"
    override val mainUrl = "https://dood.li"
}