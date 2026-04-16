package com.z1

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class Z1Plugin : Plugin() {
    override fun load(context: Context) {
        // Register main API provider
        registerMainAPI(Z1Provider())
        
        // Register custom extractors yang sudah dibuat
        registerExtractorAPI(Z1Wish())
        registerExtractorAPI(Z1VidStack())
        registerExtractorAPI(Z1Vidhide())
    }
}