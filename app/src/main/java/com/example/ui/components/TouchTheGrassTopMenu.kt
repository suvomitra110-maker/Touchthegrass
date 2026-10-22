package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.FootballCompetition
import com.example.model.FootballThemeOption
import com.example.model.SeedCatalog
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Clean OLED Palette matching IMG_8275 & IMG_8276
val OledBlack = Color(0xFF060709)
val OledCardSurface = Color(0xFF17181C)
val OledCardBorder = Color(0xFF26282E)
val OledMutedText = Color(0xFF8A8F99)
val VerifiedPillGreen = Color(0xFF10B981)
val SwitchActiveGreen = Color(0xFF22C55E)
val PremiumSlateBg = Color(0xFF6E7F8D)

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: (uid: String, displayName: String?, email: String?, photoUrl: String?) -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    val auth = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val existingUser = auth.currentUser
    if (existingUser != null) {
        onAuthSuccess(
            existingUser.uid,
            existingUser.displayName,
            existingUser.email,
            existingUser.photoUrl?.toString()
        )
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    onAuthSuccess(
                        user.uid,
                        user.displayName,
                        user.email,
                        user.photoUrl?.toString()
                    )
                } else {
                    onUnauthenticated()
                }
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: (uid: String, displayName: String?, email: String?, photoUrl: String?) -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activityContext = (context as? Activity) ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    onAuthSuccess(
                        user.uid,
                        user.displayName,
                        user.email,
                        user.photoUrl?.toString()
                    )
                } else {
                    onAuthError("Sign-in succeeded but user record was null")
                }
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Google Sign-In failed")
        }
    }
}

