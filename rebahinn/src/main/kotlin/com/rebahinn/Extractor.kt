package com.rebahinn

import com.lagradost.cloudstream3.utils.ExtractorApi

class RebahinnExtractor : ExtractorApi() {
    override var name = "Rebahinn"
    override var mainUrl = "https://rebahinn.com"
    override val requiresReferer = false
}