package com.example.model

object SeedCatalog {

    val defaultServers = listOf(
        StreamServer("srv_hydra", "Hydra 4K Ultra (Decrypted)", "4K HDR10+", "HEVC 60fps", 12, true),
        StreamServer("srv_vidcloud", "VidCloud Pro Fast", "1080p FHD", "AV1 60fps", 19, true),
        StreamServer("srv_nova", "NovaStream Atmos", "4K Dolby Vision", "HEVC Atmos", 24, true),
        StreamServer("srv_mirror", "Direct Edge Mirror", "1080p 60fps", "H.264 Low-Latency", 15, true)
    )

    val footballBroadcastServers = listOf(
        StreamServer(
            id = "srv_epicsports_in_1",
            name = "Server 1 • EpicSports Live (live.epicsports.in)",
            resolution = "1080p 60fps HD",
            codec = "EpicSports Direct",
            pingMs = 9,
            isAdFree = true,
            providerTag = "live.epicsports.in/?m=1",
            streamUrl = "https://live.epicsports.in/?m=1",
            videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        ),
        StreamServer(
            id = "srv_epicsports_in_2",
            name = "Server 2 • EpicSports Mirror #2",
            resolution = "1080p 60fps",
            codec = "HLS Low-Latency",
            pingMs = 11,
            isAdFree = true,
            providerTag = "live.epicsports.in • S2",
            streamUrl = "https://live.epicsports.in/?m=1",
            videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
        ),
        StreamServer(
            id = "srv_frametv_4k",
            name = "Server 3 • FrameTV 4K Ultra",
            resolution = "4K 60fps HDR",
            codec = "HEVC Low-Latency",
            pingMs = 12,
            isAdFree = true,
            providerTag = "FrameTV Primary 4K",
            streamUrl = "https://framextv.cc/",
            videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        ),
        StreamServer(
            id = "srv_epicsports_global",
            name = "Server 4 • EpicSports Global Estadio",
            resolution = "4K HDR",
            codec = "Dolby Stadium Audio",
            pingMs = 15,
            isAdFree = true,
            providerTag = "EpicSports Global CDN",
            streamUrl = "https://live.epicsports.in/?m=1",
            videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        ),
        StreamServer(
            id = "srv_frametv_tactical",
            name = "Server 5 • Tactical SpiderCam 4K",
            resolution = "4K All-22",
            codec = "AV1 Multi-View",
            pingMs = 16,
            isAdFree = true,
            providerTag = "FrameTV Tactical",
            streamUrl = "https://live.epicsports.in/?m=1",
            videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4"
        )
    )

