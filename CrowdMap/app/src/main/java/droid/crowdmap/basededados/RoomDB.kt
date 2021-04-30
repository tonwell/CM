package droid.crowdmap.basededados

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

const val DB_NAME = "crowdmap_database"

@Database(entities = [PhoneDataEntity::class], version = 1)
abstract class CrowdMapDatabase: RoomDatabase() {
    abstract fun phoneDataDao(): PhoneDataDao

    companion object {
        @Volatile
        private var INSTANCE: CrowdMapDatabase? = null

        fun getDatabase(context: Context): CrowdMapDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        CrowdMapDatabase::class.java,
                        DB_NAME
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}