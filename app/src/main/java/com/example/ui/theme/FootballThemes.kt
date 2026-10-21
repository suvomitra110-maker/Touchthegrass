package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class AbstractPatternStyle {
    MESSI_MONOCHROME,
    STARBALL_RINGS,
    DIAGONAL_PRISM,
    HEXAGON_GRID,
    STADIUM_WAVES,
    TACTICAL_RADAR
}

data class FootballAppThemeOption(
    val id: String,
    val rank: Int,
    val name: String,
    val subtitle: String,
    val bgPrimaryHex: Long,
    val bgSecondaryHex: Long,
    val accentPrimaryHex: Long,
    val accentSecondaryHex: Long,
    val glassSurfaceAlpha: Float,
    val glassBorderAlpha: Float,
    val patternStyle: AbstractPatternStyle
)

object FootballThemesCatalog {

    val top20Themes: List<FootballAppThemeOption> = listOf(
        FootballAppThemeOption(
            id = "messi_monochrome_goat",
            rank = 1,
            name = "1. Messi #10 Black & White",
            subtitle = "Pure monochrome black & white GOAT silhouette with #10 geometry",
            bgPrimaryHex = 0xFF050505,
            bgSecondaryHex = 0xFF1A1A1A,
            accentPrimaryHex = 0xFFFFFFFF,
            accentSecondaryHex = 0xFFD4D4D8,
            glassSurfaceAlpha = 0.12f,
            glassBorderAlpha = 0.34f,
            patternStyle = AbstractPatternStyle.MESSI_MONOCHROME
        ),
        FootballAppThemeOption(
            id = "ucl_starball_midnight",
            rank = 2,
            name = "2. UCL Starball Midnight",
            subtitle = "Champions League sapphire & silver starball rings",
            bgPrimaryHex = 0xFF04091A,
            bgSecondaryHex = 0xFF0D1F4D,
            accentPrimaryHex = 0xFF00E5FF,
            accentSecondaryHex = 0xFFE2E8F0,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.STARBALL_RINGS
        ),
        FootballAppThemeOption(
            id = "camp_nou_blaugrana",
            rank = 3,
            name = "3. Blaugrana Prism",
            subtitle = "Abstract crimson & royal blue glass ribbons",
            bgPrimaryHex = 0xFF0B0719,
            bgSecondaryHex = 0xFF2A081C,
            accentPrimaryHex = 0xFFFF2A55,
            accentSecondaryHex = 0xFF3B82F6,
            glassSurfaceAlpha = 0.14f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.DIAGONAL_PRISM
        ),
        FootballAppThemeOption(
            id = "miami_vice_herons",
            rank = 4,
            name = "4. Inter Miami Neon Vice",
            subtitle = "Obsidian black & flamingo pink Messi #10 Miami waves",
            bgPrimaryHex = 0xFF08060C,
            bgSecondaryHex = 0xFF240A1C,
            accentPrimaryHex = 0xFFFF2A85,
            accentSecondaryHex = 0xFFFFFFFF,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.30f,
            patternStyle = AbstractPatternStyle.STADIUM_WAVES
        ),
        FootballAppThemeOption(
            id = "anfield_kop_vortex",
            rank = 5,
            name = "5. Anfield Kop Red Vortex",
            subtitle = "Deep liverbird crimson & golden stadium acoustics",
            bgPrimaryHex = 0xFF120407,
            bgSecondaryHex = 0xFF2E0810,
            accentPrimaryHex = 0xFFFF3355,
            accentSecondaryHex = 0xFFFFD13B,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.26f,
            patternStyle = AbstractPatternStyle.STADIUM_WAVES
        ),
        FootballAppThemeOption(
            id = "bernabeu_white_gold",
            rank = 6,
            name = "6. Bernabéu White & Gold",
            subtitle = "Monochrome obsidian, ivory white & royal gold crown lines",
            bgPrimaryHex = 0xFF0A0A0E,
            bgSecondaryHex = 0xFF1F1D18,
            accentPrimaryHex = 0xFFFFFFFF,
            accentSecondaryHex = 0xFFFFD13B,
            glassSurfaceAlpha = 0.14f,
            glassBorderAlpha = 0.32f,
            patternStyle = AbstractPatternStyle.DIAGONAL_PRISM
        ),
        FootballAppThemeOption(
            id = "epl_ultraviolet_lion",
            rank = 7,
            name = "7. EPL Ultraviolet Lion",
            subtitle = "Premier League royal purple & electric cyan cyber-mesh",
            bgPrimaryHex = 0xFF0D061A,
            bgSecondaryHex = 0xFF220E3B,
            accentPrimaryHex = 0xFF00FF87,
            accentSecondaryHex = 0xFFC084FC,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.HEXAGON_GRID
        ),
        FootballAppThemeOption(
            id = "san_siro_nerazzurri",
            rank = 8,
            name = "8. San Siro Nerazzurri",
            subtitle = "Electric Milanese azure & pitch black diagonal shards",
            bgPrimaryHex = 0xFF050A14,
            bgSecondaryHex = 0xFF0B1E40,
            accentPrimaryHex = 0xFF38BDF8,
            accentSecondaryHex = 0xFFFFFFFF,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.DIAGONAL_PRISM
        ),
        FootballAppThemeOption(
            id = "maracana_golden_samba",
            rank = 9,
            name = "9. Maracanã Golden Samba",
            subtitle = "Canary yellow, pitch emerald & Rio midnight curves",
            bgPrimaryHex = 0xFF04140E,
            bgSecondaryHex = 0xFF0A2E1F,
            accentPrimaryHex = 0xFFFFD13B,
            accentSecondaryHex = 0xFF00FF87,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.STADIUM_WAVES
        ),
        FootballAppThemeOption(
            id = "signal_iduna_honeycomb",
            rank = 10,
            name = "10. Yellow Wall Honeycomb",
            subtitle = "Dortmund cyber yellow & carbon black hexagonal net",
            bgPrimaryHex = 0xFF0A0A06,
            bgSecondaryHex = 0xFF1F1D08,
            accentPrimaryHex = 0xFFFFEA00,
            accentSecondaryHex = 0xFFFFFFFF,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.30f,
            patternStyle = AbstractPatternStyle.HEXAGON_GRID
        ),
        FootballAppThemeOption(
            id = "allianz_crimson_diamond",
            rank = 11,
            name = "11. Allianz Crimson Diamond",
            subtitle = "Bavarian glowing red diamond lattice on dark slate",
            bgPrimaryHex = 0xFF120508,
            bgSecondaryHex = 0xFF2B0A12,
            accentPrimaryHex = 0xFFFF2A55,
            accentSecondaryHex = 0xFFF8FAFC,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.HEXAGON_GRID
        ),
        FootballAppThemeOption(
            id = "albiceleste_world_cup",
            rank = 12,
            name = "12. Albiceleste Three Stars",
            subtitle = "Argentina sky blue, pure white & golden sun rays",
            bgPrimaryHex = 0xFF06101E,
            bgSecondaryHex = 0xFF0F2942,
            accentPrimaryHex = 0xFF7DD3FC,
            accentSecondaryHex = 0xFFFFD13B,
            glassSurfaceAlpha = 0.14f,
            glassBorderAlpha = 0.32f,
            patternStyle = AbstractPatternStyle.MESSI_MONOCHROME
        ),
        FootballAppThemeOption(
            id = "laliga_coral_kinetic",
            rank = 13,
            name = "13. LaLiga Coral Kinetic",
            subtitle = "Spanish football coral pulse & dark obsidian glass",
            bgPrimaryHex = 0xFF120808,
            bgSecondaryHex = 0xFF2B1111,
            accentPrimaryHex = 0xFFFF5E3A,
            accentSecondaryHex = 0xFFFFFFFF,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.DIAGONAL_PRISM
        ),
        FootballAppThemeOption(
            id = "serie_a_calcio_aurora",
            rank = 14,
            name = "14. Serie A Calcio Aurora",
            subtitle = "Italian emerald, ocean blue & ruby abstract light",
            bgPrimaryHex = 0xFF051214,
            bgSecondaryHex = 0xFF0A282E,
            accentPrimaryHex = 0xFF00E5FF,
            accentSecondaryHex = 0xFF00FF87,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.STARBALL_RINGS
        ),
        FootballAppThemeOption(
            id = "cyber_pitch_matrix",
            rank = 15,
            name = "15. Cyber Pitch Tactical Matrix",
            subtitle = "Neon emerald telemetry grid & isometric pitch radar",
            bgPrimaryHex = 0xFF040F0A,
            bgSecondaryHex = 0xFF072619,
            accentPrimaryHex = 0xFF00FF87,
            accentSecondaryHex = 0xFF00E5FF,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.30f,
            patternStyle = AbstractPatternStyle.TACTICAL_RADAR
        ),
        FootballAppThemeOption(
            id = "ballon_dor_liquid_gold",
            rank = 16,
            name = "16. Ballon d'Or Liquid Gold",
            subtitle = "8x Golden Ball trophy shimmer on monochrome charcoal",
            bgPrimaryHex = 0xFF0A0906,
            bgSecondaryHex = 0xFF211C10,
            accentPrimaryHex = 0xFFFFD13B,
            accentSecondaryHex = 0xFFFFFFFF,
            glassSurfaceAlpha = 0.14f,
            glassBorderAlpha = 0.32f,
            patternStyle = AbstractPatternStyle.STARBALL_RINGS
        ),
        FootballAppThemeOption(
            id = "arctic_frost_glass",
            rank = 17,
            name = "17. Arctic Monochrome Frost",
            subtitle = "High-clarity silver-white frosted crystal over deep slate",
            bgPrimaryHex = 0xFF0B0F17,
            bgSecondaryHex = 0xFF1E293B,
            accentPrimaryHex = 0xFFFFFFFF,
            accentSecondaryHex = 0xFF38BDF8,
            glassSurfaceAlpha = 0.16f,
            glassBorderAlpha = 0.38f,
            patternStyle = AbstractPatternStyle.MESSI_MONOCHROME
        ),
        FootballAppThemeOption(
            id = "tokyo_blue_lock_striker",
            rank = 18,
            name = "18. Neo-Tokyo Anime Striker",
            subtitle = "Electric indigo & cyber cyan egoist speed-lines",
            bgPrimaryHex = 0xFF05071C,
            bgSecondaryHex = 0xFF101942,
            accentPrimaryHex = 0xFF00E5FF,
            accentSecondaryHex = 0xFFFF2A85,
            glassSurfaceAlpha = 0.13f,
            glassBorderAlpha = 0.28f,
            patternStyle = AbstractPatternStyle.HEXAGON_GRID
        ),
        FootballAppThemeOption(
            id = "wembley_arch_twilight",
            rank = 19,
            name = "19. Wembley Arch Twilight",
            subtitle = "Iconic illuminated silver stadium arch over midnight sky",
            bgPrimaryHex = 0xFF080B14,
            bgSecondaryHex = 0xFF161F33,
            accentPrimaryHex = 0xFFE2E8F0,
            accentSecondaryHex = 0xFF00FF87,
            glassSurfaceAlpha = 0.14f,
            glassBorderAlpha = 0.30f,
            patternStyle = AbstractPatternStyle.STADIUM_WAVES
        ),
        FootballAppThemeOption(
            id = "obsidian_carbon_stealth",
            rank = 20,
            name = "20. Obsidian Carbon Stealth",
            subtitle = "Pure OLED black & stark white zero-distraction broadcast",
            bgPrimaryHex = 0xFF000000,
            bgSecondaryHex = 0xFF121212,
            accentPrimaryHex = 0xFFFFFFFF,
            accentSecondaryHex = 0xFF94A3B8,
            glassSurfaceAlpha = 0.11f,
            glassBorderAlpha = 0.30f,
            patternStyle = AbstractPatternStyle.TACTICAL_RADAR
        )
    )

