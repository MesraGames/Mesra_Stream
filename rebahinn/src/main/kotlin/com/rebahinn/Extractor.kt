package com.rebahinn

import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.VidStack
import com.lagradost.cloudstream3.extractors.VidStack
import com.lagradost.cloudstream3.extractors.Hxfile
import com.lagradost.cloudstream3.extractors.DoodLaExtractor

// Menggunakan VidStack sebagai base karena banyak web streaming Indo menggunakannya
class RebahinnVid : VidStack() {
    override var name = "RebahinnVid"
    override var mainUrl = "https://sub.rebahinn.com"
    override var requiresReferer = true
}

// Menggunakan StreamWish karena sering menjadi backup di rebahinn
class RebahinnWish : StreamWishExtractor() {
    override val name = "RebahinnWish"
    override val mainUrl = "https://wish.rebahinn.com"
}

class RebahinnEmbed : VidStack() {
    override var name = "Rebahinn Embed"
    override var mainUrl = "https://rebahinn.net"
    override var requiresReferer = true
}

class Vidhidepre : Dingtezuni() {
    override var name = "Vidhidepre"
    override var mainUrl = "https://vidhidepre.com"
}

class Dhtpre : Dingtezuni() {
    override var name = "Earnvids"
    override var mainUrl = "https://dhtpre.com"
}

class Mivalyo : Dingtezuni() {
    override var name = "Earnvids"
    override var mainUrl = "https://mivalyo.com"

open class Dingtezuni : ExtractorApi() {
    override val name = "Earnvids"
    override val mainUrl = "https://dingtezuni.com"
    override val requiresReferer = true

override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers = mapOf(
            "Sec-Fetch-Dest" to "empty",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Site" to "cross-site",
            "Origin" to mainUrl,
	        "User-Agent" to USER_AGENT,
        )
        
        val response = app.get(getEmbedUrl(url), referer = referer)
        val script = if (!getPacked(response.text).isNullOrEmpty()) {
            var result = getAndUnpack(response.text)
            if(result.contains("var links")){
                result = result.substringAfter("var links")
            }
            result
        } else {
            response.document.selectFirst("script:containsData(sources:)")?.data()
        } ?: return

        // m3u8 urls could be prefixed by 'file:', 'hls2:' or 'hls4:', so we just match ':'
        Regex(":\\s*\"(.*?m3u8.*?)\"").findAll(script).forEach { m3u8Match ->
            generateM3u8(
                name,
                fixUrl(m3u8Match.groupValues[1]),
                referer = "$mainUrl/",
                headers = headers
            ).forEach(callback)
        }
    }

    private fun getEmbedUrl(url: String): String {
		return when {
			url.contains("/d/") -> url.replace("/d/", "/v/")
			url.contains("/download/") -> url.replace("/download/", "/v/")
			url.contains("/file/") -> url.replace("/file/", "/v/")
			else -> url.replace("/f/", "/v/")
		}
	}

}

class Hglink : StreamWishExtractor() {
    override val name = "Hglink"
    override val mainUrl = "https://hglink.to"
}

class Hgcloud : StreamWishExtractor() {
    override val name = "Hgcloud"
    override val mainUrl = "https://hgcloud.to"
}

class Gdriveplayerto : Gdriveplayer() {
    override val mainUrl: String = "https://gdriveplayer.to"
}

class Xshotcok : Hxfile() {
    override val name = "Xshotcok"
    override val mainUrl = "https://xshotcok.com"
}

class Dsvplay : DoodLaExtractor() {
    override var mainUrl = "https://dsvplay.com"
}
