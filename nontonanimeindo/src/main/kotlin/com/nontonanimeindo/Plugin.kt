package com.nontonanimeindo

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NontonanimeindoPlugin : Plugin() {
    override fun load(context: Context) {
        // Register the main provider
        registerMainAPI(NontonanimeindoProvider())
        
        // Register all simple extractors
        registerExtractorAPI(NontonanimeindoVid())
        registerExtractorAPI(NontonanimeindoStreamWish())
        registerExtractorAPI(NontonanimeindoFilesim())
        registerExtractorAPI(NontonanimeindoGdrive())
    }
}