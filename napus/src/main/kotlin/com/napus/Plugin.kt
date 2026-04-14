package com.napus

import com.lagradost.cloudstream3.Plugin
import com.lagradost.cloudstream3.CloudstreamPlugin
import android.content.Context
import com.lagradost.cloudstream3.utils.ExtractorApi

@CloudstreamPlugin
class NapusPlugin : Plugin() {
    override fun load(context: Context) {
        // Register Provider Utama
        registerMainAPI(NapusProvider())
        
        // Register Semua Extractor yang digunakan
        registerExtractorAPI(NapusExtractor())
        registerExtractorAPI(StreamWish())
        registerExtractorAPI(Filemoon())
        registerExtractorAPI(VidhideExtractor())
    }
}