package com.napus
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class NapusPlugin : Plugin() {
    override fun load(context: Context) {
        // Registrasi provider utama
        registerMainAPI(NapusProvider())
        // Registrasi semua extractor yang telah didefinisikan
        registerExtractorAPI(NapusExtractor())
        registerExtractorAPI(StreamWishExtractor())
        registerExtractorAPI(VidhideExtractor())
    }
}