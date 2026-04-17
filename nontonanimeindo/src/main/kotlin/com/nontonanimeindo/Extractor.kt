package com.nontonanimeindo

import com.lagradost.cloudstream3.extractors.Vidstack
import com.lagradost.cloudstream3.extractors.StreamWishExtractor

class VidStackIndo : Vidstack() {
    override var name = "VidStack"
    override var mainUrl = "https://vidstack.icu"
}

class StreamWishIndo : StreamWishExtractor() {
    override var name = "StreamWish"
    override var mainUrl = "https://streamwish.to"
}