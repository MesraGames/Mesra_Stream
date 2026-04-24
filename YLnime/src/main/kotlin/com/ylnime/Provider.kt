import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.apis.AnimeApi
import com.lagradost.cloudstream3.apis.AnimeSearchResponse
import com.lagradost.cloudstream3.apis.AnimeSearchRequest
import com.lagradost.cloudstream3.apis.Episode
import com.lagradost.cloudstream3.apis.MovieSearchResponse
import com.lagradost.cloudstream3.apis.TvType
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.SplitPair
import org.jsoup.nodes.Document
import com.lagradost.cloudstream3.utils.JsoupUtils
import java.util.ArrayList
import java.util.TimeUnit
import java.util.concurrent.TimeUnit
import kotlin.math.min

class YLNime : MainAPI() {
    companion object {
        private const val mainUrl = "https://ylnime.com"
    }
    override fun getMainUrl(): String {
        return mainUrl
    }
    override fun getDisplayName(): String {
        return "YLNime"
    }
    override fun search(query: AnimeSearchRequest): AnimeSearchResponse {
        val soup = JsoupUtils.fetch(url = "$mainUrl/?s=$query=search")
        val results: ArrayList<SplitPair> = ArrayList()
        soup.select("div.post > h2 > a").forEach { element ->
            val title = element.text().trim()
            val href = element.attr("href")
            results.add(SplitPair(title, href))
        }
        return AnimeSearchResponse(results)
    }

    override fun load(url: String): String {
        if (url.contains("episode-")) {
            return "${url}episode-"
        }
        return url
    }
    override fun loadLinks(episode: Episode, callback: (ExtractorLink) -> Unit) {
        val document: Document = JsoupUtils.fetch(url = episode.url)
        val scriptTag = document.select("script").last()
        val link: String? = scriptTag?.text()?.let {
            Regex("(https?://[^"]+)").find(it)?.group(1)
        }
        if (link != null) {
            callback.invoke(ExtractorLink(
                "YLNime",
                link,
                link,
                "",
                null,
                false
            ))
        }
    }
}