package com.example.tracker_gyro.features.ambient

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker_gyro.core.ui.components.GlassCard
import com.example.tracker_gyro.core.ui.theme.DarkCanvas
import com.example.tracker_gyro.core.ui.theme.DeepSlate
import com.example.tracker_gyro.core.ui.theme.FrostedSilver
import com.example.tracker_gyro.core.ui.theme.LightCanvas
import com.example.tracker_gyro.core.ui.theme.SubtleEmerald

@Composable
fun AmbientScreen(viewModel: AmbientViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startTracking()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopTracking()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopTracking()
        }
    }

    val lux by viewModel.lux.collectAsStateWithLifecycle()
    val autoMode by viewModel.autoMode.collectAsStateWithLifecycle()
    val torchState by viewModel.torchState.collectAsStateWithLifecycle()

    val targetBgColor = if (lux > 50f) LightCanvas else DarkCanvas
    val bgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 1000),
        label = "BgColorAnim"
    )

    val textColor = if (lux > 50f) DeepSlate else FrostedSilver
    val darkTheme = lux <= 50f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            darkTheme = darkTheme
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${lux.toInt()} Lux",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ambient Light",
                    fontSize = 16.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            darkTheme = darkTheme
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto Flashlight",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Switch(
                        checked = autoMode,
                        onCheckedChange = viewModel::toggleAutoMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LightCanvas,
                            checkedTrackColor = SubtleEmerald
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manual Override",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Switch(
                        checked = torchState,
                        onCheckedChange = viewModel::setManualTorch,
                        enabled = !autoMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LightCanvas,
                            checkedTrackColor = SubtleEmerald
                        )
                    )
                }
            }
        }
    }
}
