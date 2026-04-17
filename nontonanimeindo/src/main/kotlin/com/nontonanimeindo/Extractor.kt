package com.nontonanimeindo

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.extractors.Vidstack
import com.lagradost.cloudstream3.extractors.StreamWishExtractor

class VidStackExtractor : Vidstack() {
    override val name = "VidStack"
    override val mainUrl = "https://vidstack.com"
}

class MyStreamWish : StreamWishExtractor() {
    override val name = "StreamWish"
    override val mainUrl = "https://streamwish.to"
}