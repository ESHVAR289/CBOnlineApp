package com.codingblocks.cbonlineapp

import android.app.Application
import com.codingblocks.onlineapi.CustomResponseInterceptor
import com.devbrackets.android.exomedia.ExoMedia
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.TransferListener
import com.squareup.picasso.Picasso
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import okhttp3.OkHttpClient

class CBOnlineApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Picasso.setSingletonInstance(Picasso.Builder(this).build())
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Cabin-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())
        configureExoMedia()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
//            shortcutAction(::updateShortcuts)
    }

    private fun configureExoMedia() {
        // Registers the media sources to use the OkHttp client instead of the standard Apache one
        // Note: the OkHttpDataSourceFactory can be found in the ExoPlayer extension library `extension-okhttp`
        ExoMedia.setDataSourceFactoryProvider(object : ExoMedia.DataSourceFactoryProvider {
            private var instance: DataSource.Factory? = null

            override fun provide(userAgent: String, listener: TransferListener?): DataSource.Factory {
                if (instance == null) {
                    // Updates the network data source to use the OKHttp implementation
                    val interceptor = CustomResponseInterceptor()
                    val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

                    val upstreamFactory = OkHttpDataSourceFactory(client, userAgent, listener)
                    instance = upstreamFactory
                    // Adds a cache around the upstreamFactory
//                    val cache = SimpleCache(File(cacheDir, "ExoMediaCache"), LeastRecentlyUsedCacheEvictor((50 * 1024 * 1024).toLong()))
//                    instance = CacheDataSourceFactory(cache, upstreamFactory, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                }

                return instance!!
            }
        })
    }
}