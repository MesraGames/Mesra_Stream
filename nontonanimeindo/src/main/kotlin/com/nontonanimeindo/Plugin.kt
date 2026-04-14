package com.nontonanimeindo

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NontonAnimeIndoPlugin : Plugin() {
    override fun load(context: Context) {
        // Registrasi Provider Utama
        registerMainAPI(NontonanimeindoProvider())
        
        // Registrasi Custom dan External Extractors
        registerExtractorAPI(NontonAnimeIndoExtractor())
        registerExtractorAPI(AlternativeVidHide())
        registerExtractorAPI(AlternativeFilemoon())
    }
}