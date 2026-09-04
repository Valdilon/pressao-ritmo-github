package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurements",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["measuredAtEpoch"]),
        Index(value = ["measuredAt"])
    ]
)
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val profileId: Long = 1L, // Associates measurement with a specific user profile
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int,
    val measuredAt: String, // ISO 8601 string (e.g. 2026-08-12T12:42:00.000Z)
    val measuredAtEpoch: Long, // Epoch timestamp in milliseconds for fast sorting & ranges
    val observation: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

