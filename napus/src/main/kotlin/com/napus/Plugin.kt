package com.napus

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NapusPlugin : Plugin() {
    override fun load(context: Context) {
        // Mendaftarkan Provider dan Extractor ke sistem Cloudstream
        registerMainAPI(NapusProvider())
        registerExtractorAPI(NapusExtractor())
    }
}