    fun findById(id: String): FootballAppThemeOption {
        return top20Themes.find { it.id == id } ?: top20Themes.first()
    }
}

/**
 * Renders the full-screen Abstract Football Background with custom Messi #10 Black & White
 * monochrome silhouette geometry and stadium light vectors behind the transparent glass UI.
 */
@Composable
fun AbstractFootballBackgroundCanvas(
    theme: FootballAppThemeOption,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bgPrimary = Color(theme.bgPrimaryHex)
    val bgSecondary = Color(theme.bgSecondaryHex)
    val accentPrimary = Color(theme.accentPrimaryHex)
    val accentSecondary = Color(theme.accentSecondaryHex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgPrimary, bgSecondary, bgPrimary)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle ambient glow orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentPrimary.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(w * 0.82f, h * 0.18f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.82f, h * 0.18f),
                radius = w * 0.65f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentSecondary.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(w * 0.18f, h * 0.78f),
                    radius = w * 0.60f
                ),
                center = Offset(w * 0.18f, h * 0.78f),
                radius = w * 0.60f
            )

            when (theme.patternStyle) {
                AbstractPatternStyle.MESSI_MONOCHROME -> {
                    // Stark Black & White Messi #10 Geometric Pitch & Iconic Celebration Silhouette Lines
                    val strokeThin = Stroke(width = 1.5.dp.toPx())
                    val strokeBold = Stroke(width = 3.dp.toPx())
                    val whiteLine = accentPrimary.copy(alpha = 0.13f)

                    // Vertical monochrome stripes (Argentina / Classic Monochrome jersey motif)
                    val stripeCount = 7
                    val stripeW = w / stripeCount
                    for (i in 0 until stripeCount step 2) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.035f),
                            topLeft = Offset(i * stripeW, 0f),
                            size = Size(stripeW, h)
                        )
                    }

                    // Center pitch circle & halfway line
                    drawCircle(
                        color = whiteLine,
                        radius = w * 0.28f,
                        center = Offset(w * 0.5f, h * 0.42f),
                        style = strokeThin
                    )
                    drawLine(
                        color = whiteLine,
                        start = Offset(0f, h * 0.42f),
                        end = Offset(w, h * 0.42f),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Stylized Geometric "#10" Emblem in the background
                    val numColor = accentPrimary.copy(alpha = 0.11f)
                    // Digit "1"
                    drawLine(
                        color = numColor,
                        start = Offset(w * 0.36f, h * 0.28f),
                        end = Offset(w * 0.36f, h * 0.55f),
                        strokeWidth = 16.dp.toPx()
                    )
                    // Digit "0"
                    drawRect(
                        color = numColor,
                        topLeft = Offset(w * 0.46f, h * 0.28f),
                        size = Size(w * 0.20f, h * 0.27f),
                        style = Stroke(width = 16.dp.toPx())
                    )

                    // Iconic sky-pointing celebration silhouette rays
                    val crestPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.20f)
                        lineTo(w * 0.32f, h * 0.05f)
                        moveTo(w * 0.5f, h * 0.20f)
                        lineTo(w * 0.68f, h * 0.05f)
                    }
                    drawPath(crestPath, color = accentPrimary.copy(alpha = 0.12f), style = strokeBold)
                }

                AbstractPatternStyle.STARBALL_RINGS -> {
                    for (i in 1..4) {
                        drawCircle(
                            color = accentPrimary.copy(alpha = 0.08f),
                            radius = (w * 0.14f) * i,
                            center = Offset(w * 0.75f, h * 0.28f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }

                AbstractPatternStyle.DIAGONAL_PRISM -> {
                    for (i in 0..6) {
                        val yOff = h * 0.12f * i
                        drawLine(
                            color = if (i % 2 == 0) accentPrimary.copy(alpha = 0.09f) else accentSecondary.copy(alpha = 0.08f),
                            start = Offset(0f, yOff),
                            end = Offset(w, yOff - h * 0.22f),
                            strokeWidth = 24.dp.toPx()
                        )
                    }
                }

                AbstractPatternStyle.HEXAGON_GRID -> {
                    val step = 64.dp.toPx()
                    var y = 0f
                    while (y < h) {
                        drawLine(
                            color = accentPrimary.copy(alpha = 0.06f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += step
                    }
                }

                AbstractPatternStyle.STADIUM_WAVES -> {
                    for (i in 1..5) {
                        drawCircle(
                            color = accentPrimary.copy(alpha = 0.07f),
                            radius = (w * 0.18f) * i,
                            center = Offset(w * 0.5f, h * 0.15f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                AbstractPatternStyle.TACTICAL_RADAR -> {
                    val pad = 24.dp.toPx()
                    drawRect(
                        color = accentPrimary.copy(alpha = 0.08f),
                        topLeft = Offset(pad, pad),
                        size = Size(w - pad * 2, h - pad * 2),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = accentPrimary.copy(alpha = 0.09f),
                        radius = w * 0.25f,
                        center = Offset(w * 0.5f, h * 0.5f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        content()
    }
}
