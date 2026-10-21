# Touchthegrass.com League Standings & Web Launchability Plan

This plan introduces a glass-transparent **Standings** screen covering all major football competitions (EPL, LaLiga, UCL, Serie A, Bundesliga, MLS Inter Miami, and International Cups) with a compact table layout, while resolving the `FLAG_SECURE` display block so the application and web portal open cleanly in the browser preview.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product and UX choices were confirmed during Phase 1 and govern this implementation:

- **Confirmed Decision 1 — League Coverage**: Include **all major leagues including MLS (Inter Miami)** alongside Premier League (EPL), LaLiga, UEFA Champions League (UCL), Serie A, Bundesliga, and International Cup competitions.
- **Confirmed Decision 2 — Table Density**: Use a **compact table layout** displaying Rank (`#`), Club, Matches Played (`MP`), Goal Difference (`GD`), and Points (`PTS`) with tabular monospace alignment and subtle qualification zone indicators.
- **Confirmed Decision 3 — Navigation Access**: Provide a **dedicated Standings tab** in the bottom navigation bar **plus a quick link** directly inside the Live Football home screen header.
- **Confirmed Decision 4 — Root-Cause Fix for Preview & Website Opening**: Disable default startup `WindowManager.LayoutParams.FLAG_SECURE` enforcement (which blocks the browser streaming emulator video capture from displaying the app) and add a resilient **Open Touchthegrass.com Web Portal** launcher with an in-app interactive web sheet and safe external URI fallback.

---

## 1. Overview & Core Concept

