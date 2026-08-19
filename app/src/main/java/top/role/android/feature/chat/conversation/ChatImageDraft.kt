package top.role.android.feature.chat.conversation

import android.net.Uri

data class ChatImageDraft(
    val sourceUri: Uri,

    /*
     * 图片上传成功后保留 imageId。
     *
     * 如果后续 sendMobileImageMessage 失败，
     * retry 时不需要重新上传同一张图片。
     */
    val uploadedImageId: String? = null,

    val uploading: Boolean = false,
    val sending: Boolean = false,

    val errorMessage: String? = null,
)