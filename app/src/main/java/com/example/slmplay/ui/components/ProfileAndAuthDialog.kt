package com.example.slmplay.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.slmplay.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun ProfileAndAuthDialog(
    isOpen: Boolean,
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onLogin: (username: String, emailOrId: String) -> Unit,
    onRegister: (username: String, emailOrId: String) -> Unit,
    onUpdateProfile: (username: String, avatarUri: String?) -> Unit,
    onExportCloudData: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    if (!isOpen) return

    var isRegisterMode by remember { mutableStateOf(false) }
    var inputUsername by remember(userProfile.username) { mutableStateOf(userProfile.username) }
    var inputEmail by remember(userProfile.emailOrId) { mutableStateOf(userProfile.emailOrId) }
    var inputPassword by remember { mutableStateOf("") }
    var currentAvatarUri by remember(userProfile.avatarUri) { mutableStateOf(userProfile.avatarUri) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentAvatarUri = uri.toString()
            onUpdateProfile(inputUsername, uri.toString())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(32.dp))
                .background(DarkCanvas)
                .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppleCrimson.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (userProfile.isLoggedIn) Icons.Default.Person else Icons.Default.Lock,
                                contentDescription = null,
                                tint = AppleCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (userProfile.isLoggedIn) "Profil SLM Play" else if (isRegisterMode) "Créer un profil" else "Connexion",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkGlassCard)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (userProfile.isLoggedIn) {
                    // ================= LOGGED IN PROFILE VIEW =================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar View with edit badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(16.dp, CircleShape, spotColor = AppleCrimson.copy(alpha = 0.5f))
                                    .clip(CircleShape)
                                    .border(2.5.dp, Brush.linearGradient(listOf(AppleCrimson, ApplePurple)), CircleShape)
                                    .background(Color.Black)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                if (currentAvatarUri != null) {
                                    AsyncImage(
                                        model = currentAvatarUri,
                                        contentDescription = "Avatar de profil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.slm_logo),
                                        contentDescription = "Avatar par défaut SLM Play",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Edit camera icon badge
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(AppleCrimson)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Changer la photo", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username (Editable in real-time)
                        if (isEditingName) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputUsername,
                                    onValueChange = { inputUsername = it },
                                    label = { Text("Nom d'utilisateur", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkGlassCard,
                                        unfocusedContainerColor = DarkGlassCard,
                                        focusedBorderColor = AppleCrimson,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (inputUsername.isNotBlank()) {
                                            onUpdateProfile(inputUsername.trim(), currentAvatarUri)
                                            isEditingName = false
                                        }
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppleCrimson)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Valider", tint = Color.White)
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = userProfile.username,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        inputUsername = userProfile.username
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier le nom", tint = AppleCrimson, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Text(
                            text = if (userProfile.emailOrId.isNotBlank()) userProfile.emailOrId else "Compte SLM Studio connecté",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        // ⚠️ Discret Credit Notice
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Créé par SLM et ChatGPT",
                            fontSize = 9.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats & Info Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = DarkGlassElevated
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Statut", fontSize = 11.sp, color = TextSecondary)
                                    Text("Membre Pro", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppleCrimson)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(GlassBorder))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Studio", fontSize = 11.sp, color = TextSecondary)
                                    Text("Apple Glass", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(GlassBorder))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ambiance", fontSize = 11.sp, color = TextSecondary)
                                    Text("HOpE 3D", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApplePurple)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Restricted Action 1: Export Cloud data
                        GlassButton(
                            onClick = onExportCloudData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_cloud_data"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ApplePurple, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Sauvegarde & Export Cloud", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Exporter playlists, favoris, réglages et métadonnées", fontSize = 11.sp, color = TextSecondary)
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Restricted Action 2: Logout
                        GlassButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_logout_profile"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF9500), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Se déconnecter", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Restricted Action 3: Delete Account
                        GlassButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_delete_account"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AppleCrimson, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Supprimer le compte", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppleCrimson)
                            }
                        }
                    }
                } else {
                    // ================= AUTH / LOGIN / REGISTER VIEW =================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Official SLM Logo
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = AppleCrimson.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.slm_logo),
                                contentDescription = "Logo SLM Play",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isRegisterMode) "Rejoignez l'univers SLM" else "Bienvenue sur SLM Play",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Connectez-vous pour synchroniser vos créations et playlists",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // ⚠️ Discret Credit Notice
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Créé par SLM et ChatGPT",
                            fontSize = 9.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tab Toggle (Connexion / Inscription)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkGlassCard)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isRegisterMode) AppleCrimson else Color.Transparent)
                                    .clickable { isRegisterMode = false }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Connexion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (!isRegisterMode) Color.White else TextSecondary)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRegisterMode) AppleCrimson else Color.Transparent)
                                    .clickable { isRegisterMode = true }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Créer un profil", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isRegisterMode) Color.White else TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Inputs
                        OutlinedTextField(
                            value = inputUsername,
                            onValueChange = { inputUsername = it },
                            label = { Text("Nom d'utilisateur", color = TextSecondary, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkGlassCard,
                                unfocusedContainerColor = DarkGlassCard,
                                focusedBorderColor = AppleCrimson,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_username_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("E-mail ou Identifiant", color = TextSecondary, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkGlassCard,
                                unfocusedContainerColor = DarkGlassCard,
                                focusedBorderColor = AppleCrimson,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            label = { Text("Mot de passe", color = TextSecondary, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkGlassCard,
                                unfocusedContainerColor = DarkGlassCard,
                                focusedBorderColor = AppleCrimson,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        GlassButton(
                            onClick = {
                                val name = if (inputUsername.isNotBlank()) inputUsername.trim() else "Artiste SLM"
                                val email = if (inputEmail.isNotBlank()) inputEmail.trim() else "artiste@slmplay.app"
                                if (isRegisterMode) {
                                    onRegister(name, email)
                                } else {
                                    onLogin(name, email)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("auth_submit_btn"),
                            shape = RoundedCornerShape(16.dp),
                            isPrimary = true
                        ) {
                            Text(
                                text = if (isRegisterMode) "Créer mon profil SLM" else "Se connecter",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Account Deletion Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text("Supprimer le compte ?", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Cette action supprimera votre profil et vos préférences de compte. Vos fichiers musicaux locaux ne seront PAS supprimés.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Supprimer définitivement", color = AppleCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler", color = TextPrimary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
