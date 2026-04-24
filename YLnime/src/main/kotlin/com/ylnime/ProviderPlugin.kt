import io.github.lucavernini.CloudstreamPlugin
import io.github.lucavernini.ProviderPlugin
import io.github.lucavernini.extractor.YLnimeExtractor
import io.github.lucavernini.provider.YLnime

class YLnimePlugin : ProviderPlugin() {
  override val providers: List<MainAPI> = listOf(YLnime())
  override val extractors: List<VideoExtractor> = listOf(YLnimeExtractor)
}