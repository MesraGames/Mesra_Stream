package com.nontonanimeindo

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NontonanimeindoPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(NontonanimeindoProvider())
        registerExtractorAPI(NontonanimeindoVidStack())
        registerExtractorAPI(NontonanimeindoStreamWish())
    }
}