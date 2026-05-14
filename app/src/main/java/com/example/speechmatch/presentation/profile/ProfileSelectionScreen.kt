package com.example.speechmatch.presentation.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.speechmatch.data.local.entity.UserProfileEntity

/** * "Kim Çalışıyor/İzliyor?" (Çoklu Profil Seçim) Ekranı.
 * @param onProfileSelected Kullanıcı bir profile tıkladığında o profilin ID'siyle navigasyonu tetikler.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: ProfileSelectionViewModel = hiltViewModel(),
    onProfileSelected: (UserProfileEntity) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // --- Dinamik Tema ve Renk Paleti ---
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = if (isDark) Color(0xFFD0FF9A) else Color(0xFF2563EB)
    val errorColor = Color(0xFFEF4444) // Silme butonu için kırmızı renk

    // Diyalogları kontrol eden state'ler
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<UserProfileEntity?>(null) } // Silinecek profili tutar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Kim Çalışıyor?",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = primaryTextColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Profil seçin, basılı tutarak silin veya yeni ekleyin.",
            fontSize = 14.sp,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (state.isLoading) {
            CircularProgressIndicator(color = accentColor)
        } else {
            // Profilleri ve "Ekle" butonunu ızgara şeklinde listeliyoruz
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Kayıtlı Profiller
                items(state.profiles) { profile ->
                    ProfileItem(
                        profile = profile,
                        accentColor = accentColor,
                        textColor = primaryTextColor,
                        onSelect = { onProfileSelected(profile) },
                        onLongSelect = { profileToDelete = profile } // Basılı tutulunca silme state'ine atar
                    )
                }

                // 2. Yeni Profil Ekle (+) Butonu
                item {
                    AddProfileItem(
                        textColor = primaryTextColor,
                        onClick = { showAddProfileDialog = true }
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                color = errorColor,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }

    // --- 1. YENİ PROFİL EKLEME DİYALOĞU (Görünürlüğü Artırılmış) ---
    if (showAddProfileDialog) {
        var newName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = {
                Text(
                    text = "Yeni Profil Ekle",
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryTextColor,
                    fontSize = 22.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Lütfen isminizi yazın:",
                        color = secondaryTextColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Profil İsmi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = secondaryTextColor.copy(alpha = 0.5f),
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor,
                            focusedContainerColor = cardBgColor,
                            unfocusedContainerColor = cardBgColor,
                            focusedTextColor = primaryTextColor,
                            unfocusedTextColor = primaryTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.createNewProfile(newName)
                            showAddProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Oluştur", color = if (isDark) Color(0xFF0F172A) else Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) {
                    Text("İptal", color = secondaryTextColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // --- 2. PROFİL SİLME ONAY DİYALOĞU  ---
    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = {
                Text(
                    text = "Profili Sil",
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryTextColor,
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    text = "'${profile.profileName}' isimli profili ve tüm öğrenme geçmişini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.",
                    color = secondaryTextColor,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProfile(profile)
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Evet, Sil", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("İptal", color = secondaryTextColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

/** Tek bir kullanıcının Avatarını çizen bileşen. (Basılı tutma eklendi) */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileItem(
    profile: UserProfileEntity,
    accentColor: Color,
    textColor: Color,
    onSelect: () -> Unit,
    onLongSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(
            onClick = onSelect,
            onLongClick = onLongSelect // Basılı tutunca çalışır
        )
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profil: ${profile.profileName}",
                tint = accentColor,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.profileName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Seviye: ${profile.currentLevel}",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

/** "Yeni Ekle" Avatarını çizen bileşen. */
@Composable
fun AddProfileItem(
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // Ekle butonunda basılı tutmaya gerek yok, sadece tıklama
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Yeni Profil Ekle",
                tint = textColor,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Yeni Ekle",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}