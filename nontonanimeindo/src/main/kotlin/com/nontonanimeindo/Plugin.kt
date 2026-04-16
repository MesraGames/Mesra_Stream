package com.nontonanimeindo

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NontonanimeindoPlugin: Plugin() {
    override fun load(context: Context) {
        // Register API utama
        registerMainAPI(NontonanimeindoProvider())
        
        // Register semua extractor yang didefinisikan di Extractor.kt
        registerExtractorAPI(NontonAnimeIndoVid())
        registerExtractorAPI(NontonAnimeIndoWish())
        registerExtractorAPI(NontonAnimeIndoVoe())
    }
}