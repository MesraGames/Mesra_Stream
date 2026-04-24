package com.ylnime
import com.lagradost.cloudstream3.utils.*

class YLNimeExtractor : ExtractorApi() {
    override val name = "YLNimeExtractor"
    override val mainUrl = "https://ylnime.com/"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        return listOf(
            newExtractorLink(this.name, this.name, url, ExtractorLinkType.VIDEO) {
                this.referer = mainUrl
                this.quality = Qualities.Unknown.value
            }
        )
    }
}