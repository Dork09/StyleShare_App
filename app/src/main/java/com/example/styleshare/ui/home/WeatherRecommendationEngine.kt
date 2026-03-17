package com.example.styleshare.ui.home

import com.example.styleshare.model.Look

class WeatherRecommendationEngine {

    fun recommend(looks: List<Look>, weather: HomeWeather?, limit: Int = 5): List<Look> {
        if (looks.isEmpty()) return emptyList()
        if (weather == null) return fallbackLooks(looks, limit)

        val profile = createProfile(weather)
        val scoredLooks = looks.map { look -> look to scoreLook(look, profile) }
        val matchingLooks = scoredLooks.filter { it.second > 0 }

        return if (matchingLooks.isNotEmpty()) {
            matchingLooks
                .sortedWith(
                    compareByDescending<Pair<Look, Int>> { it.second }
                        .thenByDescending { it.first.likesCount }
                        .thenByDescending { it.first.createdAt }
                )
                .map { it.first }
                .take(limit)
        } else {
            fallbackLooks(looks, limit)
        }
    }

    private fun scoreLook(look: Look, profile: RecommendationProfile): Int {
        val normalizedTags = look.tags.map(::normalize).toSet()
        if (normalizedTags.isEmpty()) return 0

        var score = 0
        normalizedTags.forEach { tag ->
            score += when {
                tag in profile.strongSeasonalTags -> 3
                tag in profile.directWeatherTags -> 2
                tag in profile.generalTags -> 1
                else -> 0
            }

            if (profile.isRainy && tag in profile.conditionBoostTags) {
                score += 2
            }
            if (profile.isSunnyHot && tag in profile.conditionBoostTags) {
                score += 2
            }
        }
        return score
    }

    private fun createProfile(weather: HomeWeather): RecommendationProfile {
        val temp = weather.temperatureCelsius
        val condition = normalize(weather.conditionLabel)

        val baseProfile = when {
            temp <= 10 -> RecommendationProfile(
                strongSeasonalTags = setOf("חורף", "קר"),
                directWeatherTags = setOf("מעיל", "גקט", "סוודר", "גשם"),
                generalTags = setOf("שכבות", "קריר", "ארוך")
            )
            temp in 11.0..17.99 -> RecommendationProfile(
                strongSeasonalTags = setOf("חורף", "סתיו"),
                directWeatherTags = setOf("גקט", "שכבות", "קריר"),
                generalTags = setOf("יומיומי", "קליל")
            )
            temp in 18.0..24.99 -> RecommendationProfile(
                strongSeasonalTags = setOf("אביב", "סתיו"),
                directWeatherTags = setOf("יומיומי", "קליל"),
                generalTags = setOf("שכבות", "מעבר")
            )
            else -> RecommendationProfile(
                strongSeasonalTags = setOf("קיץ", "חם"),
                directWeatherTags = setOf("גופיה", "בגד ים", "קליל"),
                generalTags = setOf("יומיומי", "קצר", "נושם")
            )
        }

        val isRainy = condition in rainyConditions
        val isSunnyHot = temp >= 25 && condition in sunnyConditions

        return baseProfile.copy(
            conditionBoostTags = when {
                isRainy -> setOf("גשם", "מעיל", "גקט", "חורף")
                isSunnyHot -> setOf("קיץ", "קליל", "בגד ים", "גופיה")
                else -> emptySet()
            },
            isRainy = isRainy,
            isSunnyHot = isSunnyHot
        )
    }

    private fun fallbackLooks(looks: List<Look>, limit: Int): List<Look> {
        return looks.sortedWith(
            compareByDescending<Look> { it.likesCount }
                .thenByDescending { it.createdAt }
        ).take(limit)
    }

    private fun normalize(value: String): String {
        return value.trim()
            .removePrefix("#")
            .replace("'", "")
            .replace("׳", "")
            .replace("\"", "")
            .replace("-", " ")
            .lowercase()
    }

    private data class RecommendationProfile(
        val strongSeasonalTags: Set<String>,
        val directWeatherTags: Set<String>,
        val generalTags: Set<String>,
        val conditionBoostTags: Set<String> = emptySet(),
        val isRainy: Boolean = false,
        val isSunnyHot: Boolean = false
    )

    private companion object {
        val rainyConditions = setOf("rain", "drizzle", "showers", "thunderstorm", "storm")
        val sunnyConditions = setOf("clear", "sunny", "mainly clear")
    }
}
