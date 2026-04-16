package com.rebahinn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RebahinnPlugin : Plugin() {
   override fun load(context: Context) {
        registerMainAPI(RebahinnProvider())
        
        // Daftarkan extractor sesuai nama kelas di atas
        registerExtractorAPI(DingtezuniExtractor())
        registerExtractorAPI(RebahinStreamWish())
        registerExtractorAPI(RebahinVidstack())
        registerExtractorAPI(RebahinHxfile())
        registerExtractorAPI(RebahinDood())
    }
}