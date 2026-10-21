package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SearchGroundingResult
import com.example.network.GeminiRepository
import com.example.ui.theme.BroadcastCard
import com.example.ui.theme.BroadcastCardElevated
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.MiamiPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.StreamCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UclGold

@Composable
fun AiScoutAndVisionScreen(
    searchQuery: String,
    isSearching: Boolean,
    searchResult: SearchGroundingResult?,
    selectedBitmap: Bitmap?,
    visionPrompt: String,
    isAnalyzingImage: Boolean,
    visionResult: String?,
    selectedTtsVoice: String,
    ttsStatus: String?,
    isTtsLoading: Boolean,
    onUpdateSearchQuery: (String) -> Unit,
    onExecuteSearchGrounding: (String?) -> Unit,
    onUpdateVisionBitmap: (Bitmap) -> Unit,
    onLoadSampleTacticalBitmap: () -> Unit,
    onUpdateVisionPrompt: (String) -> Unit,
    onAnalyzeImage: () -> Unit,
    onSelectVoice: (String) -> Unit,
    onSpeakText: (String) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Zero-permission Android Photo Picker for image understanding
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onUpdateVisionBitmap(bitmap)
            } catch (_: Exception) {
            }
        }
    }

    val quickSearchPrompts = listOf(
        "UCL & EPL Live Standings & Top Scorers",
        "Inter Miami CF & Lionel Messi Latest Match Result",
        "LaLiga, Serie A & Bundesliga Weekend Fixtures",
        "International Friendly & International Cup Schedule",
        "Top 2026 Sci-Fi & Action 4K Movie Releases"
    )

    val voices = listOf("Kore", "Puck", "Fenrir", "Aoede")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_scout_vision_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Google Search Grounding Card (gemini-3.8-flash + googleSearch tool)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StreamCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Google Search Grounding",
                            tint = StreamCyan
                        )
                        Column {
                            Text(
                                text = "GOOGLE SEARCH GROUNDING PULSE",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Model: ${GeminiRepository.MODEL_SEARCH_PRIMARY} (with googleSearch tool)",
                                style = MaterialTheme.typography.labelSmall,
                                color = StreamCyan
                            )
                        }
                    }

                    Text(
                        text = "Query live, web-grounded football tables, UCL/EPL/LaLiga/Serie A/Bundesliga scores, Inter Miami news, or movie facts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickSearchPrompts) { chipPrompt ->
                            FilterChip(
                                selected = searchQuery == chipPrompt,
                                onClick = { onExecuteSearchGrounding(chipPrompt) },
                                label = {
                                    Text(
                                        text = chipPrompt,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = BroadcastCardElevated,
                                    labelColor = TextPrimary,
                                    selectedContainerColor = StreamCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = StreamCyan
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onUpdateSearchQuery,
                        label = { Text("Google Search Grounding Query") },
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StreamCyan,
                            unfocusedBorderColor = DividerSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_grounding_input")
                    )

                    Button(
                        onClick = { onExecuteSearchGrounding(null) },
                        enabled = !isSearching,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StreamCyan,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_grounding_button")
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ObsidianBg
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Searching Live Google Data...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Run Google Search Grounding"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Get Live Grounded Answer",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (searchResult != null) {
                        HorizontalDivider(color = DividerSubtle)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BroadcastCardElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${searchResult.modelUsed} • ${searchResult.timestampLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PitchEmerald
                                )
                                OutlinedButton(
                                    onClick = { onSpeakText(searchResult.answer) }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak Grounded Result",
                                        tint = PitchEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Speak (TTS)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchEmerald
                                    )
                                }
                            }

                            Text(
                                text = searchResult.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )

                            if (searchResult.sources.isNotEmpty()) {
                                Text(
                                    text = "Verified Google Search Sources:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StreamCyan
                                )
                                searchResult.sources.forEach { src ->
                                    Surface(
                                        onClick = {
                                            try {
                                                uriHandler.openUri(src.url)
                                            } catch (_: Exception) {
                                            }
                                        },
                                        color = ObsidianBg,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🔗 ${src.title} (${src.url})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StreamCyan,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Analyze Images Card (gemini-4-argon)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MiamiPink.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Analyze Match or Movie Images",
                            tint = MiamiPink
                        )
                        Column {
                            Text(
                                text = "MATCH & CINEMA VISION ANALYZER",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Model: ${GeminiRepository.MODEL_VISION_PRIMARY} (Multimodal Image Understanding)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MiamiPink
                            )
                        }
                    }

                    Text(
                        text = "Upload any photo (football formation, live match screenshot, jersey, or movie poster) and analyze it using ${GeminiRepository.MODEL_VISION_PRIMARY}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    if (selectedBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(185.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DividerSubtle, RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                bitmap = selectedBitmap.asImageBitmap(),
                                contentDescription = "Selected photo for Gemini image analysis",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("upload_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Photo",
                                tint = MiamiPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Upload Photo",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = onLoadSampleTacticalBitmap,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = "Load Tactical Snapshot",
                                tint = PitchEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tactical Snapshot",
                                style = MaterialTheme.typography.labelLarge,
                                color = PitchEmerald
                            )
                        }
                    }

                    OutlinedTextField(
                        value = visionPrompt,
                        onValueChange = onUpdateVisionPrompt,
                        label = { Text("Image Analysis Prompt") },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MiamiPink,
                            unfocusedBorderColor = DividerSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vision_prompt_input")
                    )

                    Button(
                        onClick = onAnalyzeImage,
                        enabled = !isAnalyzingImage,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MiamiPink,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyze_image_button")
                    ) {
                        if (isAnalyzingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Photo with ${GeminiRepository.MODEL_VISION_PRIMARY}...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Analyze Image"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyze Photo (${GeminiRepository.MODEL_VISION_PRIMARY})",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (visionResult != null) {
                        HorizontalDivider(color = DividerSubtle)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BroadcastCardElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vision Breakdown",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MiamiPink
                                )
                                OutlinedButton(
                                    onClick = { onSpeakText(visionResult) }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Read Vision Analysis Aloud",
                                        tint = PitchEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Read Aloud (TTS)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchEmerald
                                    )
                                }
                            }
                            Text(
                                text = visionResult,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Gemini Text-to-Speech Voice Studio Card (gemini-3.8-flash-tts)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchEmerald.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Gemini TTS Studio",
                            tint = PitchEmerald
                        )
                        Column {
                            Text(
                                text = "GEMINI BROADCAST TTS STUDIO",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Model: ${GeminiRepository.MODEL_TTS_PRIMARY}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchEmerald
                            )
                        }
                    }

                    Text(
                        text = "Select your preferred AI stadium commentator voice for live match commentary and cinema synopses:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(voices) { voice ->
                            val selected = voice == selectedTtsVoice
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectVoice(voice) },
                                label = {
                                    Text(
                                        text = "Voice: $voice",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PitchEmerald.copy(alpha = 0.22f),
                                    selectedLabelColor = PitchEmerald,
                                    containerColor = BroadcastCardElevated,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onSpeakText(
                                "Welcome to FrameXTV Live! Tonight we have UEFA Champions League, Premier League, LaLiga, Serie A, Bundesliga, Inter Miami, and International Cup action streaming in 4K with real-time commentary!"
                            )
                        },
                        enabled = !isTtsLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PitchEmerald,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tts_studio_sample_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play Broadcast Welcome"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTtsLoading) "Generating Speech..." else "Test Commentator Voice ($selectedTtsVoice)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (ttsStatus != null) {
                        Text(
                            text = ttsStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = UclGold
                        )
                    }
                }
            }
        }
    }
}
