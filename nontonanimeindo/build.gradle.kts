apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

version = 1

cloudstream {
    description = "Nontonanimeindo Provider"
    language = "id"
    authors = listOf("RavenX")

    /**
     * Status: 1 (Ok)
     * */
    status = 1
    tvTypes = listOf(
        "TvSeries",
        "Movie",
        "Anime",
        "AsianDrama"
    )

    iconUrl = "https://t2.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://nontonanimeindo.id&size=%size%"
}

dependencies {
    // Dependensi standar sudah dihandle oleh core
}