package com.test.timeline.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(TimeLine::class), version = 1)
abstract class TimeLineDatabase : RoomDatabase() {

    abstract fun timeLineDao(): TimeLineDao

    companion object {
        private var INSTANCE: TimeLineDatabase? = null
        fun getInstance(context: Context): TimeLineDatabase? {
            if (INSTANCE == null) {
                synchronized(TimeLineDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TimeLineDatabase::class.java, "timeline.db")
                            .build()
                }
            }
            return INSTANCE
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}