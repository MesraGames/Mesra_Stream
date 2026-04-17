package com.nontonanimeindo

import com.lagradost.cloudstream3.Plugin
import com.lagradost.cloudstream3.CloudstreamAPI
import android.content.Context

class NontonAnimePlugin : Plugin() {
    override fun load(context: Context) {
        // Register the main provider
        registerMainAPI(NontonAnimeIndo())
        
        // Register the simple extractors
        registerExtractorAPI(NontonAnimeVid())
        registerExtractorAPI(NontonAnimeWish())
        registerExtractorAPI(NontonAnimeFile())
        registerExtractorAPI(NontonAnimePlayer())
    }
}