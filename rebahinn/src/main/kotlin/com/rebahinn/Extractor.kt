package com.rebahinn

import com.lagradost.cloudstream3.utils.VidStack
import com.lagradost.cloudstream3.extractors.StreamWishExtractor

class VidStackExtractor : VidStack() { 
    override val name = "VidStack"
}

class StreamWish : StreamWishExtractor() {
    override var name = "StreamWish"
}