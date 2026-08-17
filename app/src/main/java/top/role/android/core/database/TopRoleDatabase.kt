package top.role.android.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import top.role.android.core.database.note.LocalNoteDao
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.dao.LocalNoteImageDao
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.database.note.NoteTypeConverters

@Database(
    entities = [
        LocalNoteEntity::class,
        LocalNoteImageEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(
    NoteTypeConverters::class,
)
abstract class TopRoleDatabase :
    RoomDatabase() {

    abstract fun localNoteDao():
            LocalNoteDao

    abstract fun localNoteImageDao():
            LocalNoteImageDao

    companion object {

        /*
         * version 1 → 2
         *
         * 增加 local-first lifecycle target。
         */
        private val MIGRATION_1_2 =
            object : Migration(
                1,
                2,
            ) {
                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL(
                        """
                        ALTER TABLE local_notes
                        ADD COLUMN pendingRemoteStatus TEXT
                        """.trimIndent()
                    )
                }
            }

        /*
         * version 2 → 3
         *
         * 增加 Conflict 时的远端 snapshot。
         *
         * nullable column：
         * 已有 Note 自动得到 NULL，
         * 无需额外回填。
         */
        private val MIGRATION_2_3 =
            object : Migration(
                2,
                3,
            ) {
                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL(
                        """
                        ALTER TABLE local_notes
                        ADD COLUMN conflictSnapshotJson TEXT
                        """.trimIndent()
                    )
                }
            }

        /*
         * version 3 → 4
         *
         * 新增 Note 图片附件本地表。
         *
         * 图片附件与 Note 正文同步链分离：
         * - Note 本身继续维护 version / snapshot / conflict
         * - 图片独立维护本地文件与上传状态
         */
        private val MIGRATION_3_4 =
            object : Migration(
                3,
                4,
            ) {
                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS local_note_images (
                            localImageKey TEXT NOT NULL,
                            ownerUserId TEXT NOT NULL,
                            noteLocalKey TEXT NOT NULL,
                            imageId TEXT,
                            localPath TEXT NOT NULL,
                            mime TEXT NOT NULL,
                            width INTEGER,
                            height INTEGER,
                            bytes INTEGER NOT NULL,
                            uploadState TEXT NOT NULL,
                            uploadError TEXT,
                            position INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            PRIMARY KEY(localImageKey)
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS
                        index_local_note_images_ownerUserId_noteLocalKey
                        ON local_note_images(
                            ownerUserId,
                            noteLocalKey
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS
                        index_local_note_images_ownerUserId_imageId
                        ON local_note_images(
                            ownerUserId,
                            imageId
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS
                        index_local_note_images_uploadState
                        ON local_note_images(
                            uploadState
                        )
                        """.trimIndent()
                    )
                }
            }

        fun create(
            context: Context,
        ): TopRoleDatabase {
            return Room
                .databaseBuilder(
                    context.applicationContext,
                    TopRoleDatabase::class.java,
                    "toprole.db",
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                )
                .build()
        }
    }
}