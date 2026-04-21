plugins {
        alias(libs.plugins.android.application) // Recommended: use the alias from your libs.versions.toml
        id("com.google.gms.google-services")
}


android {
    namespace ="com.itwpro.diet_trackerboom"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.itwpro.diet_trackerboom"
        minSdk = 24
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)


    // Firebase BOM — manages all Firebase versions together
    implementation (platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-analytics")

    // MPAndroidChart — weight & calorie charts
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}