package com.nontonanimeindo
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.INFER_TYPE
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.extractors.*

class NontonVid : VidStack() {
    override var name = "NontonVid"
    override var mainUrl = "https://sub.nontonanimeindo.com"
    override var requiresReferer = true
}

class NontonFile : Filesim() {
    override val name = "NontonFile"
    override var mainUrl = "https://file.nontonanimeindo.com"
}