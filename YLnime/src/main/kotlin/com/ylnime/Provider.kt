package com.cloudstream.cloudstream

import com.cloudstream.cloudstream.MainAPI
import com.cloudstream.cloudstream.SearchResponse
import com.cloudstream.cloudstream.Video
import com.lambdapioneer.exductor.extended.Jsoup
import com.lambdapioneer.exductor.extended.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class YLnimeProvider : MainAPI("https://ylnime.com") {
    override fun search(query: String): SearchResponse {
        val url = "https://ylnime.com/index.php?search=$query"
        val doc = Jsoup.connect(url).get()
        val items = mutableListOf<Video>()
        for (item in doc.select(".post-article > .entry-header > .entry-title > a")) {
            val id = item.attr("href").split("/").last()
            val title = item.text()
            items.add(Video(id, title, this.name, this))
        }
        return SearchResponse(items)
    }

    override fun home(): SearchResponse? {
        val url = "https://ylnime.com/index.php?terbaru=1"
        val doc = Jsoup.connect(url).get()
        val items = mutableListOf<Video>()
        for (item in doc.select(".post-article > .entry-header > .entry-title > a")) {
            val id = item.attr("href").split("/").last()
            val title = item.text()
            items.add(Video(id, title, this.name, this))
        }
        return SearchResponse(items)
    }

    override fun video(id: String): Video {
        return Video(id, "", this.name, this)
    }
}