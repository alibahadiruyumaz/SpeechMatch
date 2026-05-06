package com.example.speechmatch.presentation.placement

import com.example.speechmatch.data.local.entity.VocabularyEntity

data class PlacementUiState(
    val currentWordIndex: Int = 0,
    val testWords: List<VocabularyEntity> = emptyList(),
    val isTestFinished: Boolean = false,
    val calculatedLevel: String = "A1",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)