package top.role.android.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import top.role.android.core.database.dao.LocalNoteImageDao
import top.role.android.core.database.dao.LocalNoteMediaSyncDao
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.database.entity.LocalNoteMediaSyncEntity
import top.role.android.core.database.note.LocalNoteDao
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.NoteTypeConverters

@Database(
    entities = [
        LocalNoteEntity::class,
        LocalNoteImageEntity::class,
        LocalNoteMediaSyncEntity::class,
    ],
    version = 8,
    exportSchema = true,
)
@TypeConverters(
    NoteTypeConverters::class,
)
abstract class TopRoleDatabase :
    RoomDatabase() {

    abstract fun localNoteDao(): LocalNoteDao

    abstract fun localNoteImageDao(): LocalNoteImageDao

    abstract fun localNoteMediaSyncDao(): LocalNoteMediaSyncDao

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

        /*
         * version 4 → 5
         *
         * 新增 Note Media 独立同步状态表。
         *
         * Content Sync 与 Media Sync 从这一版开始
         * 正式成为两个独立的 local-first 同步域：
         *
         * Content：
         * - LocalNoteEntity
         * - version
         * - pendingSnapshotJson
         * - conflictSnapshotJson
         *
         * Media：
         * - LocalNoteImageEntity 作为当前本地媒体事实
         * - LocalNoteMediaSyncEntity 维护关系同步状态
         * - mediaVersion
         * - pending media snapshot
         * - media conflict
         *
         * 本 migration 只创建状态表。
         *
         * 不扫描、不迁移、不补偿历史
         * local_note_images 数据。
         */
        private val MIGRATION_4_5 =
            object : Migration(
                4,
                5,
            ) {
                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS local_note_media_sync (
                            syncKey TEXT NOT NULL,
                            ownerUserId TEXT NOT NULL,
                            noteLocalKey TEXT NOT NULL,
                            noteId TEXT,
                            remoteVersion INTEGER,
                            remoteUpdatedAt INTEGER,
                            syncStateRaw TEXT NOT NULL,
                            pendingSnapshotJson TEXT,
                            conflictSnapshotJson TEXT,
                            lastSyncError TEXT,
                            localUpdatedAt INTEGER NOT NULL,
                            PRIMARY KEY(syncKey)
                        )
                        """.trimIndent()
                    )

                    /*
                     * 每个 owner + noteLocalKey
                     * 只能存在一条 Media Sync 状态。
                     */
                    db.execSQL(
                        """
                        CREATE UNIQUE INDEX IF NOT EXISTS
                        index_local_note_media_sync_ownerUserId_noteLocalKey
                        ON local_note_media_sync(
                            ownerUserId,
                            noteLocalKey
                        )
                        """.trimIndent()
                    )

                    /*
                     * Note 获得正式 noteId 后，
                     * 可以通过 remote identity 定位 media sync state。
                     *
                     * noteId 对新建尚未同步的 Note 可以为空。
                     */
                    db.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS
                        index_local_note_media_sync_ownerUserId_noteId
                        ON local_note_media_sync(
                            ownerUserId,
                            noteId
                        )
                        """.trimIndent()
                    )

                    /*
                     * Coordinator / Media Push Service
                     * 按账号查找 Pending / Waiting。
                     */
                    db.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS
                        index_local_note_media_sync_ownerUserId_syncStateRaw
                        ON local_note_media_sync(
                            ownerUserId,
                            syncStateRaw
                        )
                        """.trimIndent()
                    )
                }
            }

        val MIGRATION_5_6 =
            object : Migration(
                5,
                6,
            ) {
                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL(
                        """
                CREATE TABLE local_note_images_new (
                    localImageKey TEXT NOT NULL PRIMARY KEY,
                    ownerUserId TEXT NOT NULL,
                    noteLocalKey TEXT NOT NULL,
                    imageId TEXT,
                    localPath TEXT,
                    mime TEXT,
                    width INTEGER,
                    height INTEGER,
                    bytes INTEGER,
                    uploadState TEXT NOT NULL,
                    uploadError TEXT,
                    position INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                INSERT INTO local_note_images_new (
                    localImageKey,
                    ownerUserId,
                    noteLocalKey,
                    imageId,
                    localPath,
                    mime,
                    width,
                    height,
                    bytes,
                    uploadState,
                    uploadError,
                    position,
                    createdAt,
                    updatedAt
                )
                SELECT
                    localImageKey,
                    ownerUserId,
                    noteLocalKey,
                    imageId,
                    localPath,
                    mime,
                    width,
                    height,
                    bytes,
                    uploadState,
                    uploadError,
                    position,
                    createdAt,
                    updatedAt
                FROM local_note_images
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                DROP TABLE local_note_images
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                ALTER TABLE local_note_images_new
                RENAME TO local_note_images
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                CREATE INDEX index_local_note_images_ownerUserId_noteLocalKey
                ON local_note_images(
                    ownerUserId,
                    noteLocalKey
                )
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                CREATE INDEX index_local_note_images_ownerUserId_imageId
                ON local_note_images(
                    ownerUserId,
                    imageId
                )
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                CREATE INDEX index_local_note_images_uploadState
                ON local_note_images(
                    uploadState
                )
                """.trimIndent()
                    )
                }
            }

        /*
         * version 6 → 7
         *
         * 为 Android Note 增加 BlockContent v1 存储。
         *
         * 当前内容：
         * contentJson
         *
         * 服务端确认基线：
         * syncedContentJson
         *
         * 本次仅扩展 schema。
         *
         * 旧 text / syncedText 继续保留，
         * Pending / Conflict / Media 均不修改。
         */
        private val MIGRATION_6_7 =
            object : Migration(6, 7) {

                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL(
                        """
                ALTER TABLE local_notes
                ADD COLUMN contentJson TEXT
                """.trimIndent()
                    )

                    db.execSQL(
                        """
                ALTER TABLE local_notes
                ADD COLUMN syncedContentJson TEXT
                """.trimIndent()
                    )
                }
            }

        private val MIGRATION_7_8 =
            object : Migration(7, 8) {

                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {

                    // 1. local_notes
                    db.execSQL(
                        """
                        ALTER TABLE local_notes
                        RENAME COLUMN ownerUserId
                        TO localOwnerUserId
                        """.trimIndent()
                    )

                    // 删除旧索引
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_notes_ownerUserId"
                    )
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_notes_ownerUserId_noteId"
                    )
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_notes_ownerUserId_clientKey"
                    )
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_notes_ownerUserId_dayKey"
                    )
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_notes_ownerUserId_onlyDay_dayKey"
                    )
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_notes_ownerUserId_syncState"
                    )

                    // 创建新索引
                    db.execSQL(
                        "CREATE INDEX index_local_notes_localOwnerUserId ON local_notes(localOwnerUserId)"
                    )
                    db.execSQL(
                        "CREATE INDEX index_local_notes_localOwnerUserId_noteId ON local_notes(localOwnerUserId, noteId)"
                    )
                    db.execSQL(
                        "CREATE INDEX index_local_notes_localOwnerUserId_clientKey ON local_notes(localOwnerUserId, clientKey)"
                    )
                    db.execSQL(
                        "CREATE INDEX index_local_notes_localOwnerUserId_dayKey ON local_notes(localOwnerUserId, dayKey)"
                    )
                    db.execSQL(
                        "CREATE INDEX index_local_notes_localOwnerUserId_onlyDay_dayKey ON local_notes(localOwnerUserId, onlyDay, dayKey)"
                    )
                    db.execSQL(
                        "CREATE INDEX index_local_notes_localOwnerUserId_syncState ON local_notes(localOwnerUserId, syncState)"
                    )

                    // local_note_images

                    db.execSQL(
                        """
                        ALTER TABLE local_note_images
                        RENAME COLUMN ownerUserId
                        TO localOwnerUserId
                        """.trimIndent()
                    )

                    // 删除旧身份索引
                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_note_images_ownerUserId_noteLocalKey"
                    )

                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_note_images_ownerUserId_imageId"
                    )

                    // 创建新身份索引
                    db.execSQL(
                       """
                        CREATE INDEX index_local_note_images_localOwnerUserId_noteLocalKey
                        ON local_note_images(localOwnerUserId, noteLocalKey)
                        """.trimIndent()
                    )

                    db.execSQL(
                      """
                        CREATE INDEX index_local_note_images_localOwnerUserId_imageId
                        ON local_note_images(localOwnerUserId, imageId)
                      """.trimIndent()
                    )

                    // uploadState 索引不变

                    // 3. local_note_media_sync
                    db.execSQL(
                        """
                        ALTER TABLE local_note_media_sync
                        RENAME COLUMN ownerUserId
                        TO localOwnerUserId
                        """.trimIndent()
                    )

                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_note_media_sync_ownerUserId_noteLocalKey"
                    )

                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_note_media_sync_ownerUserId_noteId"
                    )

                    db.execSQL(
                        "DROP INDEX IF EXISTS index_local_note_media_sync_ownerUserId_syncStateRaw"
                    )

                    db.execSQL(
                        """
                        CREATE UNIQUE INDEX index_local_note_media_sync_localOwnerUserId_noteLocalKey
                        ON local_note_media_sync(localOwnerUserId, noteLocalKey)
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE INDEX index_local_note_media_sync_localOwnerUserId_noteId
                        ON local_note_media_sync(localOwnerUserId, noteId)
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE INDEX index_local_note_media_sync_localOwnerUserId_syncStateRaw
                        ON local_note_media_sync(localOwnerUserId, syncStateRaw)
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
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                )
                .build()
        }
    }
}