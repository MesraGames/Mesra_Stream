package com.lagradost.cloudstream3.plugins.nontonanimeindo

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context
import com.lagradost.cloudstream3.extractors.*

@CloudstreamPlugin
class NontonAnimeIndoPlugin : Plugin() {
    override fun load(context: Context) {
        // Registrasi Provider Utama
        registerMainAPI(NontonAnimeIndoProvider())
        
        // Registrasi Extractor Kustom
        registerExtractorAPI(NontonAnimeIndoExtractor())
        
        // Registrasi Extractor Host Pihak Ketiga (Bawaan)
        registerExtractorAPI(StreamWishCustom())
        registerExtractorAPI(FilemoonCustom())
        registerExtractorAPI(DoodLaExtractor())
        registerExtractorAPI(OkRu())
    }
}