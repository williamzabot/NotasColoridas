package com.williamzabot.notascoloridas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.williamzabot.notascoloridas.data.db.dao.NoteDAO
import com.williamzabot.notascoloridas.data.db.entity.Note

@Database(entities = [Note::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract val noteDAO: NoteDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance: AppDatabase? = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }

                return instance
            }
        }
    }
}