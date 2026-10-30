package com.mirandadevsource.laexquisssita.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirandadevsource.laexquisssita.domain.model.Promotion
import com.mirandadevsource.laexquisssita.domain.repository.PromotionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val promotions: List<Promotion> = emptyList(),
    val selectedCategory: String = ALL_CATEGORY,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
) {
    companion object {
        const val ALL_CATEGORY = "Todas"
        val categories = listOf(ALL_CATEGORY, "Cafetería", "Repostería", "Especialidades")
    }
}

class HomeViewModel(private val repository: PromotionRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { loadData() }

    fun loadData() = fetch(isRefresh = false)

    fun refresh() = fetch(isRefresh = true)

    fun onCategorySelected(category: String) {
        if (category in HomeUiState.categories) _state.update { it.copy(selectedCategory = category) }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    private fun fetch(isRefresh: Boolean) {
        _state.update {
            it.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            val result = repository.getActivePromotions()
            result.onSuccess { feed ->
                _state.update {
                    it.copy(
                        promotions = feed.promotions,
                        isOffline = feed.isOffline,
                        isLoading = false,
                        isRefreshing = false,
                        hasLoaded = true,
                    )
                }
            }.onFailure { error ->
                if (error is CancellationException) throw error
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = true,
                        errorMessage = "No pudimos cargar las promociones. Comprueba tu conexión e inténtalo de nuevo.",
                        hasLoaded = true,
                    )
                }
            }
        }
    }
}
