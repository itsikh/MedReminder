package com.itsikh.medreminder.data.db

import androidx.room.TypeConverter
import com.itsikh.medreminder.data.model.LogStatus

class Converters {
    @TypeConverter fun fromLogStatus(v: LogStatus): String = v.name
    @TypeConverter fun toLogStatus(v: String): LogStatus = LogStatus.valueOf(v)
}
