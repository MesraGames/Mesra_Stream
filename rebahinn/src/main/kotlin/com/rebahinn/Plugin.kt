package com.rebahinn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RebahinnPlugin : Plugin() {
    override fun load(context: Context) {
        // Mendaftarkan provider dan extractor ke sistem
        registerMainAPI(RebahinnProvider())
        registerExtractorAPI(RebahinnExtractor())
    }
}