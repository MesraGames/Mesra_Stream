import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import com.lagradost.cloudstream3.gradle.CloudstreamPluginConfiguration

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

cloudstream {
    setBaseVersionName("1.0.0")
    setBaseVersionCode(1)
}

dependencies {
    implementation(project(":app"))
}