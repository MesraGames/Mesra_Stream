package com.nontonanimeindo

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.extractors.StreamWishExtractor

class MyStreamWish : StreamWishExtractor() {
    override val name = "StreamWish"
    override val mainUrl = "https://streamwish.to"
}