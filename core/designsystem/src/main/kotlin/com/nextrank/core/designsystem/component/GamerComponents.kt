package com.nextrank.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.theme.CombatOrange
import com.nextrank.core.designsystem.theme.HudLine
import com.nextrank.core.designsystem.theme.NeonLime
import com.nextrank.core.designsystem.theme.NeonPink
import com.nextrank.core.designsystem.theme.PanelDark
import kotlin.math.cos
import kotlin.math.sin

private val PanelShape = RoundedCornerShape(8.dp)
private val ButtonMinHeight = 52.dp

@Composable
fun GamerScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF061018),
                        Color(0xFF0B1520),
                        Color(0xFF081019),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                        radius = 760f,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
fun GamerHeader(
    eyebrow: String? = null,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        eyebrow?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NeonLime,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun GamerPanel(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.35f), PanelShape),
        shape = PanelShape,
        colors = CardDefaults.cardColors(containerColor = PanelDark.copy(alpha = 0.92f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.14f), Color.Transparent),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

@Composable
fun GamerPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonMinHeight),
        enabled = enabled,
        shape = PanelShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = HudLine,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GamerSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonMinHeight),
        enabled = enabled,
        shape = PanelShape,
        border = BorderStroke(1.dp, HudLine),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GamerStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    GamerPanel(modifier = modifier, accent = accent) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = accent,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun GamerCircularProgress(
    title: String,
    primaryValue: String,
    subtitle: String,
    progress: Float,
    badgeLabel: String,
    badgeValue: String,
    modifier: Modifier = Modifier,
    accent: Color = NeonLime,
    secondaryAccent: Color = NeonPink,
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    GamerPanel(modifier = modifier, accent = accent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(286.dp)) {
                val strokeWidth = 26.dp.toPx()
                val inset = strokeWidth / 2f + 10.dp.toPx()
                val arcSize = size.copy(
                    width = size.width - inset * 2f,
                    height = size.height - inset * 2f,
                )
                val startAngle = 132f
                val maxSweep = 276f

                drawArc(
                    color = HudLine.copy(alpha = 0.55f),
                    startAngle = startAngle,
                    sweepAngle = maxSweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        0.00f to accent,
                        0.55f to Color(0xFF00D9FF),
                        0.82f to secondaryAccent,
                        1.00f to accent,
                    ),
                    startAngle = startAngle,
                    sweepAngle = maxSweep * safeProgress,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                val center = this.center
                val radius = size.minDimension / 2f - 58.dp.toPx()
                repeat(40) { index ->
                    val fraction = index / 39f
                    val angle = Math.toRadians((startAngle + maxSweep * fraction).toDouble())
                    val dotCenter = androidx.compose.ui.geometry.Offset(
                        x = center.x + cos(angle).toFloat() * radius,
                        y = center.y + sin(angle).toFloat() * radius,
                    )
                    val isActive = fraction <= safeProgress
                    drawCircle(
                        color = if (isActive) accent.copy(alpha = 0.86f) else Color.White.copy(alpha = 0.18f),
                        radius = if (isActive) 3.2.dp.toPx() else 2.2.dp.toPx(),
                        center = dotCenter,
                    )
                }

                val badgeAngle = Math.toRadians((startAngle + maxSweep * safeProgress).toDouble())
                val badgeRadius = size.minDimension / 2f - 30.dp.toPx()
                val badgeCenter = androidx.compose.ui.geometry.Offset(
                    x = center.x + cos(badgeAngle).toFloat() * badgeRadius,
                    y = center.y + sin(badgeAngle).toFloat() * badgeRadius,
                )
                drawCircle(
                    color = PanelDark,
                    radius = 30.dp.toPx(),
                    center = badgeCenter,
                )
                drawCircle(
                    color = accent.copy(alpha = 0.34f),
                    radius = 30.dp.toPx(),
                    center = badgeCenter,
                    style = Stroke(width = 3.dp.toPx()),
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 42.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent,
                    textAlign = TextAlign.Center,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 36.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(PanelDark)
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = badgeValue,
                        style = MaterialTheme.typography.titleLarge,
                        color = accent,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

@Composable
fun GamerChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = CombatOrange,
) {
    Box(
        modifier = modifier
            .clip(PanelShape)
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.45f), PanelShape)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun GamerStatRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

val GamerAccentPink: Color = NeonPink
val GamerAccentLime: Color = NeonLime
val GamerAccentOrange: Color = CombatOrange
