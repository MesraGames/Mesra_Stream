package com.rebahinn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RebahinnPlugin: Plugin() {
    override fun load(context: Context) {
        // Daftar Provider Utama
        registerMainAPI(RebahinnProvider())
        
        // Daftar Semua Mesin (Extractor) dari Extractor.kt
        registerExtractorAPI(DingtezuniExtractor())
        registerExtractorAPI(RebahinStreamWish())
        registerExtractorAPI(RebahinVidstack())
        registerExtractorAPI(RebahinHxfile())
        registerExtractorAPI(RebahinDood())
    }
}