package com.izmir.avmmap

import android.app.Application

/**
 * Uygulama başlangıç sınıfı.
 * Bağımlılık enjeksiyon konteynırını başlatır.
 */
class IzmirAVMApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
