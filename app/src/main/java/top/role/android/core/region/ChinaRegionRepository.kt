package top.role.android.core.region

import android.content.Context

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

import top.role.android.core.model.region.ChinaRegionNode


class ChinaRegionRepository(
    context: Context,
) {

    private val appContext =
        context.applicationContext

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    @Volatile
    private var cachedRegions:
            List<ChinaRegionNode>? = null


    suspend fun getRegions():
            List<ChinaRegionNode> {

        cachedRegions
            ?.let {
                return it
            }

        return withContext(
            Dispatchers.IO
        ) {

            cachedRegions
                ?.let {
                    return@withContext it
                }

            val content =
                appContext
                    .assets
                    .open("pca.json")
                    .bufferedReader()
                    .use {
                        it.readText()
                    }

            val result =
                json.decodeFromString<
                        List<ChinaRegionNode>
                        >(
                    content
                )

            cachedRegions =
                result

            result
        }
    }
}