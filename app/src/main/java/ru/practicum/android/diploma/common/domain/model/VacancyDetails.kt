package ru.practicum.android.diploma.common.domain.model

data class VacancyDetails(
    val id: Long,
    val name: String,
    val salary: Salary?,
    val areaName: String,
    val employerName: String?,
    val employerLogoUrl240: String?,
    val experience: String?,
    val scheduleName: String,
    val description: String,
    val keySkills: List<String>,
    val address: Address?,
    val alternateUrl: String,
    val isFavorite: Boolean = false,
)
