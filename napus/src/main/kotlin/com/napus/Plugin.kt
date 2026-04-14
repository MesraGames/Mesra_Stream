package com.napus

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NapusPlugin: Plugin() {
    override fun load(context: Context) {
        // Registrasi API Utama
        registerMainAPI(NapusProvider())
        
        // Registrasi Semua Extractor
        registerExtractorAPI(NapusRawExtractor())
        registerExtractorAPI(StreamWishNapus())
        registerExtractorAPI(FilemoonNapus())
        registerExtractorAPI(VidmolyNapus())
    }
}