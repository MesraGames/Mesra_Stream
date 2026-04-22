package com.rebahinn

import com.lagradost.cloudstream3.utils.Filesim

class RebahinnExtractor : Filesim() {
    override val name = "RebahinnExtractor"
    override val mainUrl = "https://rebahinn.net"
    override val requiresReferer = true
}