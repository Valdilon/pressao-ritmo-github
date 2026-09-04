package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.domain.model.UserProfile
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant

@Composable
fun ProfileSwitcherHeader(
    activeProfile: UserProfile?,
    profileCount: Int,
    activeRemindersCount: Int,
    onOpenProfileManager: () -> Unit,
    onOpenReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColor = try {
        if (activeProfile?.avatarColorHex != null) {
            Color(android.graphics.Color.parseColor(activeProfile.avatarColorHex))
        } else {
            SleekPrimary
        }
    } catch (_: Exception) {
        SleekPrimary
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_switcher_header"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = SleekSurfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile selector button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenProfileManager)
                    .padding(4.dp)
                    .testTag("switch_profile_badge"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeProfile?.photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(activeProfile.photoUri),
                            contentDescription = "Foto do Perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = activeProfile?.initials ?: "P",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeProfile?.fullName ?: "Selecionar Perfil",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Trocar perfil",
                            tint = SleekPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (profileCount > 1) "$profileCount perfis disponíveis" else "Perfil ativo",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekPrimary,
                        fontSize = 11.sp
                    )
                }
            }

            // Reminders quick action button
            Surface(
                onClick = onOpenReminders,
                shape = RoundedCornerShape(14.dp),
                color = SleekPrimaryContainer.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.testTag("header_reminders_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Lembretes",
                        tint = SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeRemindersCount > 0) "$activeRemindersCount ativo(s)" else "Lembretes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                }
            }
        }
    }
}
