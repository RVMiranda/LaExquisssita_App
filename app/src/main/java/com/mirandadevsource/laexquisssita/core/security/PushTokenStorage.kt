package com.mirandadevsource.laexquisssita.core.security

interface PushTokenStorage {
    fun getFcmToken(): String?
    fun setFcmToken(token: String?)
}