fun signOutGoogle(
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    try {
        FirebaseAuth.getInstance().signOut()
    } catch (_: Exception) {
    }
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun UserAvatarCircle(
    avatarId: String,
    customBitmap: Bitmap?,
    size: Dp = 56.dp,
    borderColor: Color = Color.White.copy(alpha = 0.25f),
    modifier: Modifier = Modifier
) {
    val preset = SeedCatalog.avatarPresets.find { it.id == avatarId }
        ?: SeedCatalog.avatarPresets.first()

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(preset.primaryHex), Color(preset.secondaryHex))
                )
            )
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap.asImageBitmap(),
                contentDescription = "User Circular Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = preset.badgeText,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.30f).sp,
                color = if (preset.primaryHex == 0xFFFFFFFF) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun MessiAndAbstractBackground(
    theme: FootballThemeOption,
    modifier: Modifier = Modifier
) {
    val topColor = if (theme.isMessiMonochrome) OledBlack else Color(theme.bgTopHex)
    val bottomColor = if (theme.isMessiMonochrome) Color(0xFF0B0C0E) else Color(theme.bgBottomHex)
    val accent = Color(theme.accentHex)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor, topColor)
            )
        )
        if (!theme.isMessiMonochrome) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.minDimension * 0.55f
                ),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.85f, size.height * 0.12f)
            )
        } else {
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = size.minDimension * 0.38f,
                center = Offset(size.width * 0.85f, size.height * 0.15f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

/**
 * Full-Screen or Modal "My Profile" & "Settings" Experience modeled directly on IMG_8276 & IMG_8275
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TouchTheGrassProfileAndSettingsScreen(
    initialShowSettings: Boolean = false,
    isSignedIn: Boolean,
    userName: String,
    userEmail: String?,
    favoriteClub: String,
    profilePictureId: String,
    customAvatarBitmap: Bitmap?,
    selectedTheme: FootballThemeOption,
    enabledLeagues: Set<FootballCompetition>,
    flagSecureEnabled: Boolean,
    wafEnabled: Boolean,
    selectedTtsVoice: String,
    liveMatchCount: Int,
    savedWatchlistCount: Int,
    securityScore: Int,
    onGoogleAuthSuccess: (uid: String, displayName: String?, email: String?, photoUrl: String?) -> Unit,
    onGoogleSignedOut: () -> Unit,
    onAuthError: (String) -> Unit,
    onSaveProfile: (name: String, club: String, avatarId: String) -> Unit,
    onUploadCustomAvatar: (Bitmap) -> Unit,
    onSelectTheme: (FootballThemeOption) -> Unit,
    onToggleLeague: (FootballCompetition) -> Unit,
    onEnableAllLeagues: () -> Unit,
    onToggleFlagSecure: (Boolean) -> Unit,
    onToggleWaf: (Boolean) -> Unit,
    onSelectVoice: (String) -> Unit,
    onOpenSecurityShield: () -> Unit,
    onOpenAlertsAndWatchlist: () -> Unit,
    onCloseIfModal: (() -> Unit)? = null
) {
    var showSettingsPage by rememberSaveable(initialShowSettings) { mutableStateOf(initialShowSettings) }
    var expandAccountEditor by rememberSaveable { mutableStateOf(false) }
    var expandThemesPicker by rememberSaveable { mutableStateOf(false) }
    var expandLeaguesPicker by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isSigningIn by remember { mutableStateOf(false) }
    var draftName by remember(userName) { mutableStateOf(userName) }
    var draftClub by remember(favoriteClub) { mutableStateOf(favoriteClub) }
    var draftAvatarId by remember(profilePictureId) { mutableStateOf(profilePictureId) }

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
                onUploadCustomAvatar(bitmap)
            } catch (e: Exception) {
                onAuthError("Could not load selected photo: ${e.localizedMessage}")
            }
        }
    }

    if (showSettingsPage) {
        // --- SCREEN 2: SETTINGS (Exact IMG_8275 UI Layout) ---
        SettingsScreenContent(
            isSignedIn = isSignedIn,
            userName = userName,
            favoriteClub = favoriteClub,
            selectedTheme = selectedTheme,
            enabledLeagues = enabledLeagues,
            flagSecureEnabled = flagSecureEnabled,
            wafEnabled = wafEnabled,
            selectedTtsVoice = selectedTtsVoice,
            expandThemesPicker = expandThemesPicker,
            expandLeaguesPicker = expandLeaguesPicker,
            onToggleExpandThemes = { expandThemesPicker = !expandThemesPicker },
            onToggleExpandLeagues = { expandLeaguesPicker = !expandLeaguesPicker },
            onSelectTheme = onSelectTheme,
            onToggleLeague = onToggleLeague,
            onEnableAllLeagues = onEnableAllLeagues,
            onToggleFlagSecure = onToggleFlagSecure,
            onToggleWaf = onToggleWaf,
            onSelectVoice = onSelectVoice,
            onSwitchToProfile = { showSettingsPage = false },
            onOpenSecurityShield = onOpenSecurityShield,
            onCloseIfModal = onCloseIfModal
        )
    } else {
        // --- SCREEN 1: MY PROFILE (Exact IMG_8276 UI Layout) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("my_profile_screen_container")
        ) {
            // Top Header: Serif "My Profile" + Subtitle + Top-Right Circular Gear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Profile",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 30.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Manage your profile and\naccount information",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OledMutedText,
                        lineHeight = 19.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Circular Settings Gear Button (Matches IMG_8276 top-right button)
                    Surface(
                        color = OledCardSurface,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, OledCardBorder),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { showSettingsPage = true }
                            .testTag("open_settings_gear_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (onCloseIfModal != null) {
                        Surface(
                            color = OledCardSurface,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, OledCardBorder),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { onCloseIfModal() }
                                .testTag("close_glass_menu_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Profile Card with Floating Green "✓ Verified" Pill Badge in Top-Right (Matches IMG_8276)
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = OledCardSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, OledCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable { expandAccountEditor = !expandAccountEditor }
                        .testTag("profile_header_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Round Profile Picture
                        Box(contentAlignment = Alignment.BottomEnd) {
                            UserAvatarCircle(
                                avatarId = draftAvatarId,
                                customBitmap = customAvatarBitmap,
                                size = 64.dp,
                                borderColor = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("menu_user_avatar_circle")
                            )
                            Surface(
                                color = VerifiedPillGreen,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Upload Photo",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = Color.White,
                                modifier = Modifier.testTag("menu_user_name")
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = favoriteClub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OledMutedText,
                                fontSize = 13.sp,
                                modifier = Modifier.testTag("menu_favorite_club")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userEmail ?: "suvomitra110@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = OledMutedText,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Edit Profile",
                            tint = OledMutedText
                        )
                    }
                }

                // Floating Green "✓ Verified" Pill Badge overlapping top-right corner of card
                Surface(
                    color = VerifiedPillGreen,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = (-2).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSignedIn) "Verified" else "Google Sync",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Expandable Google Sign-In & Profile Editor Drawer
            AnimatedVisibility(visible = expandAccountEditor) {
                Surface(
                    color = OledCardSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OledCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Edit Profile, Club & Circular Photo",
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        OutlinedTextField(
                            value = draftName,
                            onValueChange = { draftName = it },
                            label = { Text("Your Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerifiedPillGreen,
                                unfocusedBorderColor = OledCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input")
                        )

                        Text(
                            text = "Circular Avatar Presets (or tap camera on avatar to upload photo):",
                            style = MaterialTheme.typography.labelSmall,
                            color = OledMutedText
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(SeedCatalog.avatarPresets) { preset ->
                                val selected = draftAvatarId == preset.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        draftAvatarId = preset.id
                                        onSaveProfile(draftName, draftClub, preset.id)
                                    }
                                ) {
                                    UserAvatarCircle(
                                        avatarId = preset.id,
                                        customBitmap = null,
                                        size = 42.dp,
                                        borderColor = if (selected) VerifiedPillGreen else OledCardBorder
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = preset.badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) VerifiedPillGreen else OledMutedText
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Favorite Football Club:",
                            style = MaterialTheme.typography.labelSmall,
                            color = OledMutedText
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SeedCatalog.popularClubs) { club ->
                                val selected = draftClub == club
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        draftClub = club
                                        onSaveProfile(draftName, club, draftAvatarId)
                                    },
                                    label = { Text(club, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.22f),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onSaveProfile(draftName, draftClub, draftAvatarId)
                                    expandAccountEditor = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerifiedPillGreen,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_profile_button")
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                            if (isSignedIn) {
                                OutlinedButton(
                                    onClick = {
                                        signOutGoogle(
                                            credentialManager = credentialManager,
                                            onSignOutComplete = onGoogleSignedOut,
                                            scope = scope
                                        )
                                    },
                                    border = BorderStroke(1.dp, OledCardBorder),
                                    modifier = Modifier.testTag("google_sign_out_button")
                                ) {
                                    Text("Sign Out", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Premium Member / Google Sign-In Slate Card (Exact IMG_8276 Steel-Blue Card with Crown & Black Pill Button)
            Surface(
                color = PremiumSlateBg,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFD6E4F0),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Premium Member",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSignedIn) "Premium member" else "TouchTheGrass Club",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101418)
                        )
                        Text(
                            text = if (isSignedIn) "Enjoy exclusive 4K benefits" else "Sign in with Google to sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1F272E)
                        )
                    }

                    Button(
                        onClick = {
                            if (!isSignedIn) {
                                isSigningIn = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = { uid, name, email, photo ->
                                        isSigningIn = false
                                        onGoogleAuthSuccess(uid, name, email, photo)
                                    },
                                    onAuthError = { err ->
                                        isSigningIn = false
                                        onAuthError(err)
                                    },
                                    scope = scope,
                                    onAuthCancelled = { isSigningIn = false }
                                )
                            } else {
                                expandThemesPicker = !expandThemesPicker
                            }
                        },
                        enabled = !isSigningIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF090A0C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("google_sign_in_button")
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignedIn) "VIP Active" else "Google Sign-In",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // "Account Overview" Serif Section Header + 4-Tile Metric Row (Exact IMG_8276 layout)
            Text(
                text = "Account Overview",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewStatTile(
                    label = "Live Feeds",
                    value = "$liveMatchCount",
                    modifier = Modifier.weight(1f)
                )
                OverviewStatTile(
                    label = "Leagues",
                    value = "${enabledLeagues.size}/8",
                    modifier = Modifier.weight(1f)
                )
                OverviewStatTile(
                    label = "Stream",
                    value = "4K HDR",
                    modifier = Modifier.weight(1f)
                )
                OverviewStatTile(
                    label = "Rating",
                    value = "4.9",
                    showStar = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Rounded Action Row Cards (Matching IMG_8276 bottom list rows)
            ProfileActionRowCard(
                icon = Icons.Default.Dns,
                iconTint = Color(0xFF38BDF8),
                title = "Customize Leagues (${enabledLeagues.size} Active)",
                onClick = { expandLeaguesPicker = !expandLeaguesPicker }
            )

            AnimatedVisibility(visible = expandLeaguesPicker) {
                LeagueToggleCardContent(
                    enabledLeagues = enabledLeagues,
                    onToggleLeague = onToggleLeague,
                    onEnableAllLeagues = onEnableAllLeagues
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ProfileActionRowCard(
                icon = Icons.Default.History,
                iconTint = Color(0xFFF59E0B),
                title = "Top 20 Abstract Themes (#${selectedTheme.rank} ${selectedTheme.name})",
                onClick = { expandThemesPicker = !expandThemesPicker }
            )

            AnimatedVisibility(visible = expandThemesPicker) {
                Top20ThemesListContent(
                    selectedTheme = selectedTheme,
                    onSelectTheme = onSelectTheme
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ProfileActionRowCard(
                icon = Icons.Default.Place,
                iconTint = Color(0xFF10B981),
                title = "Saved Matches & Push Alerts ($savedWatchlistCount)",
                onClick = {
                    onCloseIfModal?.invoke()
                    onOpenAlertsAndWatchlist()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileActionRowCard(
                icon = Icons.Default.Redeem,
                iconTint = Color(0xFFD946EF),
                title = "App Settings & Cyber Shield ($securityScore%)",
                onClick = { showSettingsPage = true }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Exact Settings Screen matching IMG_8275:
 * - Serif "Settings" title
 * - "Invite Friends" top banner card
 * - "Details" section with Blue Circular Icons + "Setup 2FA" green pill
 * - "App" section with Amber Circular Icons + Green Switches
 * - "General" section with Purple Circular Icons + Chevrons
 */
@Composable
private fun SettingsScreenContent(
    isSignedIn: Boolean,
    userName: String,
    favoriteClub: String,
    selectedTheme: FootballThemeOption,
    enabledLeagues: Set<FootballCompetition>,
    flagSecureEnabled: Boolean,
    wafEnabled: Boolean,
    selectedTtsVoice: String,
    expandThemesPicker: Boolean,
    expandLeaguesPicker: Boolean,
    onToggleExpandThemes: () -> Unit,
    onToggleExpandLeagues: () -> Unit,
    onSelectTheme: (FootballThemeOption) -> Unit,
    onToggleLeague: (FootballCompetition) -> Unit,
    onEnableAllLeagues: () -> Unit,
    onToggleFlagSecure: (Boolean) -> Unit,
    onToggleWaf: (Boolean) -> Unit,
    onSelectVoice: (String) -> Unit,
    onSwitchToProfile: () -> Unit,
    onOpenSecurityShield: () -> Unit,
    onCloseIfModal: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("settings_screen_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 30.sp,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = OledCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, OledCardBorder),
                    modifier = Modifier.clickable { onSwitchToProfile() }
                ) {
                    Text(
                        text = "My Profile",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                if (onCloseIfModal != null) {
                    IconButton(onClick = onCloseIfModal) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Top "Invite Friends" Card (Exact IMG_8275 banner card)
        Surface(
            color = OledCardSurface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, OledCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF25272C),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = "Invite Friends",
                            tint = VerifiedPillGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Invite Friends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Share Touchthegrass.com 4K live streams!",
                        style = MaterialTheme.typography.bodySmall,
                        color = OledMutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 1: "Details" (Blue circular icons + Setup 2FA green pill)
        Text(
            text = "Details",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = OledMutedText
        )
        Spacer(modifier = Modifier.height(12.dp))

        SettingsCircleRow(
            icon = Icons.Default.Person,
            circleBrush = Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))),
            title = "Account Details ($userName • $favoriteClub)",
            onClick = onSwitchToProfile
        )
        Spacer(modifier = Modifier.height(14.dp))
        SettingsCircleRow(
            icon = Icons.Default.AccountBalanceWallet,
            circleBrush = Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))),
            title = "Streaming Servers (FrameTV & EpicSports)",
            onClick = onToggleExpandLeagues
        )
        Spacer(modifier = Modifier.height(14.dp))
        SettingsCircleRow(
            icon = Icons.Default.Lock,
            circleBrush = Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))),
            title = "Security",
            badgeText = if (isSignedIn) "2FA Active" else "Setup 2FA",
            onClick = {
                onCloseIfModal?.invoke()
                onOpenSecurityShield()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2: "App" (Amber circular icons + Green Toggle Switches matching IMG_8275)
        Text(
            text = "App",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = OledMutedText
        )
        Spacer(modifier = Modifier.height(12.dp))

        SettingsSwitchRow(
            icon = Icons.Default.Face,
            circleBrush = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
            title = "Universal Stream Display (All Devices)",
            subtitle = "FLAG_SECURE display block removed so live match streams are visible everywhere",
            checked = !flagSecureEnabled,
            onCheckedChange = { onToggleFlagSecure(false) }
        )
        Spacer(modifier = Modifier.height(14.dp))
        SettingsSwitchRow(
            icon = Icons.Default.Security,
            circleBrush = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
            title = "Smart Authentication & WAF",
            subtitle = "Skip biometric login within 15 sec & block SQLi/XSS attacks",
            checked = wafEnabled,
            onCheckedChange = onToggleWaf
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section 3: "General" (Purple/Magenta circular icons matching IMG_8275)
        Text(
            text = "General",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = OledMutedText
        )
        Spacer(modifier = Modifier.height(12.dp))

        SettingsCircleRow(
            icon = Icons.Default.CurrencyExchange,
            circleBrush = Brush.linearGradient(listOf(Color(0xFFE879F9), Color(0xFFA21CAF))),
            title = "Leagues Filter (${enabledLeagues.size}/8 Active)",
            onClick = onToggleExpandLeagues
        )

        AnimatedVisibility(visible = expandLeaguesPicker) {
            LeagueToggleCardContent(
                enabledLeagues = enabledLeagues,
                onToggleLeague = onToggleLeague,
                onEnableAllLeagues = onEnableAllLeagues
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SettingsCircleRow(
            icon = Icons.Default.Language,
            circleBrush = Brush.linearGradient(listOf(Color(0xFFE879F9), Color(0xFFA21CAF))),
            title = "Broadcast Voice ($selectedTtsVoice)",
            onClick = {
                val nextVoice = when (selectedTtsVoice) {
                    "Kore" -> "Puck"
                    "Puck" -> "Charon"
                    "Charon" -> "Fenrir"
                    else -> "Kore"
                }
                onSelectVoice(nextVoice)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingsCircleRow(
            icon = Icons.Default.WbSunny,
            circleBrush = Brush.linearGradient(listOf(Color(0xFFE879F9), Color(0xFFA21CAF))),
            title = "Appearance (#${selectedTheme.rank} ${selectedTheme.name})",
            onClick = onToggleExpandThemes
        )

        AnimatedVisibility(visible = expandThemesPicker) {
            Top20ThemesListContent(
                selectedTheme = selectedTheme,
                onSelectTheme = onSelectTheme
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OverviewStatTile(
    label: String,
    value: String,
    showStar: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OledCardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OledCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OledMutedText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showStar) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileActionRowCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        color = OledCardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, OledCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF23252B),
                shape = CircleShape,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = OledMutedText
            )
        }
    }
}

@Composable
private fun SettingsCircleRow(
    icon: ImageVector,
    circleBrush: Brush,
    title: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(circleBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        if (badgeText != null) {
            Surface(
                color = Color(0xFF063726),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = VerifiedPillGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = OledMutedText
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    circleBrush: Brush,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(circleBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OledMutedText,
                    lineHeight = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SwitchActiveGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2B2E36)
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeagueToggleCardContent(
    enabledLeagues: Set<FootballCompetition>,
    onToggleLeague: (FootballCompetition) -> Unit,
    onEnableAllLeagues: () -> Unit
) {
    Surface(
        color = OledCardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OledCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toggle Visible Match Leagues:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(
                    text = "Reset All",
                    style = MaterialTheme.typography.labelSmall,
                    color = VerifiedPillGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onEnableAllLeagues() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FootballCompetition.toggleableLeagues.forEach { league ->
                    val enabled = league in enabledLeagues
                    FilterChip(
                        selected = enabled,
                        onClick = { onToggleLeague(league) },
                        label = {
                            Text(
                                text = league.shortTag,
                                fontSize = 11.sp,
                                fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.22f),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.04f),
                            labelColor = OledMutedText
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun Top20ThemesListContent(
    selectedTheme: FootballThemeOption,
    onSelectTheme: (FootballThemeOption) -> Unit
) {
    Surface(
        color = OledCardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OledCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .testTag("top_20_themes_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SeedCatalog.top20AbstractThemes.forEach { themeItem ->
                val isSelected = themeItem.id == selectedTheme.id
                val itemAccent = Color(themeItem.accentHex)
                Surface(
                    color = if (isSelected) Color.White.copy(alpha = 0.10f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) VerifiedPillGreen else OledCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTheme(themeItem) }
                        .testTag("theme_option_${themeItem.rank}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(themeItem.bgTopHex), itemAccent)
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${themeItem.rank}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = themeItem.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = themeItem.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OledMutedText,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = VerifiedPillGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TouchTheGrassMenuDialog(
    initialShowSettings: Boolean = false,
    isSignedIn: Boolean,
    userName: String,
    userEmail: String?,
    favoriteClub: String,
    profilePictureId: String,
    customAvatarBitmap: Bitmap?,
    selectedTheme: FootballThemeOption,
    enabledLeagues: Set<FootballCompetition>,
    flagSecureEnabled: Boolean,
    wafEnabled: Boolean,
    selectedTtsVoice: String,
    liveMatchCount: Int,
    savedWatchlistCount: Int,
    securityScore: Int,
    onDismiss: () -> Unit,
    onGoogleAuthSuccess: (uid: String, displayName: String?, email: String?, photoUrl: String?) -> Unit,
    onGoogleSignedOut: () -> Unit,
    onAuthError: (String) -> Unit,
    onSaveProfile: (name: String, club: String, avatarId: String) -> Unit,
    onUploadCustomAvatar: (Bitmap) -> Unit,
    onSelectTheme: (FootballThemeOption) -> Unit,
    onToggleLeague: (FootballCompetition) -> Unit,
    onEnableAllLeagues: () -> Unit,
    onToggleFlagSecure: (Boolean) -> Unit,
    onToggleWaf: (Boolean) -> Unit,
    onSelectVoice: (String) -> Unit,
    onOpenSecurityShield: () -> Unit,
    onOpenAlertsAndWatchlist: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("upper_right_glass_menu"),
            color = OledBlack
        ) {
            TouchTheGrassProfileAndSettingsScreen(
                initialShowSettings = initialShowSettings,
                isSignedIn = isSignedIn,
                userName = userName,
                userEmail = userEmail,
                favoriteClub = favoriteClub,
                profilePictureId = profilePictureId,
                customAvatarBitmap = customAvatarBitmap,
                selectedTheme = selectedTheme,
                enabledLeagues = enabledLeagues,
                flagSecureEnabled = flagSecureEnabled,
                wafEnabled = wafEnabled,
                selectedTtsVoice = selectedTtsVoice,
                liveMatchCount = liveMatchCount,
                savedWatchlistCount = savedWatchlistCount,
                securityScore = securityScore,
                onGoogleAuthSuccess = onGoogleAuthSuccess,
                onGoogleSignedOut = onGoogleSignedOut,
                onAuthError = onAuthError,
                onSaveProfile = onSaveProfile,
                onUploadCustomAvatar = onUploadCustomAvatar,
                onSelectTheme = onSelectTheme,
                onToggleLeague = onToggleLeague,
                onEnableAllLeagues = onEnableAllLeagues,
                onToggleFlagSecure = onToggleFlagSecure,
                onToggleWaf = onToggleWaf,
                onSelectVoice = onSelectVoice,
                onOpenSecurityShield = onOpenSecurityShield,
                onOpenAlertsAndWatchlist = onOpenAlertsAndWatchlist,
                onCloseIfModal = onDismiss
            )
        }
    }
}
