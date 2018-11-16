package com.test.timeline.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey

data class TimeLine(
        @PrimaryKey var date: String,
        @ColumnInfo(name = "lat") var lat: String?,
        @ColumnInfo(name = "long") var long: String?,
        @ColumnInfo(name = "name") var name: String?,
        @ColumnInfo(name = "startTime") var startTime: String?,
        @ColumnInfo(name = "endTime") var endTime: String?

)