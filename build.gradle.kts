// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
// Dentro del bloque android {}
buildFeatures {
    viewBinding = true
}

// En el bloque dependencies {}
// Lifecycle y ViewModel
implementation(libs.androidx.lifecycle.viewmodel.ktx) // O usa versión directa: "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4"
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.ktx)

// Retrofit y Gson (Red)
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// Corrutinas (Asincronismo)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

// Coil (Carga de imágenes para el catálogo)
implementation("io.coil-kt:coil:2.7.0")