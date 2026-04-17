package com.rebahinn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RebahinnPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(RebahinnProvider())
        registerExtractorAPI(VidstackExtractor())
        registerExtractorAPI(StreamwishExtractor())
    }
}