package com.ylnime
import com.lagradost.cloudstream3.utils.*

class YLNimeExtractor : ExtractorApi() {
    override val name = "YLNimeExtractor"
    override val mainUrl = "https://ylnime.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url, referer = referer).document
        val source = document.select("source").first()
        val quality = Qualities.Unknown.value
        val src = source.attr("src") ?: ""
        return listOf(
            newExtractorLink(this.name, this.name, src, ExtractorLinkType.VIDEO) {
                this.referer = url
                this.quality = quality
            }
        )
    }
}