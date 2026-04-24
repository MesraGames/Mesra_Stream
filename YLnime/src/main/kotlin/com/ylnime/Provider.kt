import io.github.lucavernini.CloudstreamPlugin
import io.github.lucavernini.MainAPI
import io.github.lucavernini.models.Episode
import io.github.lucavernini.models.SearchResponse
import io.github.lucavernini.models.ShowSearchResponse
import java.util.regex.Pattern
import kotlin.text.replaceFirst
import kotlin.text.replace
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object YLnime : MainAPI() {
  override val id: String = "ylnime"
  override val name: String = "YLnime"
  override val lang: String = "id"
  override val hasMainPage: Boolean = true
  override val supportedTypes: List<ShowType> = listOf(ShowType.Anime)
  override val hasTrending: Boolean = false
  override val hasA-ZList: Boolean = false
  override val hasLatest: Boolean = true

  override suspend fun getMainPage(page: Int, searchQuery: String?): List<SearchResponse> {
    val doc: Document = Jsoup.connect("https://ylnime.com/index.php?terbaru=1").get()
    val list = mutableListOf<SearchResponse>()
    for (element in doc.select(".lastepisode").select(".bs")) {
      val url = element.selectFirst("a").attr("href")
      val title = element.selectFirst("a").text().trim()
      list.add(SearchResponse(url, title))
    }
    return list
  }

  override suspend fun search(query: String): List<SearchResponse> {
    val doc: Document
    try {
      doc = Jsoup.connect("https://ylnime.com/?s=$query").get()
    } catch (e: Exception) {
      return listOf()
    }
    val list = mutableListOf<SearchResponse>()
    for (element in doc.select(".search-page").select("article")) {
      val url = element.selectFirst("a").attr("href")
      val title = element.selectFirst("h3").text().trim()
      list.add(SearchResponse(url, title))
    }
    return list
  }
}