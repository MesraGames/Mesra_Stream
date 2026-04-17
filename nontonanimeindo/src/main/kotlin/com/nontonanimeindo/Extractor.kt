package com.nontonanimeindo

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.getAndUnpack
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType // IMPORT BARU YANG WAJIB

class NontonAnimeIndoExtractor : ExtractorApi() {
    override var name = "NAI-Aggressive"
    override var mainUrl = "https://nontonanimeindo.id"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink> {
        val response = app.get(url, referer = referer).text
        val extractedLinks = mutableListOf<ExtractorLink>()

        val unpacked = if (response.contains("eval(function(p,a,c,k,e,d)")) {
            getAndUnpack(response) ?: response
        } else {
            response
        }

        val videoRegex = Regex("(?i)(?:file|hls|src|url)\\s*[:=]\\s*[\"']([^\"']+\\.(?:m3u8|mp4|mkv)[^\"']*)[\"']")
        videoRegex.findAll(unpacked).forEach { match ->
            val source = match.groupValues[1]
            if (source.contains("m3u8")) {
                val links = M3u8Helper.generateM3u8(
                    name,
                    source,
                    referer ?: mainUrl
                )
                extractedLinks.addAll(links)
            } else {
                // MEMPERBAIKI FORMAT NEW EXTRACTOR LINK
                extractedLinks.add(
                    newExtractorLink(name, name, source, ExtractorLinkType.VIDEO) {
                        this.referer = referer ?: ""
                        this.quality = Qualities.Unknown.value
                    }
                )
            }
        }

        return extractedLinks
    }
}