package com.example.model

enum class MediaCategory(val label: String) {
    ALL("All"),
    MOVIES_4K("4K Movies"),
    TV_SERIES("TV Series"),
    ANIME("Anime"),
    TRENDING("Trending")
}

data class StreamServer(
    val id: String,
    val name: String,
    val resolution: String,
    val codec: String,
    val pingMs: Int,
    val isAdFree: Boolean = true,
    val providerTag: String = "FrameTV • EpicSports",
    val streamUrl: String = "https://live.epicsports.in/?m=1",
    val videoStreamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
)

data class MediaTitle(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val qualityBadge: String,
    val durationOrSeasons: String,
    val category: MediaCategory,
    val genres: List<String>,
    val synopsis: String,
    val director: String,
    val cast: List<String>,
    val audioTracks: List<String>,
    val servers: List<StreamServer>,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val isTrending: Boolean = false
)

enum class FootballCompetition(
    val code: String,
    val displayName: String,
    val shortTag: String,
    val badgeColorHex: Long
) {
    ALL("ALL", "All Live Football", "ALL", 0xFF00FF87),
    UCL("UCL", "UEFA Champions League", "UCL", 0xFF00E5FF),
    EPL("EPL", "Premier League", "EPL", 0xFF9D4EDD),
    LALIGA("LIGA", "LaLiga EA Sports", "LaLiga", 0xFFFF5E3A),
    SERIE_A("SERIE_A", "Serie A Enilive", "Serie A", 0xFF0096FF),
    BUNDESLIGA("BUNDESLIGA", "Bundesliga", "Bundesliga", 0xFFFF2A55),
    INTER_MIAMI("INTER_MIAMI", "Inter Miami Live", "Inter Miami", 0xFFFF2A85),
    INTL_FRIENDLY("INTL_FRIENDLY", "International Friendly", "Intl Friendly", 0xFF00F5D4),
    INTL_CUP("INTL_CUP", "International Cup Competition", "Intl Cup", 0xFFFFD13B);

    companion object {
        val toggleableLeagues: List<FootballCompetition>
            get() = entries.filter { it != ALL }
    }
}

enum class CommentaryType(val label: String, val badgeColorHex: Long) {
    GOAL("GOAL!", 0xFF00FF87),
    MAJOR_CHANCE("CLOSE CHANCE", 0xFF00E5FF),
    VAR_CHECK("VAR REVIEW", 0xFFFFD13B),
    YELLOW_CARD("YELLOW CARD", 0xFFFFAB00),
    TACTICAL("TACTICAL SHIFT", 0xFF94A3B8),
    KICKOFF("KICKOFF", 0xFFFF2A85)
}

data class CommentaryEvent(
    val id: String,
    val minute: Int,
    val type: CommentaryType,
    val headline: String,
    val detail: String,
    val scorerOrPlayer: String? = null
)

data class MatchStats(
    val homePossession: Int,
    val awayPossession: Int,
    val homeShotsOnTarget: Int,
    val awayShotsOnTarget: Int,
    val homeXg: Float,
    val awayXg: Float,
    val homeCorners: Int,
    val awayCorners: Int,
    val homePassAccuracy: Int,
    val awayPassAccuracy: Int,
    val homeMomentum: Float // 0.1f .. 0.9f
)

data class FootballMatch(
    val id: String,
    val competition: FootballCompetition,
    val stageLabel: String,
    val homeTeam: String,
    val homeShort: String,
    val homeColorHex: Long,
    val awayTeam: String,
    val awayShort: String,
    val awayColorHex: Long,
    val homeScore: Int,
    val awayScore: Int,
    val minute: Int,
    val addedTime: Int = 0,
    val isLive: Boolean = true,
    val kickoffLabel: String,
    val venue: String,
    val attendance: String,
    val referee: String,
    val homeFormation: String,
    val awayFormation: String,
    val homeKeyPlayers: List<String>,
    val awayKeyPlayers: List<String>,
    val stats: MatchStats,
    val commentary: List<CommentaryEvent>,
    val streamServers: List<StreamServer>
)

data class GroundingSource(
    val title: String,
    val url: String
)

data class SearchGroundingResult(
    val query: String,
    val answer: String,
    val sources: List<GroundingSource>,
    val modelUsed: String,
    val timestampLabel: String
)

data class FootballThemeOption(
    val id: String,
    val rank: Int,
    val name: String,
    val subtitle: String,
    val bgTopHex: Long,
    val bgBottomHex: Long,
    val accentHex: Long,
    val secondaryAccentHex: Long,
    val isMessiMonochrome: Boolean = false
)

data class AvatarPreset(
    val id: String,
    val label: String,
    val badgeText: String,
    val primaryHex: Long,
    val secondaryHex: Long
)

enum class QualificationZone(val label: String, val indicatorColorHex: Long) {
    CHAMPIONS_OR_TITLE("UCL / Title Zone", 0xFF10B981),
    CONTINENTAL_PLAYOFF("Continental / Playoff", 0xFF38BDF8),
    RELEGATION("Relegation Zone", 0xFFEF4444),
    NONE("Mid-table", 0x00000000)
}

data class StandingRow(
    val rank: Int,
    val teamName: String,
    val shortCode: String,
    val badgeColorHex: Long,
    val matchesPlayed: Int,
    val goalDifference: Int,
    val points: Int,
    val qualificationZone: QualificationZone = QualificationZone.NONE,
    val isLivePlaying: Boolean = false,
    val liveScoreSummary: String? = null
)

data class LeagueStandingsTable(
    val competition: FootballCompetition,
    val seasonLabel: String,
    val officialWebUrl: String,
    val lastUpdatedLabel: String,
    val sourceLabel: String,
    val isLiveNetworkSynced: Boolean,
    val rows: List<StandingRow>
)

sealed class StandingsUiState {
    data class Loading(val previewTable: LeagueStandingsTable) : StandingsUiState()
    data class Success(val table: LeagueStandingsTable) : StandingsUiState()
    data class Error(val message: String, val fallbackTable: LeagueStandingsTable) : StandingsUiState()
}

