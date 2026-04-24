package com.ylnime
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

class YLNimeExtractor : ExtractorApi() {
    override val name = "YLNimeExtractor"
    override val mainUrl = "https://ylnime.com"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        return listOf(
            ExtractorLink(this.name, this.name, url, mainUrl, Qualities.Unknown.value, false)
        )
    }
}