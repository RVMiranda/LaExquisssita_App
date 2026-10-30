package com.mirandadevsource.laexquisssita.domain.model

data class ProfileInput(val name: String, val phone: String?) {
    companion object {
        fun validated(name: String, phone: String?): ProfileInput {
            val cleanName = name.trim()
            val cleanPhone = phone?.trim()?.takeIf(String::isNotEmpty)
            require(cleanName.isNotEmpty() && cleanName.length <= 160)
            require(cleanPhone == null || cleanPhone.matches(Regex("[0-9]{10}")))
            return ProfileInput(cleanName, cleanPhone)
        }
    }
}
