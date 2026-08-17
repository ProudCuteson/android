package top.role.android.core.data.image

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class AndroidImageSourceInspector(
    private val contentResolver: ContentResolver,
) : ImageSourceInspector {

    override suspend fun inspect(
        sourceUri: Uri,
    ): ImageSourceInfo {
        return withContext(
            Dispatchers.IO,
        ) {
            val mime =
                resolveMime(
                    sourceUri = sourceUri,
                )

            val metadata =
                readOpenableMetadata(
                    sourceUri = sourceUri,
                )

            val dimensions =
                readImageDimensions(
                    sourceUri = sourceUri,
                )

            val bytes =
                metadata.bytes
                    ?: readFileBytes(
                        sourceUri = sourceUri,
                    )
                    ?: readBytesFromDescriptor(
                        sourceUri = sourceUri,
                    )
                    ?: error(
                        "IMAGE_SIZE_UNAVAILABLE",
                    )

            if (
                bytes <= 0L
            ) {
                error(
                    "IMAGE_SIZE_INVALID",
                )
            }

            ImageSourceInfo(
                mime =
                    mime,

                bytes =
                    bytes,

                fileName =
                    metadata.fileName
                        ?: resolveFileName(
                            sourceUri = sourceUri,
                        ),

                width =
                    dimensions.first,

                height =
                    dimensions.second,
            )
        }
    }

    private fun resolveMime(
        sourceUri: Uri,
    ): String {
        val resolverMime =
            runCatching {
                contentResolver
                    .getType(
                        sourceUri,
                    )
            }
                .getOrNull()
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            resolverMime != null
        ) {
            return resolverMime
        }

        return when (
            sourceUri.path
                ?.substringAfterLast(
                    ".",
                    missingDelimiterValue = "",
                )
                ?.lowercase()
        ) {
            "png" ->
                "image/png"

            "webp" ->
                "image/webp"

            "heic" ->
                "image/heic"

            "heif" ->
                "image/heif"

            "jpeg",
            "jpg" ->
                "image/jpeg"

            else ->
                "image/jpeg"
        }
    }

    private fun readOpenableMetadata(
        sourceUri: Uri,
    ): OpenableMetadata {
        /*
         * OpenableColumns 主要用于 content://。
         *
         * file:// 不需要走 ContentResolver query。
         */
        if (
            sourceUri.scheme ==
            ContentResolver.SCHEME_FILE
        ) {
            return OpenableMetadata(
                fileName =
                    resolveFileName(
                        sourceUri = sourceUri,
                    ),

                bytes =
                    readFileBytes(
                        sourceUri = sourceUri,
                    ),
            )
        }

        return runCatching {
            contentResolver
                .query(
                    sourceUri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME,
                        OpenableColumns.SIZE,
                    ),
                    null,
                    null,
                    null,
                )
                ?.use { cursor ->
                    if (
                        !cursor.moveToFirst()
                    ) {
                        return@use null
                    }

                    val nameIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME,
                        )

                    val sizeIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.SIZE,
                        )

                    val fileName =
                        if (
                            nameIndex >= 0 &&
                            !cursor.isNull(
                                nameIndex,
                            )
                        ) {
                            cursor.getString(
                                nameIndex,
                            )
                        } else {
                            null
                        }

                    val bytes =
                        if (
                            sizeIndex >= 0 &&
                            !cursor.isNull(
                                sizeIndex,
                            )
                        ) {
                            cursor.getLong(
                                sizeIndex,
                            )
                                .takeIf {
                                    it > 0L
                                }
                        } else {
                            null
                        }

                    OpenableMetadata(
                        fileName =
                            fileName,

                        bytes =
                            bytes,
                    )
                }
        }
            .getOrNull()
            ?: OpenableMetadata(
                fileName = null,
                bytes = null,
            )
    }

    private fun readImageDimensions(
        sourceUri: Uri,
    ): Pair<Int?, Int?> {
        return runCatching {
            val options =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds =
                        true
                }

            openInputStream(
                sourceUri = sourceUri,
            )
                ?.use { stream ->
                    BitmapFactory
                        .decodeStream(
                            stream,
                            null,
                            options,
                        )
                }

            val width =
                options.outWidth
                    .takeIf {
                        it > 0
                    }

            val height =
                options.outHeight
                    .takeIf {
                        it > 0
                    }

            width to height
        }
            .getOrElse {
                null to null
            }
    }

    private fun readFileBytes(
        sourceUri: Uri,
    ): Long? {
        if (
            sourceUri.scheme !=
            ContentResolver.SCHEME_FILE
        ) {
            return null
        }

        val path =
            sourceUri.path
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return null

        return runCatching {
            File(
                path,
            )
                .length()
                .takeIf {
                    it > 0L
                }
        }
            .getOrNull()
    }

    private fun readBytesFromDescriptor(
        sourceUri: Uri,
    ): Long? {
        if (
            sourceUri.scheme ==
            ContentResolver.SCHEME_FILE
        ) {
            return null
        }

        return runCatching {
            contentResolver
                .openAssetFileDescriptor(
                    sourceUri,
                    "r",
                )
                ?.use { descriptor ->
                    descriptor.length
                        .takeIf {
                            it > 0L
                        }
                }
        }
            .getOrNull()
    }

    private fun resolveFileName(
        sourceUri: Uri,
    ): String? {
        return when (
            sourceUri.scheme
        ) {
            ContentResolver.SCHEME_FILE ->
                sourceUri.path
                    ?.let {
                        File(
                            it,
                        ).name
                    }
                    ?.takeIf {
                        it.isNotBlank()
                    }

            else ->
                null
        }
    }

    private fun openInputStream(
        sourceUri: Uri,
    ): InputStream? {
        return when (
            sourceUri.scheme
        ) {
            ContentResolver.SCHEME_FILE ->
                sourceUri.path
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { path ->
                        File(
                            path,
                        ).inputStream()
                    }

            else ->
                contentResolver
                    .openInputStream(
                        sourceUri,
                    )
        }
    }

    private data class OpenableMetadata(
        val fileName: String?,
        val bytes: Long?,
    )
}