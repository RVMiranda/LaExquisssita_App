package com.mirandadevsource.laexquisssita.domain.repository

import com.mirandadevsource.laexquisssita.domain.model.CustomerProfile
import com.mirandadevsource.laexquisssita.domain.model.PointMovement
import com.mirandadevsource.laexquisssita.domain.model.CustomerRewardItem

interface LoyaltyRepository {
    suspend fun getPermanentQrToken(): Result<String>
    suspend fun getCustomerProfile(): Result<CustomerProfile>
    suspend fun updateCustomerProfile(nombre: String, telefono: String?): Result<CustomerProfile>
    suspend fun getActiveRewards(customerPoints: Int): Result<List<CustomerRewardItem>>
    suspend fun getPointsHistory(): Result<List<PointMovement>>
}

