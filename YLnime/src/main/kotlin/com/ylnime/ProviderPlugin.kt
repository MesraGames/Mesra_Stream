package com.ylnime
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class YLNimeProviderPlugin : ProviderPlugin {
    override val supportedTypes: List<TvType> = listOf(TvType.Anime)
    override val api: MainAPI = YLNimeProvider()
    override val id: String = "YLNime"
    override val name: String = "YLNime"
    
    override fun getMainAPI(): MainAPI {
        return YLNimeProvider()
    }
}