    val cinemaCatalog = listOf(
        MediaTitle(
            id = "mov_chrono_nexus",
            title = "Chrono Nexus: Eclipse Protocol",
            year = 2026,
            rating = "9.1",
            qualityBadge = "4K IMAX HDR",
            durationOrSeasons = "2h 28m",
            category = MediaCategory.MOVIES_4K,
            genres = listOf("Sci-Fi", "Action", "Cyber-Thriller"),
            synopsis = "When a quantum orbital relay above Neo-Zurich fractures time across three continents, a rogue telemetry architect must infiltrate the orbital vault before history is overwritten.",
            director = "Denis Vance",
            cast = listOf("Kaelen Voss", "Elena Rostova", "Marcus Sterling", "Sora Takahashi"),
            audioTracks = listOf("English Dolby Atmos 7.1", "Spanish 5.1", "French DTS-HD", "Japanese 5.1"),
            servers = defaultServers,
            primaryColorHex = 0xFF00E5FF,
            secondaryColorHex = 0xFF0B192C,
            isTrending = true
        ),
        MediaTitle(
            id = "mov_velvet_heist",
            title = "The Monte Carlo Gambit",
            year = 2026,
            rating = "8.7",
            qualityBadge = "4K UHD",
            durationOrSeasons = "2h 09m",
            category = MediaCategory.MOVIES_4K,
            genres = listOf("Crime", "Thriller", "Mystery"),
            synopsis = "During the Monaco Grand Prix weekend, an elite syndicate stages a silent zero-footprint vault extraction beneath the harbor casino while Interpol closes every exit.",
            director = "Sofia Moretti",
            cast = listOf("Julian Thorne", "Camille Laurent", "Enzo Bellini"),
            audioTracks = listOf("English Atmos", "Italian 5.1", "French 5.1"),
            servers = defaultServers,
            primaryColorHex = 0xFFFFD13B,
            secondaryColorHex = 0xFF2B1908,
            isTrending = true
        ),
        MediaTitle(
            id = "ser_syndicate_zero",
            title = "Blackout Grid: Season 3",
            year = 2026,
            rating = "9.4",
            qualityBadge = "4K 60FPS",
            durationOrSeasons = "3 Seasons • 24 Eps",
            category = MediaCategory.TV_SERIES,
            genres = listOf("Drama", "Espionage", "Tech"),
            synopsis = "MI6 cyber-analysts and Berlin underground whistleblowers race to expose an autonomous financial cartel manipulating European energy grids in real time.",
            director = "Lukas Weber",
            cast = listOf("Hannah Lindqvist", "Devon Brooks", "Tariq Al-Mansoor"),
            audioTracks = listOf("English 5.1", "German 5.1", "Spanish Latino"),
            servers = defaultServers,
            primaryColorHex = 0xFF00FF87,
            secondaryColorHex = 0xFF072B1F,
            isTrending = true
        ),
        MediaTitle(
            id = "ani_astral_blade",
            title = "Aether Blade: Shinjuku Requiem",
            year = 2026,
            rating = "9.3",
            qualityBadge = "4K 120Hz Anime",
            durationOrSeasons = "2 Seasons • 36 Eps",
            category = MediaCategory.ANIME,
            genres = listOf("Anime", "Dark Fantasy", "Action"),
            synopsis = "In a neon-drenched Tokyo where dimensional rifts manifest atop skyscrapers, a disgraced spirit-smith forges resonance blades capable of severing astral titans.",
            director = "Kenji Takahashi",
            cast = listOf("Renji Sato", "Akari Hoshino", "Daiki Kuroda"),
            audioTracks = listOf("Japanese Original (FLAC)", "English Dub 5.1"),
            servers = defaultServers,
            primaryColorHex = 0xFFFF2A85,
            secondaryColorHex = 0xFF2D0A22,
            isTrending = true
        ),
        MediaTitle(
            id = "ser_pitch_kings",
            title = "Champions Under Floodlights",
            year = 2026,
            rating = "8.9",
            qualityBadge = "4K Docu-Series",
            durationOrSeasons = "1 Season • 8 Eps",
            category = MediaCategory.TV_SERIES,
            genres = listOf("Sports Documentary", "Football", "Behind the Scenes"),
            synopsis = "Unprecedented locker-room and tactical camera access following Europe's elite football managers and South American icons across knockout nights.",
            director = "Mateo Silva",
            cast = listOf("Carlo Ancelotti", "Pep Guardiola", "Lionel Messi", "Jude Bellingham"),
            audioTracks = listOf("English Atmos", "Spanish 5.1", "Portuguese 5.1"),
            servers = defaultServers,
            primaryColorHex = 0xFF9D4EDD,
            secondaryColorHex = 0xFF1A0B2E,
            isTrending = false
        ),
        MediaTitle(
            id = "ani_striker_blue",
            title = "Apex Striker: World Stage",
            year = 2026,
            rating = "9.0",
            qualityBadge = "1080p BluRay",
            durationOrSeasons = "28 Episodes",
            category = MediaCategory.ANIME,
            genres = listOf("Anime", "Sports", "Football"),
            synopsis = "Three hundred prodigy forwards enter an AI-monitored training fortress to engineer the world's most clinical number nine ahead of the International Cup.",
            director = "Haruka Sato",
            cast = listOf("Kaito Isagi", "Riku Shido", "Noel Noa"),
            audioTracks = listOf("Japanese Original", "English Dub"),
            servers = defaultServers,
            primaryColorHex = 0xFF0096FF,
            secondaryColorHex = 0xFF081C38,
            isTrending = false
        )
    )

