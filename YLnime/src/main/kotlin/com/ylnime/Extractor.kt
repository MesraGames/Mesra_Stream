import com.lagradost.cloudstream3.extractors.YLNimeExtractor
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils QUALITY_1080p
import com.lagradost.cloudstream3.utils QUALITY_720p
import com.lagradost.cloudstream3.utils QUALITY_HD
class YLNimeExtractor : VideoExtractor() {
    override fun getVideoUrl(): String? {
        val scriptTag = document.querySelector("script")
        return scriptTag?.text()?.let { Regex("(https?://[^"]+)").find(it)?.group(1) }
    }

    override fun getVideoExtractorUrl(): String? {
        return "https://ylnime.com/ajax/get_link.php"
    }

    override fun getVideoDownloadUrl(): String? {
        return getVideoUrl()
    }

    override fun getVideoExtractors(): List<ExtractorLink> {
        return listOf(
            ExtractorLink(
                "ylnime",
                "https://ylnime.com/ajax/get_link.php",
                getVideoExtractorUrl(),
                null,
                QUALITY_HD,
                false
            )
        )
    }
}