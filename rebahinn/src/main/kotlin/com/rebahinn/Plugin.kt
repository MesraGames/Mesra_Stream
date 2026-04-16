package com.rebahinn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RebahinnPlugin : Plugin() {
    override fun load(context: Context) {
        // Register Provider Utama
        registerMainAPI(RebahinnProvider())
        
        // Register Semua Extractor yang telah dibuat
        registerExtractorAPI(RebahinnVid())
        registerExtractorAPI(RebahinnWish())
        registerExtractorAPI(RebahinnEmbed())
    }
}