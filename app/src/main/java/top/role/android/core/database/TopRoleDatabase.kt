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
import top.role.android.core.database.note.NoteTypeConverters

@Database(
    entities = [
        LocalNoteEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(
    NoteTypeConverters::class,
)
abstract class TopRoleDatabase :
    RoomDatabase() {

    abstract fun localNoteDao():
            LocalNoteDao

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
                )
                .build()
        }
    }
}