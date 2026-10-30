package com.arbdev.angel.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AngelCompactDock(
    currentTab: AngelTab,
    onTabSelected: (AngelTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(29.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AngelTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "iconColor"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = iconColor
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Micro dot indicator
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                                )
                        )
                    }
                }
            }
        }
    }
}
