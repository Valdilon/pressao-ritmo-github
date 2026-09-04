package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val sex: String,
    val birthDate: String?, // Format: yyyy-MM-dd
    val age: Int?,          // Legacy / derived value
    val weight: Double,     // in kg
    val height: Double,     // in meters
    val bmi: Double?,       // Derived value
    val isDefault: Boolean = false,
    val avatarColorHex: String = "#6750A4",
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

