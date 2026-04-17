package com.nontonanimeindo

import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.Vidstack

class NontonAnimeIndoStreamWish : StreamWishExtractor() {
    override var name = "StreamWish"
    override var mainUrl = "https://streamwish.to"
}

class NontonAnimeIndoVidstack : Vidstack() {
    override var name = "Vidstack"
    override var mainUrl = "https://vidstack.icu"
}