- **What It Does**: Gives football fans instant access to live-updated and remotely fetched league tables across Europe and MLS, synchronized with the active live match scores on Touchthegrass.com, and allows opening the Touchthegrass.com web portal and official league pages directly.
- **Target Audience / Persona**: Football supporters tracking title races, UEFA Champions League qualification spots, and Inter Miami's MLS Supporters' Shield standings alongside live match broadcasts.
- **Key Value**: Combines clean OLED glass-transparent table readability (`MP`, `GD`, `PTS`) with real-time network fetching, live-score point adjustments, and an always-openable preview and web experience.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Bottom Bar or Quick Link Entry**: Users tap the **Standings** icon in the sleek OLED bottom navigation bar or tap the **Standings Table** pill button on the Live Broadcasts home header.
  2. **League Switching & Search**: Users switch effortlessly between **EPL**, **LaLiga**, **UCL**, **Serie A**, **Bundesliga**, **Inter Miami (MLS)**, and **Intl Cup** using glass-morphic filter pills, or filter teams via a compact search bar.
  3. **Live Refresh & Live-Match Point Delta**: Pulling or tapping **Refresh Live Table** fetches current standings over HTTPS with automatic fallback and highlights clubs currently playing live matches (including the user's favorite club from their profile).
  4. **Opening Touchthegrass.com & Official League Portals**: Tapping the `Touchthegrass.com` header pill or the **"Launch Web Portal"** button opens an interactive in-app Web Portal preview or launches the external browser safely with `try/catch` URI protection.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Ultra-clean OLED dark glassmorphism with editorial serif titles (`FontFamily.Serif`) and crisp tabular monospace numerals (`JetBrainsMonoFontFamily`), matching the existing `My Profile` and `Settings` visual language.
  - *Color Palette & Mood*:
    - `--background`: Deep OLED Black (`#060709`) layered over the user's selected abstract football theme canvas (defaulting to **Messi #10 Black & White GOAT**).
    - `--surface-glass`: Translucent dark glass (`#17181C` at 78%–88% alpha) with subtle `1.dp` border strokes (`#26282E`).
    - `--accent-primary`: Verified Emerald (`#10B981`) for top qualification spots, live indicators, and active states.
    - `--accent-secondary`: Stream Cyan (`#38BDF8`) for active navigation states and UEFA/playoff zone markers.
  - *Typography & Hierarchy*:
    - Screen & Section Headers: Editorial Serif (`22.sp–26.sp`) with muted slate subtitles (`#8A8F99`).
    - Standings Numerals (`MP`, `GD`, `PTS`): Right-aligned monospace numerals (`13.sp–14.sp`) so columns align with mathematical precision across all 20 rows.
  - *Component Styling & Layout*:
    - Translucent sticky table header row (`#`, `CLUB`, `MP`, `GD`, `PTS`).
    - Color-coded left accent bar (`3.dp` width) indicating Champions League / Title Playoff qualification (`#10B981`), continental qualification (`#38BDF8`), or relegation zone (`#EF4444`).
    - Highlighted row border when a club matches the signed-in user's **Favorite Club** (e.g., Inter Miami CF, FC Barcelona, Real Madrid).

- **Interactive Feedback & Motion**:
  - Smooth animated transitions between league tables, a live sync status badge (`LIVE API` / `SYNCED`), and instant search filtering with an informative empty state if no club matches the filter query.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Removing Default Startup `FLAG_SECURE` to Fix Browser Preview Rendering**
  - *Chosen Approach*: Initialize `_flagSecureEnabled` to `false` and remove the unconditional `window.setFlags(FLAG_SECURE, FLAG_SECURE)` call from `MainActivity.onCreate()`.
  - *Why*: Android's `FLAG_SECURE` instructs the OS surface compositor to block screen recording and virtual display frame capture. In a cloud streaming emulator, this causes the browser preview to render a blank/black screen. Keeping it off by default makes the app immediately visible and openable while preserving the optional toggle inside Settings for testing.
  - *Alternatives Considered*: Keeping `FLAG_SECURE` always on was rejected because it prevents the streaming preview from displaying the UI.

- **Decision 2: Hybrid Live Network Fetch + Live Match Score Integration for Standings**
  - *Chosen Approach*: Fetch real standings asynchronously from public HTTPS sports endpoints (`site.api.espn.com` soccer standings JSON) using OkHttp on `Dispatchers.IO`, paired with complete, up-to-date multi-league tables that dynamically reflect live match scores from the app's match ticker.
  - *Why*: Guarantees real network telemetry when online while ensuring instant zero-latency rendering and resilience if an endpoint rate-limits or times out.
  - *Alternatives Considered*: Relying solely on an uncached remote request without fallback was rejected to avoid blank screens on restricted networks.

- **Decision 3: Safe Web Portal Launcher (In-App WebView + External Browser Intent)**
  - *Chosen Approach*: Provide both an in-app interactive **Touchthegrass.com Web Portal** modal (using an embedded Android `WebView` / portal view with live stream & league links) and a safe `Intent.ACTION_VIEW` external browser launcher wrapped in `runCatching`.
  - *Why*: Ensures "Make the website openable" works reliably inside the Android emulator regardless of whether a third-party browser package is pre-installed.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         TouchTheGrass MainActivity                        │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ Top Bar: Touchthegrass.com Editorial Header + "Open Website" Action │  │
│  └──────────────┬───────────────────────────────────────┬──────────────┘  │
│                 │                                       │                 │
│                 ▼                                       ▼                 │
│  ┌─────────────────────────────┐         ┌─────────────────────────────┐  │
│  │     LiveFootballScreen      │         │       StandingsScreen       │  │
│  │  • League Filter Bar        │────────▶│  • League Selector Pills    │  │
│  │  • Quick "Standings" Button │  Quick  │  • Club Search & Live Sync  │  │
│  │  • 4K Stream Video Player   │  Link   │  • Compact Glass Table:     │  │
│  │  • Live Match Cards         │         │    [#] [Club] [MP][GD][PTS] │  │
│  └──────────────┬──────────────┘         └──────────────┬──────────────┘  │
│                 │                                       │                 │
│  ┌──────────────┴───────────────────────────────────────┴──────────────┐  │
│  │     Bottom Navigation Bar: Live | Standings | Cinema | AI | Profile │  │
│  └──────────────────────────────┬──────────────────────────────────────┘  │
└─────────────────────────────────┼─────────────────────────────────────────┘
                                  │ StateFlow (collectAsStateWithLifecycle)
                                  ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                              FrameXViewModel                              │
│  • selectedStandingsLeague: StateFlow<FootballCompetition>                │
│  • standingsSearchQuery: StateFlow<String>                                │
│  • standingsUiState: StateFlow<StandingsUiState> (Loading/Success/Error)  │
│  • isWebPortalOpen: StateFlow<Boolean> + activeWebPortalUrl               │
└─────────────────┬───────────────────────────────────────┬─────────────────┘
                  │                                       │
                  ▼                                       ▼
┌───────────────────────────────────┐   ┌───────────────────────────────────┐
│        StandingsRepository        │   │       UserProfileRepository       │
│  • HTTPS OkHttp Live Standings    │   │  • Firestore users/{userId}       │
│  • Live Match Score Point Delta   │   │  • Favorite Club Highlight Sync   │
└───────────────────────────────────┘   └───────────────────────────────────┘
```

- **Data Model & State**:
  - `StandingRow`: Represents a single club row containing `rank`, `teamName`, `shortCode`, `badgeColorHex`, `matchesPlayed` (`MP`), `goalDifference` (`GD`), `points` (`PTS`), `qualificationZone` (`CHAMPIONS_LEAGUE`, `EUROPA_PLAYOFF`, `RELEGATION`, `NONE`), and `isLivePlaying`.
  - `LeagueStandingsTable`: Groups `competition: FootballCompetition`, `seasonLabel: String`, `lastUpdatedLabel: String`, `sourceLabel: String`, and `rows: List<StandingRow>`.
  - `StandingsUiState`: Sealed state modeling `Loading`, `Success(table, isLiveNetworkSynced)`, and `Error(message, fallbackTable)`.

- **Interactive Component & State Mapping**:
  - **League Pill Selector (`StandingsScreen`)**: Tapping any competition pill (`EPL`, `LaLiga`, `UCL`, `Serie A`, `Bundesliga`, `Inter Miami`, `Intl Cup`, `Intl Friendly`) invokes `viewModel.selectStandingsLeague(competition)`, triggering an asynchronous fetch and immediate table update.
  - **Club Filter Bar**: Typing in the glass search field updates `standingsSearchQuery` and filters rows by club name or short code in real time.
  - **Refresh Standings Button**: Invokes `viewModel.refreshStandings()` to re-fetch live standings over HTTPS and recompute live-match point deltas.
  - **Quick Link & Website Launcher**: Tapping the **Standings** quick pill in `LiveFootballScreen` switches directly to the `STANDINGS` tab for the currently selected league; tapping **Open Touchthegrass.com** opens the interactive Web Portal modal with one-tap external browser support.
