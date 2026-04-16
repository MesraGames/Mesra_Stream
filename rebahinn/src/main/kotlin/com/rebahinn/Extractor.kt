package com.rebahinn

import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.VidStack

// Menggunakan VidStack sebagai base karena banyak web streaming Indo menggunakannya
class RebahinnVid : VidStack() {
    override var name = "RebahinnVid"
    override var mainUrl = "https://sub.rebahinn.com"
    override var requiresReferer = true
}

// Menggunakan StreamWish karena sering menjadi backup di rebahinn
class RebahinnWish : StreamWishExtractor() {
    override val name = "RebahinnWish"
    override val mainUrl = "https://wish.rebahinn.com"
}

class RebahinnEmbed : VidStack() {
    override var name = "Rebahinn Embed"
    override var mainUrl = "https://rebahinn.net"
    override var requiresReferer = true
}