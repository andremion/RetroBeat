/*
 *    Copyright 2024. André Luiz Oliveira Rêgo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.andremion.musicplayer.data.api.deezer

import android.annotation.SuppressLint
import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.android.Android
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

private val SslContext = SSLContext.getInstance("SSL").apply {
    init(null, arrayOf(NoOpTrustManager), null)
}

internal actual fun buildDeezerClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Android) {
        engine {
            sslManager = { connection ->
                // Deezer API certificate doesn't work on Android 7.1.1 (API level 25) and below.
                // If API level is less than 25 and the URL is *.deezer.com, trust all certificates
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 && connection.url.isDeezer()) {
                    connection.sslSocketFactory = SslContext.socketFactory
                }
            }
        }
        block()
    }

@SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
private object NoOpTrustManager : X509TrustManager {

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        // no-op
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        // no-op
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}

private fun URL.isDeezer(): Boolean =
    host.endsWith(".deezer.com")
