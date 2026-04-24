// use an integer for version numbers
version = 1

import com.lagradost.cloudstream3.plugins.makePlugin

cloudstream {
    language = "id"
    authors = listOf("AI Scraper Generator")
    
    status = 1
    tvTypes = listOf(
        "AsianDrama",
        "TvSeries",
        "Movie"
    )

    iconUrl = "https://www.google.com/s2/favicons?domain=ylnime.com&sz=%size%"
    isCrossPlatform = false
}

makePlugin()
