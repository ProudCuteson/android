package top.role.android.feature.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun InAppBrowserScreen(
    title: String,
    url: String,
    onBack: () -> Unit,
) {

    var webView by
    remember {
        mutableStateOf<WebView?>(
            null
        )
    }


    var loading by
    remember {
        mutableStateOf(
            true
        )
    }


    var loadError by
    remember {
        mutableStateOf<String?>(
            null
        )
    }


    BackHandler {

        val currentWebView =
            webView

        if (
            currentWebView != null &&
            currentWebView.canGoBack()
        ) {

            currentWebView.goBack()

        } else {

            onBack()
        }
    }


    DisposableEffect(
        Unit
    ) {

        onDispose {

            webView
                ?.apply {

                    stopLoading()

                    webViewClient =
                        WebViewClient()

                    removeAllViews()

                    destroy()
                }


            webView =
                null
        }
    }


    Scaffold(
        topBar = {

            TopAppBar(
                title = {

                    Text(
                        text =
                            title,
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick =
                            onBack,
                    ) {

                        Icon(
                            imageVector =
                                Icons
                                    .AutoMirrored
                                    .Filled
                                    .ArrowBack,

                            contentDescription =
                                "返回",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    ),
        ) {

            AndroidView(
                modifier =
                    Modifier.fillMaxSize(),

                factory = { context ->

                    WebView(
                        context
                    ).apply {

                        /*
                         * 用户协议 / 隐私政策当前不需要
                         * JavaScript。
                         *
                         * 默认即为 false，这里明确写出来，
                         * 避免误开启。
                         */
                        settings.javaScriptEnabled =
                            false


                        /*
                         * 不允许通过 WebView 直接访问
                         * 本地文件。
                         */
                        settings.allowFileAccess =
                            false

                        settings.allowContentAccess =
                            false


                        /*
                         * 对普通 HTTPS 文档页面足够。
                         */
                        settings.domStorageEnabled =
                            false


                        webViewClient =
                            object :
                                WebViewClient() {

                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?,
                                ) {

                                    loading =
                                        true

                                    loadError =
                                        null
                                }


                                override fun onPageFinished(
                                    view: WebView?,
                                    url: String?,
                                ) {

                                    loading =
                                        false
                                }


                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?,
                                ) {

                                    /*
                                     * 只处理主页面错误，
                                     * 避免 favicon / image 等子资源失败
                                     * 导致整个页面显示加载失败。
                                     */
                                    if (
                                        request?.isForMainFrame ==
                                        true
                                    ) {

                                        loading =
                                            false

                                        loadError =
                                            "页面加载失败"
                                    }
                                }
                            }


                        webView =
                            this


                        loadUrl(
                            url
                        )
                    }
                },

                update = { view ->

                    webView =
                        view
                },
            )


            if (
                loading
            ) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),
                )
            }


            loadError
                ?.let { message ->

                    Text(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),

                        text =
                            message,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                    )
                }
        }
    }
}