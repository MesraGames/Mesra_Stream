package com.nontonanimeindo

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.INFER_TYPE
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.extractors.*

/* =========================================================================
   EXTRACTORS: Super Simple Class Overrides
   ========================================================================= */

class NontonanimeindoVid : VidStack() {
    override var name = "NontonanimeindoVid"
    override var mainUrl = "https://sub.nontonanimeindo.com"
    override var requiresReferer = true
}

class NontonanimeindoStreamWish : StreamWishExtractor() {
    override var name = "NontonAnime Wish"
    override var mainUrl = "https://streamwish.to"
}

class NontonanimeindoFilesim : Filesim() {
    override var name = "NontonAnime Filesim"
    override var mainUrl = "https://filesim.com"
}

class NontonanimeindoGdrive : GdrivePlayer() {
    override var name = "NontonAnime GDrive"
    override var mainUrl = "https://gdriveplayer.to"
}