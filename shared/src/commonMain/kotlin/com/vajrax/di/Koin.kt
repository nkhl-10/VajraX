package com.vajrax.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Shared Koin Initialization.
 * Can be called from androidMain (e.g., Application class) or iosMain (e.g., AppDelegate in Swift).
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(dataModule())
}

// iOS specific parameter-less helper for Swift
fun initKoin() = initKoin {}
