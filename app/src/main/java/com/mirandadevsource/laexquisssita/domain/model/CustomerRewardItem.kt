package com.mirandadevsource.laexquisssita.domain.model

/** Reward enriched with the authenticated customer's current-semester eligibility. */
data class CustomerRewardItem(
    val reward: Reward,
    val alreadyRedeemedInPeriod: Boolean,
    val pointsMissing: Int,
) {
    val isAvailable: Boolean
        get() = !alreadyRedeemedInPeriod && pointsMissing == 0
}
