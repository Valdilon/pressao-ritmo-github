package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class PressureClassification(
    val title: String,
    val description: String,
    val color: Color,
    val level: Int,
    val isEmergencyAlert: Boolean = false
) {
    NORMAL(
        title = "PA normal",
        description = "Sistólica < 120 e Diastólica < 80 mmHg",
        color = Color(0xFF2E7D32), // Deep Green
        level = 0
    ),
    PRE_HIPERTENSAO(
        title = "Pré-hipertensão",
        description = "Sistólica 120–139 ou Diastólica 80–89 mmHg",
        color = Color(0xFFF9A825), // Updated Amber for contrast
        level = 1
    ),
    HIPERTENSAO_ESTAGIO_1(
        title = "Hipertensão estágio 1",
        description = "Sistólica 140–159 ou Diastólica 90–99 mmHg",
        color = Color(0xFFE65100), // Vibrant Orange
        level = 2
    ),
    HIPERTENSAO_ESTAGIO_2(
        title = "Hipertensão estágio 2",
        description = "Sistólica 160–179 ou Diastólica 100–109 mmHg",
        color = Color(0xFFC62828), // Strong Crimson Red
        level = 3
    ),
    HIPERTENSAO_ESTAGIO_3(
        title = "Hipertensão estágio 3",
        description = "Sistólica ≥ 180 ou Diastólica ≥ 110 mmHg",
        color = Color(0xFF880E4F), // Deep Wine/Burgundy
        level = 4,
        isEmergencyAlert = true
    );

    companion object {
        const val STAGE_3_ALERT = "Repita a aferição após repouso. Se persistir ou houver sintomas, procure atendimento imediatamente."
        const val GENERAL_DISCLAIMER = "Estes indicadores são informativos e não substituem diagnóstico médico. Em caso de sintomas ou valores muito elevados, procure atendimento."
        const val DIASTOLIC_ERROR_MSG = "A pressão diastólica deve ser menor que a sistólica."
    }
}
