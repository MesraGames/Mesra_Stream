import io.github.lucavernini.CloudstreamPlugin
import io.github.lucavernini.extractor.SearchExtractor
import io.github.lucavernini.extractor.VideoExtractor
import io.github.lucavernini.extractor.VideoType
import java.net.URL
import java.util.regex.Pattern
import kotlin.text.regex.groupValues
import kotlin.text.trim
import kotlin.text.replaceFirst

object YLnimeExtractor : VideoExtractor() {
  override suspend fun url(url: String): VideoType? {
    val html = getHtml(url)
    val title = html.selectFirst("h1.entry-title").text().trim()
    val urlVideo = html.selectFirst("iframe[src]").attr("src")
    val videoUrl = URL(urlVideo).readText().trim()
    val regex = Pattern.compile("file: "+"([^"]+)")
    val matcher = regex.matcher(videoUrl)
    if (matcher.find()) {
      val videoLink = matcher.group(1)
      return VideoType.Video(url, title, groupValues = videoLink)
    } else {
      throw Exception("No video link found")
    }
  }
}