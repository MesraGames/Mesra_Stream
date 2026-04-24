package com.ylnime
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class YLNimePlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(YLNime())
        registerExtractorAPI(YLNimeExtractor())
    }
}