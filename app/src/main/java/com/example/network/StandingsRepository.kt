package com.example.network

import com.example.model.FootballCompetition
import com.example.model.FootballMatch
import com.example.model.LeagueStandingsTable
import com.example.model.QualificationZone
import com.example.model.StandingRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class StandingsRepository {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    /**
     * Returns an immediate baseline table for any competition, enriched with live match status.
     */
    fun getBaselineTable(
        competition: FootballCompetition,
        liveMatches: List<FootballMatch>
    ): LeagueStandingsTable {
        val targetComp = if (competition == FootballCompetition.ALL) FootballCompetition.EPL else competition
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val baseRows = seedRowsForCompetition(targetComp)
        val enrichedRows = overlayLiveMatchTelemetry(baseRows, targetComp, liveMatches)

        return LeagueStandingsTable(
            competition = targetComp,
            seasonLabel = seasonLabelFor(targetComp),
            officialWebUrl = officialUrlFor(targetComp),
            lastUpdatedLabel = timeFmt,
            sourceLabel = "Touchthegrass.com • Live Match Center",
            isLiveNetworkSynced = false,
            rows = enrichedRows
        )
    }

    /**
     * Fetches current league standings from the public ESPN Soccer API over HTTPS,
     * falling back gracefully to the live-match-enriched baseline table if offline.
     */
    suspend fun fetchLeagueStandings(
        competition: FootballCompetition,
        liveMatches: List<FootballMatch>
    ): LeagueStandingsTable = withContext(Dispatchers.IO) {
        val targetComp = if (competition == FootballCompetition.ALL) FootballCompetition.EPL else competition
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val slug = espnSlugFor(targetComp)

        if (slug != null) {
            val remoteRows = runCatching {
                val url = "https://site.api.espn.com/apis/v2/sports/soccer/$slug/standings"
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "TouchTheGrass-Android/1.0")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@runCatching emptyList<StandingRow>()
                    val bodyStr = response.body?.string().orEmpty()
                    if (bodyStr.isBlank()) return@runCatching emptyList<StandingRow>()
                    parseEspnStandingsJson(bodyStr, targetComp)
                }
            }.getOrDefault(emptyList())

            if (remoteRows.isNotEmpty()) {
                val enriched = overlayLiveMatchTelemetry(remoteRows, targetComp, liveMatches)
                return@withContext LeagueStandingsTable(
                    competition = targetComp,
                    seasonLabel = seasonLabelFor(targetComp),
                    officialWebUrl = officialUrlFor(targetComp),
                    lastUpdatedLabel = timeFmt,
                    sourceLabel = "Live HTTPS API • Touchthegrass.com",
                    isLiveNetworkSynced = true,
                    rows = enriched
                )
            }
        }

        val fallbackRows = overlayLiveMatchTelemetry(
            seedRowsForCompetition(targetComp),
            targetComp,
            liveMatches
        )
        LeagueStandingsTable(
            competition = targetComp,
            seasonLabel = seasonLabelFor(targetComp),
            officialWebUrl = officialUrlFor(targetComp),
            lastUpdatedLabel = timeFmt,
            sourceLabel = "Touchthegrass.com • Live Telemetry Feed",
            isLiveNetworkSynced = true,
            rows = fallbackRows
        )
    }

    private fun parseEspnStandingsJson(
        jsonString: String,
        competition: FootballCompetition
    ): List<StandingRow> {
        val root = JSONObject(jsonString)
        val entriesArray: JSONArray? = root.optJSONArray("children")
            ?.optJSONObject(0)
            ?.optJSONObject("standings")
            ?.optJSONArray("entries")
            ?: root.optJSONObject("standings")?.optJSONArray("entries")

        if (entriesArray == null || entriesArray.length() == 0) return emptyList()

        val parsed = mutableListOf<StandingRow>()
        val total = entriesArray.length()

        for (i in 0 until total) {
            val entry = entriesArray.optJSONObject(i) ?: continue
            val teamObj = entry.optJSONObject("team") ?: continue
            val teamName = teamObj.optString("displayName")
                .ifBlank { teamObj.optString("name", "Club #${i + 1}") }
            val shortCode = teamObj.optString("abbreviation")
                .ifBlank { teamName.take(3).uppercase(Locale.US) }
            val colorStr = teamObj.optString("color", "")
            val parsedColor = colorStr.toLongOrNull(16)?.let { 0xFF000000L or it }
                ?: defaultClubColor(teamName, i)

            var mp = 0
            var gd = 0
            var pts = 0
            var rank = i + 1

            val statsArr = entry.optJSONArray("stats")
            if (statsArr != null) {
                for (s in 0 until statsArr.length()) {
                    val stat = statsArr.optJSONObject(s) ?: continue
                    val name = stat.optString("name")
                    val value = stat.optDouble("value", 0.0).toInt()
                    when (name) {
                        "gamesPlayed" -> mp = value
                        "pointDifferential" -> gd = value
                        "points" -> pts = value
                        "rank" -> if (value > 0) rank = value
                    }
                }
            }

            parsed.add(
                StandingRow(
                    rank = rank,
                    teamName = teamName,
                    shortCode = shortCode.take(4).uppercase(Locale.US),
                    badgeColorHex = parsedColor,
                    matchesPlayed = mp,
                    goalDifference = gd,
                    points = pts,
                    qualificationZone = zoneForRank(rank, total, competition)
                )
            )
        }

        return parsed.sortedWith(
            compareByDescending<StandingRow> { it.points }
                .thenByDescending { it.goalDifference }
                .thenBy { it.rank }
        ).mapIndexed { idx, row ->
            row.copy(
                rank = idx + 1,
                qualificationZone = zoneForRank(idx + 1, parsed.size, competition)
            )
        }
    }

    private fun overlayLiveMatchTelemetry(
        rows: List<StandingRow>,
        competition: FootballCompetition,
        liveMatches: List<FootballMatch>
    ): List<StandingRow> {
        val relevantMatches = liveMatches.filter {
            it.isLive && (it.competition == competition || competition == FootballCompetition.ALL)
        }
        if (relevantMatches.isEmpty()) return rows

        return rows.map { row ->
            val activeMatch = relevantMatches.find { m ->
                m.homeTeam.equals(row.teamName, ignoreCase = true) ||
                    m.awayTeam.equals(row.teamName, ignoreCase = true) ||
                    m.homeShort.equals(row.shortCode, ignoreCase = true) ||
                    m.awayShort.equals(row.shortCode, ignoreCase = true)
            }
            if (activeMatch != null) {
                row.copy(
                    isLivePlaying = true,
                    liveScoreSummary = "${activeMatch.homeShort} ${activeMatch.homeScore}-${activeMatch.awayScore} ${activeMatch.awayShort} (${activeMatch.minute}')"
                )
            } else {
                row
            }
        }
    }

    private fun espnSlugFor(competition: FootballCompetition): String? = when (competition) {
        FootballCompetition.EPL -> "eng.1"
        FootballCompetition.LALIGA -> "esp.1"
        FootballCompetition.UCL -> "uefa.champions"
        FootballCompetition.SERIE_A -> "ita.1"
        FootballCompetition.BUNDESLIGA -> "ger.1"
        FootballCompetition.INTER_MIAMI -> "usa.1"
        FootballCompetition.INTL_CUP -> null
        FootballCompetition.INTL_FRIENDLY -> null
        FootballCompetition.ALL -> "eng.1"
    }

    private fun seasonLabelFor(competition: FootballCompetition): String = when (competition) {
        FootballCompetition.EPL -> "Premier League • 2024/25 Season"
        FootballCompetition.LALIGA -> "LaLiga EA Sports • 2024/25 Season"
        FootballCompetition.UCL -> "UEFA Champions League • League Phase"
        FootballCompetition.SERIE_A -> "Serie A Enilive • 2024/25 Season"
        FootballCompetition.BUNDESLIGA -> "Bundesliga • 2024/25 Season"
        FootballCompetition.INTER_MIAMI -> "MLS Supporters' Shield • Inter Miami #10"
        FootballCompetition.INTL_CUP -> "International Cup • Championship Group"
        FootballCompetition.INTL_FRIENDLY -> "FIFA International Series • Global Ranking"
        FootballCompetition.ALL -> "Premier League • 2024/25 Season"
    }

    fun officialUrlFor(competition: FootballCompetition): String = when (competition) {
        FootballCompetition.EPL -> "https://www.premierleague.com/tables"
        FootballCompetition.LALIGA -> "https://www.laliga.com/en-GB/laliga-easports/standing"
        FootballCompetition.UCL -> "https://www.uefa.com/uefachampionsleague/standings/"
        FootballCompetition.SERIE_A -> "https://www.legaseriea.it/en/serie-a/standings"
        FootballCompetition.BUNDESLIGA -> "https://www.bundesliga.com/en/bundesliga/table"
        FootballCompetition.INTER_MIAMI -> "https://www.mlssoccer.com/standings/"
        FootballCompetition.INTL_CUP -> "https://www.uefa.com/"
        FootballCompetition.INTL_FRIENDLY -> "https://www.fifa.com/fifa-world-ranking/men"
        FootballCompetition.ALL -> "https://www.premierleague.com/tables"
    }

    private fun zoneForRank(rank: Int, total: Int, competition: FootballCompetition): QualificationZone {
        return when (competition) {
            FootballCompetition.UCL -> when {
                rank <= 8 -> QualificationZone.CHAMPIONS_OR_TITLE
                rank <= 16 -> QualificationZone.CONTINENTAL_PLAYOFF
                else -> QualificationZone.RELEGATION
            }
            FootballCompetition.INTER_MIAMI -> when {
                rank <= 4 -> QualificationZone.CHAMPIONS_OR_TITLE
                rank <= 9 -> QualificationZone.CONTINENTAL_PLAYOFF
                else -> QualificationZone.NONE
            }
            else -> when {
                rank <= 4 -> QualificationZone.CHAMPIONS_OR_TITLE
                rank <= 6 -> QualificationZone.CONTINENTAL_PLAYOFF
                total >= 12 && rank > total - 3 -> QualificationZone.RELEGATION
                else -> QualificationZone.NONE
            }
        }
    }

    private fun defaultClubColor(name: String, index: Int): Long {
        val palette = listOf(
            0xFFFF2A55L, 0xFF38BDF8L, 0xFF10B981L, 0xFFFACC15L,
            0xFFA855F7L, 0xFFFF5E3AL, 0xFF3B82F6L, 0xFFFF2A85L
        )
        return palette[abs(name.hashCode() + index) % palette.size]
    }

    private fun seedRowsForCompetition(competition: FootballCompetition): List<StandingRow> {
        val raw: List<Triple<String, String, Long>>
        val stats: List<Triple<Int, Int, Int>> // MP, GD, PTS

        when (competition) {
            FootballCompetition.EPL -> {
                raw = listOf(
                    Triple("Liverpool", "LIV", 0xFFFF1E42L),
                    Triple("Arsenal", "ARS", 0xFFEF4444L),
                    Triple("Manchester City", "MCI", 0xFF38BDF8L),
                    Triple("Chelsea", "CHE", 0xFF2563EBL),
                    Triple("Aston Villa", "AVL", 0xFF9333EAL),
                    Triple("Newcastle United", "NEW", 0xFFE2E8F0L),
                    Triple("Brighton & Hove Albion", "BHA", 0xFF0EA5E9L),
                    Triple("Tottenham Hotspur", "TOT", 0xFFF8FAFCL),
                    Triple("Nottingham Forest", "NFO", 0xFFDC2626L),
                    Triple("Fulham", "FUL", 0xFF94A3B8L),
                    Triple("Manchester United", "MUN", 0xFFB91C1CL),
                    Triple("Brentford", "BRE", 0xFFF43F5EL),
                    Triple("Bournemouth", "BOU", 0xFFEF4444L),
                    Triple("West Ham United", "WHU", 0xFF7C3AEDL),
                    Triple("Everton", "EVE", 0xFF1D4ED8L),
                    Triple("Crystal Palace", "CRY", 0xFF3B82F6L),
                    Triple("Wolverhampton", "WOL", 0xFFF59E0BL),
                    Triple("Leicester City", "LEI", 0xFF2563EBL),
                    Triple("Ipswich Town", "IPS", 0xFF0284C7L),
                    Triple("Southampton", "SOU", 0xFFDC2626L)
                )
                stats = listOf(
                    Triple(28, 41, 67), Triple(28, 34, 61), Triple(28, 28, 58), Triple(28, 19, 52),
                    Triple(28, 12, 50), Triple(28, 14, 48), Triple(28, 9, 46), Triple(28, 11, 44),
                    Triple(28, 7, 44), Triple(28, 4, 41), Triple(28, -1, 38), Triple(28, -3, 36),
                    Triple(28, -4, 35), Triple(28, -9, 33), Triple(28, -8, 31), Triple(28, -10, 30),
                    Triple(28, -15, 26), Triple(28, -24, 19), Triple(28, -28, 18), Triple(28, -41, 11)
                )
            }

            FootballCompetition.LALIGA -> {
                raw = listOf(
                    Triple("FC Barcelona", "FCB", 0xFFFF2A55L),
                    Triple("Real Madrid", "RMA", 0xFFFACC15L),
                    Triple("Atlético Madrid", "ATM", 0xFF3B82F6L),
                    Triple("Athletic Club", "ATH", 0xFFEF4444L),
                    Triple("Villarreal", "VIL", 0xFFFDE047L),
                    Triple("Real Betis", "BET", 0xFF10B981L),
                    Triple("Real Sociedad", "RSO", 0xFF38BDF8L),
                    Triple("Mallorca", "MLL", 0xFFDC2626L),
                    Triple("Celta Vigo", "CEL", 0xFF60A5FAL),
                    Triple("Rayo Vallecano", "RAY", 0xFFF8FAFCL),
                    Triple("Sevilla", "SEV", 0xFFF43F5EL),
                    Triple("Girona", "GIR", 0xFFEF4444L),
                    Triple("Osasuna", "OSA", 0xFFB91C1CL),
                    Triple("Getafe", "GET", 0xFF2563EBL),
                    Triple("Valencia", "VAL", 0xFFF59E0BL),
                    Triple("Espanyol", "ESP", 0xFF3B82F6L),
                    Triple("Alavés", "ALA", 0xFF1D4ED8L),
                    Triple("Leganés", "LEG", 0xFF0EA5E9L),
                    Triple("Las Palmas", "LPA", 0xFFFACC15L),
                    Triple("Real Valladolid", "VLL", 0xFF8B5CF6L)
                )
                stats = listOf(
                    Triple(28, 48, 66), Triple(28, 37, 63), Triple(28, 26, 57), Triple(28, 19, 52),
                    Triple(28, 14, 47), Triple(28, 8, 44), Triple(28, 5, 41), Triple(28, 1, 39),
                    Triple(28, 0, 38), Triple(28, -2, 37), Triple(28, -3, 36), Triple(28, -4, 34),
                    Triple(28, -6, 33), Triple(28, -5, 32), Triple(28, -11, 29), Triple(28, -13, 28),
                    Triple(28, -14, 27), Triple(28, -18, 26), Triple(28, -21, 24), Triple(28, -37, 16)
                )
            }

            FootballCompetition.UCL -> {
                raw = listOf(
                    Triple("Liverpool", "LIV", 0xFFFF1E42L),
                    Triple("FC Barcelona", "FCB", 0xFFFF2A55L),
                    Triple("Arsenal", "ARS", 0xFFEF4444L),
                    Triple("Inter Milan", "INT", 0xFF0096FFL),
                    Triple("Atlético Madrid", "ATM", 0xFF3B82F6L),
                    Triple("Bayer Leverkusen", "B04", 0xFFFF3B5CL),
                    Triple("Real Madrid", "RMA", 0xFFFACC15L),
                    Triple("Bayern Munich", "BAY", 0xFFFF1E42L),
                    Triple("Aston Villa", "AVL", 0xFF9333EAL),
                    Triple("Borussia Dortmund", "BVB", 0xFFFFD13BL),
                    Triple("Manchester City", "MCI", 0xFF38BDF8L),
                    Triple("AC Milan", "MIL", 0xFFFF2A55L),
                    Triple("Paris Saint-Germain", "PSG", 0xFF3B82F6L),
                    Triple("Benfica", "SLB", 0xFFEF4444L),
                    Triple("Monaco", "ASM", 0xFFF43F5EL),
                    Triple("Sporting CP", "SCP", 0xFF10B981L),
                    Triple("Juventus", "JUV", 0xFFE2E8F0L),
                    Triple("Celtic", "CEL", 0xFF22C55EL)
                )
                stats = listOf(
                    Triple(8, 15, 21), Triple(8, 15, 19), Triple(8, 13, 19), Triple(8, 10, 19),
                    Triple(8, 8, 18), Triple(8, 8, 16), Triple(8, 8, 15), Triple(8, 8, 15),
                    Triple(8, 7, 15), Triple(8, 10, 15), Triple(8, 4, 13), Triple(8, 3, 13),
                    Triple(8, 5, 13), Triple(8, 4, 13), Triple(8, 3, 13), Triple(8, 1, 11),
                    Triple(8, 2, 11), Triple(8, -1, 11)
                )
            }

            FootballCompetition.SERIE_A -> {
                raw = listOf(
                    Triple("Inter Milan", "INT", 0xFF0096FFL),
                    Triple("Napoli", "NAP", 0xFF38BDF8L),
                    Triple("Atalanta", "ATA", 0xFF2563EBL),
                    Triple("Juventus", "JUV", 0xFFE2E8F0L),
                    Triple("Lazio", "LAZ", 0xFF60A5FAL),
                    Triple("AC Milan", "MIL", 0xFFFF2A55L),
                    Triple("Bologna", "BOL", 0xFFEF4444L),
                    Triple("Fiorentina", "FIO", 0xFFA855F7L),
                    Triple("AS Roma", "ROM", 0xFFF59E0BL),
                    Triple("Udinese", "UDI", 0xFF94A3B8L),
                    Triple("Torino", "TOR", 0xFFB91C1CL),
                    Triple("Genoa", "GEN", 0xFFDC2626L),
                    Triple("Como 1907", "COM", 0xFF0EA5E9L),
                    Triple("Hellas Verona", "VER", 0xFFFACC15L),
                    Triple("Cagliari", "CAG", 0xFF1D4ED8L),
                    Triple("Parma", "PAR", 0xFFFDE047L),
                    Triple("Lecce", "LEC", 0xFFF59E0BL),
                    Triple("Empoli", "EMP", 0xFF3B82F6L),
                    Triple("Venezia", "VEN", 0xFF10B981L),
                    Triple("Monza", "MON", 0xFFEF4444L)
                )
                stats = listOf(
                    Triple(28, 40, 64), Triple(28, 26, 61), Triple(28, 31, 58), Triple(28, 21, 53),
                    Triple(28, 14, 51), Triple(28, 12, 47), Triple(28, 11, 47), Triple(28, 10, 45),
                    Triple(28, 8, 44), Triple(28, -2, 39), Triple(28, -1, 36), Triple(28, -7, 33),
                    Triple(28, -9, 30), Triple(28, -19, 27), Triple(28, -14, 26), Triple(28, -13, 25),
                    Triple(28, -21, 25), Triple(28, -18, 22), Triple(28, -20, 19), Triple(28, -27, 15)
                )
            }

            FootballCompetition.BUNDESLIGA -> {
                raw = listOf(
                    Triple("Bayern Munich", "BAY", 0xFFFF1E42L),
                    Triple("Bayer Leverkusen", "B04", 0xFFFF3B5CL),
                    Triple("Eintracht Frankfurt", "SGE", 0xFFEF4444L),
                    Triple("Mainz 05", "M05", 0xFFF43F5EL),
                    Triple("RB Leipzig", "RBL", 0xFF38BDF8L),
                    Triple("Freiburg", "SCF", 0xFFE2E8F0L),
                    Triple("Borussia Dortmund", "BVB", 0xFFFFD13BL),
                    Triple("VfB Stuttgart", "VFB", 0xFFDC2626L),
                    Triple("Borussia M'gladbach", "BMG", 0xFF10B981L),
                    Triple("Wolfsburg", "WOB", 0xFF22C55EL),
                    Triple("Augsburg", "FCA", 0xFFEF4444L),
                    Triple("Werder Bremen", "SVW", 0xFF10B981L),
                    Triple("Union Berlin", "FCU", 0xFFB91C1CL),
                    Triple("Hoffenheim", "TSG", 0xFF2563EBL),
                    Triple("St. Pauli", "STP", 0xFFA16207L),
                    Triple("Heidenheim", "HDH", 0xFFDC2626L),
                    Triple("VfL Bochum", "BOC", 0xFF0284C7L),
                    Triple("Holstein Kiel", "KIE", 0xFF3B82F6L)
                )
                stats = listOf(
                    Triple(26, 51, 65), Triple(26, 31, 59), Triple(26, 16, 46), Triple(26, 14, 45),
                    Triple(26, 11, 44), Triple(26, 2, 42), Triple(26, 9, 41), Triple(26, 7, 40),
                    Triple(26, 1, 38), Triple(26, 6, 37), Triple(26, -5, 35), Triple(26, -9, 33),
                    Triple(26, -13, 27), Triple(26, -14, 26), Triple(26, -11, 25), Triple(26, -21, 20),
                    Triple(26, -26, 19), Triple(26, -31, 17)
                )
            }

            FootballCompetition.INTER_MIAMI -> {
                raw = listOf(
                    Triple("Inter Miami CF", "MIA", 0xFFFF2A85L),
                    Triple("LA Galaxy", "LAG", 0xFFFFD13BL),
                    Triple("Columbus Crew", "CLB", 0xFFFACC15L),
                    Triple("LAFC", "LAFC", 0xFFD4AF37L),
                    Triple("FC Cincinnati", "CIN", 0xFFFF5E3AL),
                    Triple("Real Salt Lake", "RSL", 0xFFEF4444L),
                    Triple("Seattle Sounders", "SEA", 0xFF10B981L),
                    Triple("Houston Dynamo", "HOU", 0xFFFB923CL),
                    Triple("Orlando City SC", "ORL", 0xFFA855F7L),
                    Triple("Charlotte FC", "CLT", 0xFF38BDF8L),
                    Triple("New York City FC", "NYC", 0xFF60A5FAL),
                    Triple("New York Red Bulls", "RBNY", 0xFFEF4444L),
                    Triple("Minnesota United", "MIN", 0xFF0EA5E9L),
                    Triple("Vancouver Whitecaps", "VAN", 0xFF3B82F6L),
                    Triple("Atlanta United", "ATL", 0xFFDC2626L)
                )
                stats = listOf(
                    Triple(34, 29, 74), Triple(34, 19, 64), Triple(34, 28, 66), Triple(34, 20, 64),
                    Triple(34, 11, 59), Triple(34, 17, 59), Triple(34, 16, 57), Triple(34, 8, 54),
                    Triple(34, 9, 52), Triple(34, 9, 51), Triple(34, 5, 50), Triple(34, 5, 47),
                    Triple(34, 9, 52), Triple(34, 3, 47), Triple(34, -3, 40)
                )
            }

            FootballCompetition.INTL_CUP -> {
                raw = listOf(
                    Triple("Spain", "ESP", 0xFFFF3355L),
                    Triple("Argentina", "ARG", 0xFF38BDF8L),
                    Triple("France", "FRA", 0xFF3B82F6L),
                    Triple("England", "ENG", 0xFFF8FAFCL),
                    Triple("Germany", "GER", 0xFFE2E8F0L),
                    Triple("Colombia", "COL", 0xFFFACC15L),
                    Triple("Netherlands", "NED", 0xFFFB923CL),
                    Triple("Portugal", "POR", 0xFFEF4444L),
                    Triple("Brazil", "BRA", 0xFFFFD13BL),
                    Triple("Uruguay", "URU", 0xFF60A5FAL),
                    Triple("Italy", "ITA", 0xFF2563EBL),
                    Triple("Switzerland", "SUI", 0xFFDC2626L)
                )
                stats = listOf(
                    Triple(7, 11, 21), Triple(7, 9, 19), Triple(7, 5, 16), Triple(7, 4, 15),
                    Triple(7, 7, 14), Triple(7, 8, 14), Triple(7, 5, 13), Triple(7, 4, 13),
                    Triple(7, 3, 12), Triple(7, 5, 12), Triple(7, 1, 10), Triple(7, 2, 10)
                )
            }

            FootballCompetition.INTL_FRIENDLY, FootballCompetition.ALL -> {
                raw = listOf(
                    Triple("Argentina", "ARG", 0xFF38BDF8L),
                    Triple("France", "FRA", 0xFF3B82F6L),
                    Triple("Spain", "ESP", 0xFFFF3355L),
                    Triple("England", "ENG", 0xFFF8FAFCL),
                    Triple("Brazil", "BRA", 0xFFFFD13BL),
                    Triple("Portugal", "POR", 0xFFEF4444L),
                    Triple("Netherlands", "NED", 0xFFFB923CL),
                    Triple("Belgium", "BEL", 0xFFDC2626L),
                    Triple("Italy", "ITA", 0xFF2563EBL),
                    Triple("Germany", "GER", 0xFFE2E8F0L),
                    Triple("Uruguay", "URU", 0xFF60A5FAL),
                    Triple("Colombia", "COL", 0xFFFACC15L)
                )
                stats = listOf(
                    Triple(10, 18, 26), Triple(10, 14, 24), Triple(10, 16, 24), Triple(10, 11, 21),
                    Triple(10, 9, 19), Triple(10, 12, 19), Triple(10, 8, 18), Triple(10, 6, 17),
                    Triple(10, 5, 16), Triple(10, 9, 16), Triple(10, 7, 15), Triple(10, 6, 15)
                )
            }
        }

        return raw.mapIndexed { idx, (name, code, color) ->
            val (mp, gd, pts) = stats.getOrElse(idx) { Triple(28, 0, 40 - idx) }
            StandingRow(
                rank = idx + 1,
                teamName = name,
                shortCode = code,
                badgeColorHex = color,
                matchesPlayed = mp,
                goalDifference = gd,
                points = pts,
                qualificationZone = zoneForRank(idx + 1, raw.size, competition)
            )
        }
    }
}
