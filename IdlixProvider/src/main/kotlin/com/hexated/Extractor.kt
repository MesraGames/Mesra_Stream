package com.z1

import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.VidStack
import com.lagradost.cloudstream3.extractors.VidhideExtractor

class Z1Wish : StreamWishExtractor() {
    override var name = "Z1 Wish"
    override var mainUrl = "https://dwish.net" // URL StreamWish yang sering digunakan
}

class Z1VidStack : VidStack() {
    override var name = "Z1 Player"
    override var mainUrl = "https://player.idlix.com" // Sesuaikan dengan subdomain pemutar idlix
    override var requiresReferer = true
}

class Z1Vidhide : VidhideExtractor() {
    override var name = "Z1 Vidhide"
    override var mainUrl = "https://vidhidepre.com"
}