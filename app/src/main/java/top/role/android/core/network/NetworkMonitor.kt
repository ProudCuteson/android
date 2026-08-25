package top.role.android.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged


class NetworkMonitor(
    context: Context,
) {

    private val connectivityManager =
        context.getSystemService(
            ConnectivityManager::class.java
        )


    val isOnline: Flow<Boolean> =
        callbackFlow {

            fun currentOnline(): Boolean {
                val network =
                    connectivityManager
                        .activeNetwork
                        ?: return false

                val capabilities =
                    connectivityManager
                        .getNetworkCapabilities(
                            network
                        )
                        ?: return false

                return capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) &&
                        capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED
                        )
            }


            trySend(
                currentOnline()
            )


            val callback =
                object :
                    ConnectivityManager.NetworkCallback() {

                    override fun onAvailable(
                        network: Network,
                    ) {
                        trySend(
                            currentOnline()
                        )
                    }


                    override fun onLost(
                        network: Network,
                    ) {
                        trySend(
                            currentOnline()
                        )
                    }


                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        trySend(
                            currentOnline()
                        )
                    }
                }


            connectivityManager
                .registerDefaultNetworkCallback(
                    callback
                )


            awaitClose {
                connectivityManager
                    .unregisterNetworkCallback(
                        callback
                    )
            }
        }
            .distinctUntilChanged()
}