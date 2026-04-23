package com.izmir.avmmap

import android.content.Context
import com.izmir.avmmap.data.cache.MallCache
import com.izmir.avmmap.data.network.OverpassApiService
import com.izmir.avmmap.data.repository.MallRepositoryImpl
import com.izmir.avmmap.domain.repository.MallRepository
import com.izmir.avmmap.domain.usecase.GetMallsUseCase
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Basit bağımlılık enjeksiyon konteynırı.
 * Hilt/Dagger yerine manuel DI kullanılmıştır (hafiflik için).
 */
class AppContainer(context: Context) {

    /** Uygulama genelinde kullanılacak JSON parser */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /** HTTP istemcisi */
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        engine {
            config {
                connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            }
        }
    }

    /** Overpass API servisi */
    val overpassApiService = OverpassApiService(httpClient)

    /** Önbellek */
    val mallCache = MallCache(context)

    /** Repository */
    val mallRepository: MallRepository = MallRepositoryImpl(overpassApiService, mallCache)

    /** Use case */
    val getMallsUseCase = GetMallsUseCase(mallRepository)
}
