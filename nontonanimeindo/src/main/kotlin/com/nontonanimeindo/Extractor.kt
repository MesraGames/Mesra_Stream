package com.nontonanimeindo

import com.lagradost.cloudstream3.extractors.VidStack
import com.lagradost.cloudstream3.extractors.StreamWishExtractor

class NontonAnimeIndoVid : VidStack() {
    override var name = "NontonAnimeIndo Player"
    override var mainUrl = "https://nontonanimeindo.id"
    override var requiresReferer = true
}

class NontonAnimeIndoWish : StreamWishExtractor() {
    override val name = "NontonAnimeIndo Wish"
    override val mainUrl = "https://wish.nontonanimeindo.id"
}

class NontonAnimeIndoVoe : VidStack() { // Beberapa site indo rebrand Voe/Vidstack
    override var name = "NontonAnimeIndo Voe"
    override var mainUrl = "https://voe.sx"
}