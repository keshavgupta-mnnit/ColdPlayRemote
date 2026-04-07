object Dependencies {

    val coreKtx by lazy { "androidx.core:core-ktx:${Versions.coreKtx}" }
    val appcompat by lazy { "androidx.appcompat:appcompat:${Versions.appcompat}" }
    val junit by lazy { "junit:junit:${Versions.junit}" }
    val junitExt by lazy { "androidx.test.ext:junit:${Versions.junitExt}" }
    val espressoCore by lazy { "androidx.test.espresso:espresso-core:${Versions.espressoCore}" }

    val composeUi by lazy { "androidx.compose.ui:ui:${Versions.composeVersion}" }
    val composeMaterial3 by lazy { "androidx.compose.material3:material3:${Versions.material3}" }
    val composePreview by lazy { "androidx.compose.ui:ui-tooling-preview:${Versions.composeVersion}" }
    val lifecycleRuntime by lazy { "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntime}" }
    val activityCompose by lazy { "androidx.activity:activity-compose:${Versions.activityCompose}" }

    val composeJunit by lazy { "androidx.compose.ui:ui-test-junit4:${Versions.composeVersion}" }
    val composeUiTool by lazy { "androidx.compose.ui:ui-tooling:${Versions.composeVersion}" }

    val composeMaterialIcons by lazy { "androidx.compose.material:material-icons-extended:${Versions.composeMaterialIcons}" }

    val navigationCompose by lazy { "androidx.navigation:navigation-compose:${Versions.navigationCompose}" }

    val destinationCompose by lazy { "io.github.raamcosta.compose-destinations:core:${Versions.destinationCompose}" }
    val destinationComposeKsp by lazy { "io.github.raamcosta.compose-destinations:ksp:${Versions.destinationCompose}" }

    val hilt by lazy { "com.google.dagger:hilt-android:${Versions.hilt}" }
    val hiltCompiler by lazy { "com.google.dagger:hilt-compiler:${Versions.hiltCompiler}" }
    val hiltNavigationCompose by lazy { "androidx.hilt:hilt-navigation-compose:${Versions.hiltNavigationCompose}" }

    val firebase by lazy { "com.google.firebase:firebase-bom:${Versions.firebase}" }
    val firebaseAnalytics by lazy { "com.google.firebase:firebase-analytics" }
    val firebaseCrashlytics by lazy { "com.google.firebase:firebase-crashlytics" }

    val coroutines by lazy {"org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"}

    val timber by lazy { "com.jakewharton.timber:timber:${Versions.timber}" }

}
