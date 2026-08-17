package top.role.android.core.data.image

import android.net.Uri

interface ImageSourceInspector {

    suspend fun inspect(
        sourceUri: Uri,
    ): ImageSourceInfo
}