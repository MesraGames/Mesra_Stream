package com.rebahinn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RebahinnPlugin : Plugin() {
    override fun load(context: Context) {
        // Registrasi Provider
        registerMainAPI(RebahinnProvider())
        // Registrasi Extractor Custom
        registerExtractorAPI(RebahinnExtractor())
    }
}