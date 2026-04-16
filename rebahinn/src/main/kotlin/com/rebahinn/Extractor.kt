package com.rebahinn

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app

class RebahinnExtractor : ExtractorApi() {
    override val name = "RebahinnPlayer"
    override val mainUrl = "https://www.rebahinn.net"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        // Contoh ekstraksi link streaming mentah dari halaman iframe
        val response = app.get(url, referer = referer).text
        
        // Gunakan Regex untuk mencari file .m3u8 atau .mp4 di dalam script
        val sourceRegex = Regex("file\"?:\"?(https?://.*?\\.m3u8)")
        val streamUrl = sourceRegex.find(response)?.groupValues?.get(1)

        if (streamUrl != null) {
            callback.invoke(
                newExtractorLink(
                    this.name,
                    this.name,
                    streamUrl,
                    referer ?: "",
                    Qualities.Unknown.value,
                    type = if (streamUrl.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                )
            )
        }
    }
}