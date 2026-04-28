import java.util.Properties

// use an integer for version numbers
version = 13

android {
    // INI BARIS YANG DITAMBAHKAN AGAR BUILDCONFIG BERHASIL DI-GENERATE
    namespace = "com.yflix"

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    
    defaultConfig {
        // 1. Membaca properti dengan aman (Safe Read)
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // 2. Mengambil nilai dari local.properties, atau fallback ke Environment Variables GitHub, atau string kosong jika tidak ada
        val yfxEnc = properties.getProperty("YFXENC") ?: System.getenv("YFXENC") ?: ""
        val yfxDec = properties.getProperty("YFXDEC") ?: System.getenv("YFXDEC") ?: ""
        val kaimeg = properties.getProperty("KAIMEG") ?: System.getenv("KAIMEG") ?: ""

        // 3. Memasukkan nilai tersebut ke dalam BuildConfig
        buildConfigField("String", "YFXENC", "\"$yfxEnc\"")
        buildConfigField("String", "YFXDEC", "\"$yfxDec\"")
        buildConfigField("String", "KAIMEG", "\"$kaimeg\"")
    }
}

dependencies {
    implementation("com.google.android.material:material:1.13.0")
}

cloudstream {
    language = "en"
    // All of these properties are optional, you can safely remove them
    description = "Movies & TV Series Etc\nSettings allow selecting domains such as 1Movies, SolarMovie and Sflix"
    
    // Opsional: Karena Anda ingin mandiri dari phisher98, Anda bisa mengganti nama author dengan nama Anda sendiri
    authors = listOf("Mesra Stream") 
    
    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "AsianDrama",
        "TvSeries",
        "Anime",
        "Movie",
        "Cartoon",
        "AnimeMovie"
    )

    iconUrl = "https://www.google.com/s2/favicons?domain=yflix.to/&sz=%size%"

    requiresResources = true
    isCrossPlatform = false
}