    val initialMatches = listOf(
        FootballMatch(
            id = "match_ucl_rma_mci",
            competition = FootballCompetition.UCL,
            stageLabel = "Quarter-Final • 2nd Leg",
            homeTeam = "Real Madrid",
            homeShort = "RMA",
            homeColorHex = 0xFFF8FAFC,
            awayTeam = "Manchester City",
            awayShort = "MCI",
            awayColorHex = 0xFF38BDF8,
            homeScore = 2,
            awayScore = 2,
            minute = 78,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "Estadio Santiago Bernabéu, Madrid",
            attendance = "81,044",
            referee = "Clément Turpin (FRA)",
            homeFormation = "4-3-1-2",
            awayFormation = "3-2-4-1",
            homeKeyPlayers = listOf("Vinícius Jr", "Mbappé", "Bellingham", "Valverde", "Courtois"),
            awayKeyPlayers = listOf("Haaland", "De Bruyne", "Foden", "Rodri", "Ederson"),
            stats = MatchStats(
                homePossession = 44,
                awayPossession = 56,
                homeShotsOnTarget = 7,
                awayShotsOnTarget = 8,
                homeXg = 2.14f,
                awayXg = 2.08f,
                homeCorners = 5,
                awayCorners = 7,
                homePassAccuracy = 89,
                awayPassAccuracy = 93,
                homeMomentum = 0.62f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_ucl_1",
                    minute = 78,
                    type = CommentaryType.GOAL,
                    headline = "GOLAZO! Federico Valverde thunderbolt from 24 yards!",
                    detail = "Vinícius Jr floats a diagonal cross to the edge of the box and Valverde lashes a first-time volley into the bottom-left corner past Ederson! 2-2 at the Bernabéu!",
                    scorerOrPlayer = "F. Valverde (78')"
                ),
                CommentaryEvent(
                    id = "c_ucl_2",
                    minute = 69,
                    type = CommentaryType.GOAL,
                    headline = "GOAL! Phil Foden curls a clinical strike into the top corner!",
                    detail = "Manchester City patient build-up around the box finds Foden in a pocket of space; he shifts onto his left boot and bends an unstoppable finish into the roof of the net.",
                    scorerOrPlayer = "P. Foden (69')"
                ),
                CommentaryEvent(
                    id = "c_ucl_3",
                    minute = 54,
                    type = CommentaryType.MAJOR_CHANCE,
                    headline = "Double save by Courtois denies Haaland & De Bruyne!",
                    detail = "Haaland powers a point-blank header from a corner, parried away by Courtois before leaping back up to tip De Bruyne's rebound over the crossbar."
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_miami_mia_lag",
            competition = FootballCompetition.INTER_MIAMI,
            stageLabel = "MLS Spotlight Broadcast",
            homeTeam = "Inter Miami CF",
            homeShort = "MIA",
            homeColorHex = 0xFFFF2A85,
            awayTeam = "LA Galaxy",
            awayShort = "LAG",
            awayColorHex = 0xFFFFD13B,
            homeScore = 3,
            awayScore = 2,
            minute = 85,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "Chase Stadium, Fort Lauderdale",
            attendance = "21,550",
            referee = "Ismail Elfath (USA)",
            homeFormation = "4-3-3",
            awayFormation = "4-2-3-1",
            homeKeyPlayers = listOf("Lionel Messi (C)", "Luis Suárez", "Sergio Busquets", "Jordi Alba"),
            awayKeyPlayers = listOf("Riqui Puig", "Marco Reus", "Gabriel Pec", "Dejan Joveljić"),
            stats = MatchStats(
                homePossession = 61,
                awayPossession = 39,
                homeShotsOnTarget = 9,
                awayShotsOnTarget = 5,
                homeXg = 2.85f,
                awayXg = 1.64f,
                homeCorners = 8,
                awayCorners = 4,
                homePassAccuracy = 91,
                awayPassAccuracy = 84,
                homeMomentum = 0.74f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_mia_1",
                    minute = 85,
                    type = CommentaryType.GOAL,
                    headline = "MAGIC FROM MESSI! 25-yard free kick curls inside the near post!",
                    detail = "Lionel Messi steps up in the 85th minute and whips a trademark left-footed free-kick over the wall and into the top-right Postage Stamp! Chase Stadium erupts as Inter Miami take a 3-2 lead!",
                    scorerOrPlayer = "L. Messi (85')"
                ),
                CommentaryEvent(
                    id = "c_mia_2",
                    minute = 64,
                    type = CommentaryType.GOAL,
                    headline = "GOAL! Luis Suárez finishes a classic one-two with Jordi Alba!",
                    detail = "Busquets threads a disguised vertical pass to Alba, who cuts it back first-time for Suárez to dink over the keeper.",
                    scorerOrPlayer = "L. Suárez (64')"
                ),
                CommentaryEvent(
                    id = "c_mia_3",
                    minute = 51,
                    type = CommentaryType.MAJOR_CHANCE,
                    headline = "Riqui Puig hits the woodwork on the counter!",
                    detail = "LA Galaxy break rapidly through Puig who drives 40 yards and rattles the crossbar from outside the penalty arc."
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_epl_liv_che",
            competition = FootballCompetition.EPL,
            stageLabel = "Premier League • Matchweek 29",
            homeTeam = "Liverpool",
            homeShort = "LIV",
            homeColorHex = 0xFFFF3355,
            awayTeam = "Chelsea",
            awayShort = "CHE",
            awayColorHex = 0xFF2563EB,
            homeScore = 2,
            awayScore = 1,
            minute = 82,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "Anfield, Liverpool",
            attendance = "61,276",
            referee = "Michael Oliver (ENG)",
            homeFormation = "4-2-3-1",
            awayFormation = "4-2-3-1",
            homeKeyPlayers = listOf("Mohamed Salah", "Luis Díaz", "Mac Allister", "Van Dijk", "Alisson"),
            awayKeyPlayers = listOf("Cole Palmer", "Enzo Fernández", "Moisés Caicedo", "Nicolas Jackson"),
            stats = MatchStats(
                homePossession = 53,
                awayPossession = 47,
                homeShotsOnTarget = 6,
                awayShotsOnTarget = 5,
                homeXg = 2.12f,
                awayXg = 1.45f,
                homeCorners = 7,
                awayCorners = 5,
                homePassAccuracy = 87,
                awayPassAccuracy = 86,
                homeMomentum = 0.58f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_epl_1",
                    minute = 82,
                    type = CommentaryType.MAJOR_CHANCE,
                    headline = "Cole Palmer curls inches wide of the far post!",
                    detail = "Chelsea work a neat overload on the right flank; Palmer cuts inside Robertson and bends a low effort that whispers past the upright."
                ),
                CommentaryEvent(
                    id = "c_epl_2",
                    minute = 68,
                    type = CommentaryType.GOAL,
                    headline = "GOAL! Mohamed Salah slots home at the Kop End!",
                    detail = "Mac Allister slides a defense-splitting pass into the right channel and Salah arrows a clinical strike inside the far post for 2-1!",
                    scorerOrPlayer = "M. Salah (68')"
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_liga_bar_atm",
            competition = FootballCompetition.LALIGA,
            stageLabel = "LaLiga EA Sports • Matchday 30",
            homeTeam = "FC Barcelona",
            homeShort = "BAR",
            homeColorHex = 0xFFEC4899,
            awayTeam = "Atlético Madrid",
            awayShort = "ATM",
            awayColorHex = 0xFFEF4444,
            homeScore = 3,
            awayScore = 1,
            minute = 73,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "Spotify Camp Nou, Barcelona",
            attendance = "89,400",
            referee = "Jesús Gil Manzano (ESP)",
            homeFormation = "4-3-3",
            awayFormation = "5-3-2",
            homeKeyPlayers = listOf("Lamine Yamal", "Pedri", "Raphinha", "Lewandowski", "Cubarsí"),
            awayKeyPlayers = listOf("Antoine Griezmann", "Julián Álvarez", "Koke", "Jan Oblak"),
            stats = MatchStats(
                homePossession = 64,
                awayPossession = 36,
                homeShotsOnTarget = 8,
                awayShotsOnTarget = 3,
                homeXg = 2.66f,
                awayXg = 1.05f,
                homeCorners = 9,
                awayCorners = 2,
                homePassAccuracy = 92,
                awayPassAccuracy = 81,
                homeMomentum = 0.71f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_liga_1",
                    minute = 73,
                    type = CommentaryType.GOAL,
                    headline = "SENSATIONAL! Lamine Yamal cuts inside and bends it into the far corner!",
                    detail = "18-year-old Lamine Yamal dances past two defenders on the right wing and unleashes a curling left-footed strike past Oblak for 3-1!",
                    scorerOrPlayer = "L. Yamal (73')"
                ),
                CommentaryEvent(
                    id = "c_liga_2",
                    minute = 55,
                    type = CommentaryType.GOAL,
                    headline = "GOAL! Raphinha finishes off Pedri's silk through-ball!",
                    detail = "Pedri threads an outside-of-the-boot pass between the center-backs and Raphinha rounds Oblak to score.",
                    scorerOrPlayer = "Raphinha (55')"
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_seria_int_mil",
            competition = FootballCompetition.SERIE_A,
            stageLabel = "Serie A • Derby della Madonnina",
            homeTeam = "Inter Milan",
            homeShort = "INT",
            homeColorHex = 0xFF0096FF,
            awayTeam = "AC Milan",
            awayShort = "MIL",
            awayColorHex = 0xFFFF2A55,
            homeScore = 1,
            awayScore = 1,
            minute = 66,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "San Siro, Milan",
            attendance = "75,817",
            referee = "Daniele Orsato (ITA)",
            homeFormation = "3-5-2",
            awayFormation = "4-2-3-1",
            homeKeyPlayers = listOf("Lautaro Martínez", "Nicolò Barella", "Marcus Thuram", "Bastoni"),
            awayKeyPlayers = listOf("Rafael Leão", "Christian Pulisic", "Theo Hernández", "Maignan"),
            stats = MatchStats(
                homePossession = 51,
                awayPossession = 49,
                homeShotsOnTarget = 5,
                awayShotsOnTarget = 5,
                homeXg = 1.54f,
                awayXg = 1.48f,
                homeCorners = 6,
                awayCorners = 5,
                homePassAccuracy = 88,
                awayPassAccuracy = 86,
                homeMomentum = 0.50f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_sa_1",
                    minute = 66,
                    type = CommentaryType.VAR_CHECK,
                    headline = "VAR Check Complete: No Penalty on Marcus Thuram",
                    detail = "After reviewing Theo Hernández's sliding challenge inside the box, VAR confirms clean contact with the ball. Play resumes at San Siro!"
                ),
                CommentaryEvent(
                    id = "c_sa_2",
                    minute = 52,
                    type = CommentaryType.GOAL,
                    headline = "GOAL! Rafael Leão equalizes with a blistering solo run!",
                    detail = "Leão accelerates past two challenges down the left channel and fires low across Sommer into the far corner! 1-1!",
                    scorerOrPlayer = "R. Leão (52')"
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_bun_lev_bvb",
            competition = FootballCompetition.BUNDESLIGA,
            stageLabel = "Bundesliga • Topspiel",
            homeTeam = "Bayer Leverkusen",
            homeShort = "B04",
            homeColorHex = 0xFFFF3B5C,
            awayTeam = "Borussia Dortmund",
            awayShort = "BVB",
            awayColorHex = 0xFFFFD13B,
            homeScore = 2,
            awayScore = 2,
            minute = 80,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "BayArena, Leverkusen",
            attendance = "30,210",
            referee = "Felix Zwayer (GER)",
            homeFormation = "3-4-2-1",
            awayFormation = "4-3-3",
            homeKeyPlayers = listOf("Florian Wirtz", "Grimaldo", "Granit Xhaka", "Victor Boniface"),
            awayKeyPlayers = listOf("Serhou Guirassy", "Karim Adeyemi", "Julian Brandt", "Gregor Kobel"),
            stats = MatchStats(
                homePossession = 58,
                awayPossession = 42,
                homeShotsOnTarget = 7,
                awayShotsOnTarget = 6,
                homeXg = 2.20f,
                awayXg = 1.92f,
                homeCorners = 8,
                awayCorners = 4,
                homePassAccuracy = 90,
                awayPassAccuracy = 84,
                homeMomentum = 0.66f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_bun_1",
                    minute = 80,
                    type = CommentaryType.GOAL,
                    headline = "TOR! Florian Wirtz weaves through the box for 2-2!",
                    detail = "Wirtz exchanges a razor-sharp wall pass with Boniface and dinks the ball over Kobel as Leverkusen push for another late winner!",
                    scorerOrPlayer = "F. Wirtz (80')"
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_friendly_bra_arg",
            competition = FootballCompetition.INTL_FRIENDLY,
            stageLabel = "International Friendly • Superclásico",
            homeTeam = "Brazil",
            homeShort = "BRA",
            homeColorHex = 0xFFFFD13B,
            awayTeam = "Argentina",
            awayShort = "ARG",
            awayColorHex = 0xFF38BDF8,
            homeScore = 1,
            awayScore = 1,
            minute = 56,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "Estádio do Maracanã, Rio de Janeiro",
            attendance = "78,838",
            referee = "Wilton Sampaio",
            homeFormation = "4-2-3-1",
            awayFormation = "4-3-3",
            homeKeyPlayers = listOf("Vinícius Jr", "Rodrygo", "Endrick", "Bruno Guimarães"),
            awayKeyPlayers = listOf("Lionel Messi", "Julián Álvarez", "Alexis Mac Allister", "Emi Martínez"),
            stats = MatchStats(
                homePossession = 50,
                awayPossession = 50,
                homeShotsOnTarget = 5,
                awayShotsOnTarget = 4,
                homeXg = 1.38f,
                awayXg = 1.31f,
                homeCorners = 5,
                awayCorners = 4,
                homePassAccuracy = 88,
                awayPassAccuracy = 90,
                homeMomentum = 0.52f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_fr_1",
                    minute = 56,
                    type = CommentaryType.TACTICAL,
                    headline = "Endrick enters to electrify the Brazil attack",
                    detail = "High-tempo South American showcase at the Maracanã as both nations test their attacking trios."
                )
            ),
            streamServers = footballBroadcastServers
        ),
        FootballMatch(
            id = "match_cup_esp_fra",
            competition = FootballCompetition.INTL_CUP,
            stageLabel = "International Cup Competition • Final Stage",
            homeTeam = "Spain",
            homeShort = "ESP",
            homeColorHex = 0xFFFF3355,
            awayTeam = "France",
            awayShort = "FRA",
            awayColorHex = 0xFF3B82F6,
            homeScore = 2,
            awayScore = 1,
            minute = 79,
            addedTime = 0,
            isLive = true,
            kickoffLabel = "LIVE • 2nd Half",
            venue = "Olympiastadion, Berlin",
            attendance = "71,000",
            referee = "François Letexier",
            homeFormation = "4-3-3",
            awayFormation = "4-3-3",
            homeKeyPlayers = listOf("Rodri", "Lamine Yamal", "Nico Williams", "Dani Olmo"),
            awayKeyPlayers = listOf("Kylian Mbappé", "Ousmane Dembélé", "William Saliba", "Maignan"),
            stats = MatchStats(
                homePossession = 59,
                awayPossession = 41,
                homeShotsOnTarget = 6,
                awayShotsOnTarget = 5,
                homeXg = 1.94f,
                awayXg = 1.52f,
                homeCorners = 6,
                awayCorners = 5,
                homePassAccuracy = 91,
                awayPassAccuracy = 87,
                homeMomentum = 0.60f
            ),
            commentary = listOf(
                CommentaryEvent(
                    id = "c_cup_1",
                    minute = 79,
                    type = CommentaryType.GOAL,
                    headline = "GOAL! Dani Olmo turns on a dime inside the box for 2-1!",
                    detail = "Nico Williams fizzes a low cross into the penalty area; Olmo deftly flicks it past Tchouaméni and volleys firmly into the net!",
                    scorerOrPlayer = "D. Olmo (79')"
                )
            ),
            streamServers = footballBroadcastServers
        )
    )

    val top20AbstractThemes = listOf(
        FootballThemeOption("messi_monochrome_goat", 1, "Messi #10 Black & White", "Simple Monochrome GOAT • Glass Edition", 0xFF050505, 0xFF141414, 0xFFFFFFFF, 0xFFD4D4D8, isMessiMonochrome = true),
        FootballThemeOption("touchthegrass_emerald", 2, "TouchTheGrass Pure Pitch", "Clean Emerald Turf & Obsidian Glass", 0xFF04140B, 0xFF0A2918, 0xFF00FF87, 0xFF00E5FF),
        FootballThemeOption("miami_vice_10", 3, "Inter Miami #10 Vice", "Neon Heron Pink & Midnight Onyx", 0xFF120510, 0xFF1E0A1C, 0xFFFF2A85, 0xFF00F5D4),
        FootballThemeOption("camp_nou_blaugrana", 4, "Blaugrana Dynasty", "Abstract Crimson & Royal Indigo Mesh", 0xFF0B0924, 0xFF280819, 0xFFFF2A55, 0xFF3B82F6),
        FootballThemeOption("albiceleste_world_cup", 5, "Albiceleste Sky & Gold", " Lusail Final Sky Blue & World Cup Gold", 0xFF061626, 0xFF0F2942, 0xFF38BDF8, 0xFFFFD13B),
        FootballThemeOption("ucl_starball_midnight", 6, "UCL Starball Midnight", "Champions League Deep Royal & Cyan", 0xFF050B24, 0xFF0D1B42, 0xFF00E5FF, 0xFF818CF8),
        FootballThemeOption("epl_electric_lion", 7, "Premier League Prism", "Electric Violet & Hyper Mint Abstract", 0xFF130726, 0xFF220E3B, 0xFFA855F7, 0xFF00FF87),
        FootballThemeOption("laliga_coral_obsidian", 8, "LaLiga El Clásico Fire", "Volcanic Coral & Carbon Glass", 0xFF1A0806, 0xFF2B100C, 0xFFFF5E3A, 0xFFFFD13B),
        FootballThemeOption("bernabeu_silver_crown", 9, "Bernabéu Galáctico", "Platinum White, Royal Gold & Slate", 0xFF0D1117, 0xFF1B222D, 0xFFF8FAFC, 0xFFFACC15),
        FootballThemeOption("san_siro_rossoneri", 10, "San Siro Cathedral", "Milan Crimson Waves & Pitch Shadow", 0xFF170407, 0xFF26090E, 0xFFEF4444, 0xFFF8FAFC),
        FootballThemeOption("allianz_crimson_hex", 11, "Allianz Arena Hex Grid", "Bavarian Neon Red & Deep Graphite", 0xFF18060B, 0xFF290B14, 0xFFFF1E42, 0xFF60A5FA),
        FootballThemeOption("wembley_arch_aurora", 12, "Wembley Arch Aurora", "Electric Turf & Silver Floodlight", 0xFF061814, 0xFF0E2923, 0xFF10B981, 0xFFE2E8F0),
        FootballThemeOption("maracana_samba_gold", 13, "Maracanã Samba Pulse", "Canary Gold & Amazon Emerald", 0xFF0F1A06, 0xFF1B2B0C, 0xFFFACC15, 0xFF22C55E),
        FootballThemeOption("signal_iduna_wall", 14, "Yellow Wall Cyber", "High-Voltage Cyber Yellow & Pitch Black", 0xFF141204, 0xFF242008, 0xFFFDE047, 0xFFFFFFFF),
        FootballThemeOption("anfield_kop_inferno", 15, "Anfield Kop Flare", "Deep Scarlet & Golden Crest Glow", 0xFF1C0508, 0xFF2E0B10, 0xFFF43F5E, 0xFFFBBF24),
        FootballThemeOption("etihad_sky_laser", 16, "Cityzen Sky Laser", "Ice Cyan & Deep Sapphire Glass", 0xFF051524, 0xFF0B253D, 0xFF38BDF8, 0xFFA5F3FC),
        FootballThemeOption("paris_parc_neon", 17, "Parc des Princes Night", "Midnight Navy & Electric Rouge", 0xFF070C1F, 0xFF121A36, 0xFFFB7185, 0xFF60A5FA),
        FootballThemeOption("cruyff_total_orange", 18, "Total Football 14", "Dutch Kinetic Orange & Dark Slate", 0xFF1A0C04, 0xFF2B1508, 0xFFFB923C, 0xFFF8FAFC),
        FootballThemeOption("cyber_stadium_2077", 19, "Hologram Pitch 2077", "Neon Magenta & Telemetry Cyan Grid", 0xFF09061C, 0xFF160E36, 0xFF00E5FF, 0xFFD946EF),
        FootballThemeOption("minimal_chalk_tactics", 20, "Tactical Chalkboard", "Clean Coach Slate & White Pitch Lines", 0xFF0B1215, 0xFF162228, 0xFFE2E8F0, 0xFF34D399)
    )

    val avatarPresets = listOf(
        AvatarPreset("avatar_messi_goat", "Messi #10 GOAT", "M10", 0xFFFFFFFF, 0xFF111111),
        AvatarPreset("avatar_inter_miami", "Inter Miami Heron", "MIA", 0xFFFF2A85, 0xFF120510),
        AvatarPreset("avatar_barca_10", "Blaugrana #10", "FCB", 0xFFFF2A55, 0xFF1D4ED8),
        AvatarPreset("avatar_argentina_star", "3-Star Albiceleste", "ARG", 0xFF38BDF8, 0xFFFACC15),
        AvatarPreset("avatar_ucl_captain", "UCL Starball Pro", "UCL", 0xFF00E5FF, 0xFF0B192C),
        AvatarPreset("avatar_touch_grass", "TouchTheGrass VIP", "TTG", 0xFF00FF87, 0xFF052E16)
    )

    val popularClubs = listOf(
        "Inter Miami CF",
        "FC Barcelona",
        "Argentina National Team",
        "Real Madrid",
        "Manchester City",
        "Arsenal",
        "Liverpool",
        "Bayern Munich",
        "AC Milan",
        "Paris Saint-Germain"
    )
}

