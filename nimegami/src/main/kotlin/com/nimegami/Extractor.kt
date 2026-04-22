package com.nimegami

import com.lagradost.cloudstream3.utils.ExtractorApi

class NimegamiExtractor : ExtractorApi() {
    override var name = "Nimegami"
    override var mainUrl = "https://nimegami.id"
    override val requiresReferer = false
}