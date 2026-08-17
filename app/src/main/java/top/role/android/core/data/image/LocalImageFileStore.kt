package top.role.android.core.data.image

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class LocalImageFileStore(
    context: Context,
    private val contentResolver: ContentResolver,
) {

    private val rootDirectory =
        File(
            context.applicationContext.filesDir,
            "note-images",
        )

    suspend fun importImage(
        sourceUri: Uri,
        mime: String,
    ): String {
        return withContext(
            Dispatchers.IO,
        ) {
            ensureDirectory()

            val target =
                File(
                    rootDirectory,
                    buildFileName(
                        mime = mime,
                    ),
                )

            val input =
                contentResolver
                    .openInputStream(
                        sourceUri,
                    )
                    ?: error(
                        "IMAGE_SOURCE_OPEN_FAILED",
                    )

            try {
                input.use { source ->
                    target
                        .outputStream()
                        .use { destination ->
                            source.copyTo(
                                destination,
                            )
                        }
                }

                target.absolutePath
            } catch (
                error: Throwable,
            ) {
                runCatching {
                    target.delete()
                }

                throw error
            }
        }
    }

    suspend fun delete(
        localPath: String,
    ) {
        withContext(
            Dispatchers.IO,
        ) {
            if (
                localPath.isBlank()
            ) {
                return@withContext
            }

            runCatching {
                File(
                    localPath,
                ).delete()
            }
        }
    }

    private fun ensureDirectory() {
        if (
            rootDirectory.exists()
        ) {
            return
        }

        check(
            rootDirectory.mkdirs() ||
                    rootDirectory.exists(),
        ) {
            "NOTE_IMAGE_DIRECTORY_CREATE_FAILED"
        }
    }

    private fun buildFileName(
        mime: String,
    ): String {
        return buildString {
            append(
                UUID.randomUUID()
                    .toString(),
            )

            append(
                ".",
            )

            append(
                extensionForMime(
                    mime = mime,
                )
            )
        }
    }

    private fun extensionForMime(
        mime: String,
    ): String {
        return when (
            mime.lowercase()
        ) {
            "image/png" ->
                "png"

            "image/webp" ->
                "webp"

            "image/heic",
            "image/heif" ->
                "heic"

            else ->
                "jpg"
        }
    }
}