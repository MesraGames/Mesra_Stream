apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

version = 1

cloudstream {
    description = "Nontonanimeindo Provider"
    language = "id"
    authors = listOf("Cloudstream AI")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
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
    // Tambahkan dependensi khusus ekstensi di sini jika diperlukan
}