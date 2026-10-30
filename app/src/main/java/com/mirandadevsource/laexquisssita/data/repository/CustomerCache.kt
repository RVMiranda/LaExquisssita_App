package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.domain.model.CustomerProfile

interface CustomerCache {
    fun profile(userId: String): CustomerProfile?
    fun saveProfile(userId: String, profile: CustomerProfile)
    fun qr(userId: String): String?
    fun saveQr(userId: String, token: String)
    fun clearCustomerData()
}
