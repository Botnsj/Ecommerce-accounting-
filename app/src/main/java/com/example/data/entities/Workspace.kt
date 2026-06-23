package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subscriptionPlan: String = "Pro", // Basic, Pro, Enterprise
    val gstNumber: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val currentBalance: Double = 500000.0 // Starting cash balance
)
