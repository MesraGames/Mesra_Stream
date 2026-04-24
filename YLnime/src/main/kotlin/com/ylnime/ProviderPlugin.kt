package com.cloudstream.plugin

import com.cloudstream.cloudstream.MainAPI
import com.cloudstream.cloudstream.ProviderPlugin
import com.lambdapioneer.exductor.extended.ProviderModule

class YLnimePlugin : ProviderPlugin {
    override fun mainAPIs(): List<MainAPI> {
        return listOf(YLnimeProvider())
    }

    override fun extractors(): List<VideoExtractor> {
        return listOf(YLnimeExtractor())
    }
}