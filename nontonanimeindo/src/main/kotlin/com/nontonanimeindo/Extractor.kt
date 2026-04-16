package com.nontonanimeindo

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.getAndUnpack

class NontonAnimeIndoExtractor : ExtractorApi() {
    override var name = "NAI-Aggressive"
    override var mainUrl = "https://nontonanimeindo.id"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink> {
        val response = app.get(url, referer = referer).text
        val extractedLinks = mutableListOf<ExtractorLink>()

        // Menggunakan Unpack jika skrip terproteksi Dean Edwards
        val unpacked = if (response.contains("eval(function(p,a,c,k,e,d)")) {
            getAndUnpack(response)
        } else {
            response
        }

        // Regex Agresif untuk mencari m3u8 atau mp4 di dalam source code
        val videoRegex = Regex("(?i)(?:file|hls|src|url)\\s*[:=]\\s*[\"']([^\"']+\\.(?:m3u8|mp4|mkv)[^\"']*)[\"']")
        videoRegex.findAll(unpacked).forEach { match ->
            val source = match.groupValues[1]
            if (source.contains("m3u8")) {
                val links = M3u8Helper.generateM3u8(
                    name,
                    source,
                    referer ?: mainUrl,
                    name = "Multi Resolution"
                )
                extractedLinks.addAll(links)
            } else {
                extractedLinks.add(
                    ExtractorLink(name, name, source, referer ?: "", 0, false)
                )
            }
        }

        return extractedLinks
    }
}