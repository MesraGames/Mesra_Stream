package com.nontonanimeindo

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.DoodLaExtractor

@CloudstreamPlugin
class NontonAnimeIndoPlugin : Plugin() {
    override fun load(context: Context) {
        // Registrasi API utama
        registerMainAPI(NontonAnimeIndoProvider())
        
        // Registrasi Extractor kustom dan bawaan yang sering digunakan target
        registerExtractorAPI(NontonAnimeIndoExtractor())
        registerExtractorAPI(StreamWishExtractor())
        registerExtractorAPI(DoodLaExtractor())
    }
}