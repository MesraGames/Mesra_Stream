package com.cloudstream.expressor

import com.cloudstream.expressor.Extractor
import com.cloudstream.expressor.Video
import com.cloudstream.expressor.VideoExtractor
import com.cloudstream.expressor.utils.Http
import com.cloudstream.expressor.utils.Http.Request
import com.cloudstream.expressor.utils.Json
import com.lambdapioneer.exductor.extended.VideoExtractor

class YLnimeExtractor : VideoExtractor("https://ylnime.com") {
    override fun extract(id: String, initialUrl: String): List<Video> {
        val url = "$initialUrl?id=$id"
        val soup = Http.get(url).soup()
        val videos = mutableListOf<Video>()
        val sources = soup.select("source")
        for (source in sources) {
            val videoUrl = source.attr("src")
            val quality = if (source.attr("res") != "") source.attr("res") else "Unknown"
            videos.add(Video(videoUrl, quality))
        }
        return videos
    }
}