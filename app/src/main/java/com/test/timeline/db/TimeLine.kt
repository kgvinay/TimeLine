package com.test.timeline.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "timeline")
data class TimeLine(@PrimaryKey var dateString: String = "",
                       var name: String = "",
                       var lat: Double = 0.0,
                       var lon: Double = 0.0,
                       var startTime: String = "",
                       var endTime: String = ""
)
{
    @Ignore
    constructor():this("","",0.0,0.0,"","")
}