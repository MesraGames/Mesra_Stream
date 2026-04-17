package com.nontonanimeindo

import com.lagradost.cloudstream3.extractors.*

/* =========================================================================== */
/*                    EXTRACTORS MENGGUNAKAN CLASS BAWAAN                      */
/* =========================================================================== */

class NontonAnimeVid : VidStack() {
    override var name = "NontonAnimeVid"
    override var mainUrl = "https://sub.nontonanimeindo.com"
    override var requiresReferer = true
}

class NontonAnimeWish : StreamWishExtractor() {
    override val name = "NontonAnimeWish"
    override val mainUrl = "https://wish.nontonanimeindo.id"
}

class NontonAnimeFile : Filesim() {
    override var name = "NontonAnimeFile"
    override var mainUrl = "https://filesim.com"
}

class NontonAnimePlayer : Gdriveplayer() {
    override var name = "NontonAnimePlayer"
    override var mainUrl = "https://gdriveplayer.to"
}