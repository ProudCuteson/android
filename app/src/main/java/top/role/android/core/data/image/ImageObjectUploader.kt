package top.role.android.core.data.image

import android.net.Uri
import top.role.android.core.model.image.MobileImageUploadAuth

interface ImageObjectUploader {

    suspend fun upload(
        sourceUri: Uri,
        mime: String,
        bytes: Long,
        auth: MobileImageUploadAuth,
    )
}