package za.co.woolworths.financial.services.android.ui.activities.webview.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.activities.webview.usercase.WebViewClientHandler
import za.co.woolworths.financial.services.android.ui.activities.webview.usercase.WebViewHandler

@Module
@InstallIn(ViewModelComponent::class)
object WebViewModule {

    @Provides
    fun provideWebViewHandler(webViewClientHandler: WebViewClientHandler) = WebViewHandler(webViewClientHandler)

    @Provides
    fun provideWebViewClientHandler() = WebViewClientHandler()
}