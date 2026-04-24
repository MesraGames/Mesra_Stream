import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.ProviderPlugin
import com.lagradost.cloudstream3.utils.AppUtils
class YLNimePlugin : ProviderPlugin {
    override fun getMainUrl(): String {
        return "https://ylnime.com"
    }

    override fun withPlugin(): Boolean {
        return true
    }

    override fun getName(): String {
        return "YLNime"
    }

    override fun getApi(): MainAPI {
        return YLNime()
    }

    override fun getViewModel(): Any {
        return null
    }

    override fun onCreate(app: AppUtils) {
    }
}