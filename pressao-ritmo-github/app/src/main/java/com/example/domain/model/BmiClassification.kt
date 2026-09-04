package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class BmiClassification(
    val title: String,
    val rangeDescription: String,
    val color: Color
) {
    ABAIXO_DO_PESO(
        title = "Abaixo do peso",
        rangeDescription = "< 18,5",
        color = Color(0xFF0288D1)
    ),
    PESO_ADEQUADO(
        title = "Peso adequado",
        rangeDescription = "18,5 a 24,9",
        color = Color(0xFF2E7D32)
    ),
    SOBREPESO(
        title = "Sobrepeso",
        rangeDescription = "25,0 a 29,9",
        color = Color(0xFFF57F17)
    ),
    OBESIDADE_GRAU_1(
        title = "Obesidade grau 1",
        rangeDescription = "30,0 a 34,9",
        color = Color(0xFFE65100)
    ),
    OBESIDADE_GRAU_2(
        title = "Obesidade grau 2",
        rangeDescription = "35,0 a 39,9",
        color = Color(0xFFC62828)
    ),
    OBESIDADE_GRAU_3(
        title = "Obesidade grau 3",
        rangeDescription = "≥ 40,0",
        color = Color(0xFF880E4F)
    )
}
