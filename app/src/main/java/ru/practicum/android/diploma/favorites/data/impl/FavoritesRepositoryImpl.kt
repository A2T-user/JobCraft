package ru.practicum.android.diploma.favorites.data.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.common.data.converter.FavoriteVacancyDbConverter
import ru.practicum.android.diploma.common.data.db.AppDatabase
import ru.practicum.android.diploma.common.domain.model.VacancyDetails
import ru.practicum.android.diploma.common.domain.model.VacancyFromList
import ru.practicum.android.diploma.favorites.domain.FavoritesRepository

class FavoritesRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val vacancyDbConverter: FavoriteVacancyDbConverter
) : FavoritesRepository {

    override suspend fun addVacancyToFavorites(vacancy: VacancyDetails) {
        appDatabase.favoriteVacanciesDao().insertVacancy(vacancyDbConverter.convert(vacancy))
    }

    override suspend fun updateVacancyInFavorites(vacancy: VacancyDetails) {
        appDatabase.favoriteVacanciesDao().updateVacancy(vacancyDbConverter.convert(vacancy))
    }

    override suspend fun removeVacancyFromFavorites(vacancyId: Long) {
        appDatabase.favoriteVacanciesDao().removeVacancy(vacancyId)
    }

    override fun getFavoriteVacancies(): Flow<List<VacancyFromList>> =
        appDatabase.favoriteVacanciesDao().getVacancies()
            .map { vacancies -> vacancies.map { vacancyDbConverter.convertToVacancyFromList(it) } }

}
