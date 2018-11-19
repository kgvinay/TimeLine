package com.test.timeline.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update


@Dao
interface TimeLineDao {

    @Query("SELECT * FROM timeline WHERE dateString LIKE :date")
    fun getTimeLineForDate(date: String?): List<TimeLine>

    @Insert(onConflict = REPLACE)
    fun saveTimeLine(timeLine: TimeLine)

    @Update
    fun updateUsers(vararg updatedTimeLine: TimeLine)

}