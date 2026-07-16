@file:Suppress(
    "CyclomaticComplexMethod",
    "FunctionNaming",
    "LongMethod",
    "LongParameterList",
    "MagicNumber",
    "MaxLineLength",
    "TooManyFunctions",
)

package com.nextrank.feature.onboarding.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.component.GamerAccentLime
import com.nextrank.core.designsystem.component.GamerAccentOrange
import com.nextrank.core.designsystem.component.GamerAccentPink
import com.nextrank.core.designsystem.component.GamerChip
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerPrimaryButton
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val LAST_PAGE = 15
private const val TOTAL_PAGES = 16
private const val GENERATION_PAGE = 12
private const val GENERATION_DELAY_MILLIS = 2_200L

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentPage by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onComplete()
    }

    LaunchedEffect(currentPage) {
        if (currentPage == GENERATION_PAGE) {
            delay(GENERATION_DELAY_MILLIS)
            currentPage += 1
        }
    }

    GamerScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepHud(currentPage)
            OnboardingPage(
                page = currentPage,
                state = uiState,
                viewModel = viewModel,
            )
            uiState.errorMessage?.let { message ->
                GamerPanel(accent = MaterialTheme.colorScheme.error) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        GamerPrimaryButton(
            text = when {
                uiState.isSaving -> "Сохраняем профиль..."
                currentPage == GENERATION_PAGE -> "Генерируем..."
                currentPage == LAST_PAGE -> "Начать"
                currentPage == 0 -> "Начать"
                else -> "Далее"
            },
            onClick = {
                if (currentPage < LAST_PAGE) {
                    currentPage += 1
                } else {
                    viewModel.onComplete()
                }
            },
            enabled = !uiState.isSaving && currentPage != GENERATION_PAGE && canContinue(currentPage, uiState),
        )
        if (currentPage > 0) {
            GamerSecondaryButton(
                text = "Назад",
                onClick = { currentPage -= 1 },
                enabled = !uiState.isSaving,
            )
        }
    }
}

private fun canContinue(
    currentPage: Int,
    state: OnboardingUiState,
): Boolean = when (currentPage) {
    1 -> state.nickname.isNotBlank()
    10 -> state.faceitStatus != FaceitConnectStatus.CONNECTING
    else -> true
}

@Composable
private fun StepHud(currentPage: Int) {
    GamerPanel(accent = GamerAccentPink) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (currentPage == 0) "Старт" else "Шаг $currentPage из ${TOTAL_PAGES - 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            GamerChip(
                text = "${((currentPage + 1) * 100) / TOTAL_PAGES}% готово",
                accent = GamerAccentLime,
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    page: Int,
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
) {
    when (page) {
        0 -> WelcomeStep()
        1 -> NicknameStep(
            nickname = state.nickname,
            onNicknameChange = viewModel::onNicknameChange,
        )
        2 -> SingleChoiceStep(
            title = "Какая у тебя главная цель?",
            subtitle = "Выбери один фокус — под него соберём первый 14-дневный трек.",
            options = OnboardingGoal.entries,
            selected = state.goal,
            label = OnboardingGoal::label,
            onSelect = viewModel::onGoalSelect,
        )
        3 -> LevelStep(state, viewModel)
        4 -> MultiChoiceStep("Где играешь чаще?", "Можно выбрать несколько режимов.", PlayMode.entries, state.modes, PlayMode::label, viewModel::onModeToggle)
        5 -> MultiChoiceStep("Любимые карты", "Выбери до трёх карт — тренировки будут ближе к твоему пулу.", Cs2Map.entries, state.favoriteMaps, Cs2Map::label, viewModel::onMapToggle)
        6 -> SingleChoiceStep("Сколько времени есть на тренировку?", "Коротко и регулярно лучше, чем редко и героически.", TrainingDuration.entries, state.trainingDuration, TrainingDuration::label, viewModel::onTrainingDurationSelect)
        7 -> SingleChoiceStep("Как часто готов тренироваться?", "Подберём ритм без выгорания.", TrainingFrequency.entries, state.trainingFrequency, TrainingFrequency::label, viewModel::onTrainingFrequencySelect)
        8 -> MultiChoiceStep("Что кажется слабым местом?", "Выбери максимум три пункта.", WeakSpot.entries, state.weakSpots, WeakSpot::label, viewModel::onWeakSpotToggle)
        9 -> MultiChoiceStep("Чем уже пользуешься?", "Это поможет не советовать очевидное.", TrainingTool.entries, state.tools, TrainingTool::label, viewModel::onToolToggle)
        10 -> FaceitConnectStep(
            state = state,
            onNicknameChange = viewModel::onFaceitNicknameChange,
            onConnect = viewModel::connectFaceit,
            onSkip = { viewModel.onFaceitConnectChoice(false) },
        )
        11 -> SelfScoreStep(state.selfScores, viewModel::onSelfScoreChange)
        12 -> GenerationStep()
        13 -> ProfileSummaryStep(state)
        14 -> PlanPreviewStep(state)
        15 -> FirstTrainingStep(state)
    }
}

@Composable
private fun WelcomeStep() {
    GamerHeader(
        title = "Прокачай свой CS2 как в фитнес-приложении",
        subtitle = "Мы составим персональный план тренировок за 5 минут. Это не анкета — это первая диагностика.",
    )
    GamerPanel(accent = GamerAccentLime) {
        Text(
            text = "На выходе: профиль игрока, приоритеты, 14-дневный трек и первая тренировка на сегодня.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NicknameStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
) {
    GamerHeader(
        title = "Как к тебе обращаться?",
        subtitle = "Имя попадёт в профиль игрока и будет использоваться в персональных миссиях.",
    )
    GamerPanel(accent = GamerAccentLime) {
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("Никнейм") },
            placeholder = { Text("Например, Koresheff") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
            ),
        )
    }
}

@Composable
private fun LevelStep(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
) {
    GamerHeader(
        title = "Текущий уровень",
        subtitle = "Укажи Premier Rating, если играешь Premier. FACEIT level подтянем автоматически на следующем шаге.",
    )
    GamerPanel(accent = GamerAccentOrange) {
        OutlinedTextField(
            value = state.premierRating,
            onValueChange = viewModel::onPremierRatingChange,
            label = { Text("Premier Rating") },
            placeholder = { Text("Например, 17800") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
            ),
        )
    }
}

@Composable
private fun FaceitConnectStep(
    state: OnboardingUiState,
    onNicknameChange: (String) -> Unit,
    onConnect: () -> Unit,
    onSkip: () -> Unit,
) {
    GamerHeader(
        title = "Подключить FACEIT?",
        subtitle = "Найдём твой FACEIT-профиль и подтянем реальный level/ELO для персонального плана.",
    )
    GamerPanel(accent = GamerAccentLime) {
        OutlinedTextField(
            value = state.faceitNickname.ifBlank { state.nickname },
            onValueChange = onNicknameChange,
            label = { Text("FACEIT nickname") },
            placeholder = { Text("Например, ${state.nickname.ifBlank { "Koresheff" }}") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = state.faceitStatus != FaceitConnectStatus.CONNECTING,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
            ),
        )
        GamerPrimaryButton(
            text = if (state.faceitStatus == FaceitConnectStatus.CONNECTING) {
                "Ищем профиль..."
            } else {
                "Подключить FACEIT"
            },
            onClick = onConnect,
            enabled = state.faceitStatus != FaceitConnectStatus.CONNECTING,
        )
        when (state.faceitStatus) {
            FaceitConnectStatus.CONNECTING -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = GamerAccentLime,
            )
            FaceitConnectStatus.CONNECTED -> FaceitConnectedPanel(state.faceitPlayer)
            FaceitConnectStatus.ERROR -> Text(
                text = state.faceitError ?: "Не удалось найти FACEIT-профиль",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            FaceitConnectStatus.SKIPPED -> Text(
                text = "Ок, пропустим FACEIT на старте. Можно будет подключить позже.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            FaceitConnectStatus.IDLE -> Text(
                text = "Если nickname совпадает с FACEIT — просто нажми подключить.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    ChoiceCard("Позже", state.faceitStatus == FaceitConnectStatus.SKIPPED, GamerAccentOrange, onSkip)
}

@Composable
private fun FaceitConnectedPanel(player: ConnectedFaceitPlayer?) {
    if (player == null) return
    GamerPanel(accent = GamerAccentLime) {
        SummaryLine("FACEIT", player.nickname)
        SummaryLine("Level", player.skillLevel?.toString() ?: "Не указан")
        SummaryLine("ELO", player.faceitElo?.toString() ?: "Не указан")
        SummaryLine("Game", player.game.uppercase())
    }
}

@Composable
private fun SelfScoreStep(
    scores: Map<SelfScoreCategory, Int>,
    onChange: (SelfScoreCategory, Int) -> Unit,
) {
    GamerHeader(
        title = "Самооценка",
        subtitle = "Оцени навыки по шкале 1–10. Это не экзамен, а калибровка нагрузки.",
    )
    SelfScoreCategory.entries.forEach { category ->
        GamerPanel {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(category.label, fontWeight = FontWeight.Bold)
                GamerChip(text = "${scores[category] ?: 5}/10", accent = GamerAccentLime)
            }
            Slider(
                value = (scores[category] ?: 5).toFloat(),
                onValueChange = { onChange(category, it.roundToInt()) },
                valueRange = 1f..10f,
                steps = 8,
            )
        }
    }
}

@Composable
private fun GenerationStep() {
    GamerHeader(
        title = "Генерируем профиль",
        subtitle = "Анализируем ответы, собираем архетип игрока и первый 14-дневный трек.",
    )
    GamerPanel(accent = GamerAccentPink) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CircularProgressIndicator(color = GamerAccentLime)
            Text(
                text = "Синхронизируем цель, уровень, карты и слабые места...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            GamerChip(text = "AI-трек почти готов", accent = GamerAccentLime)
        }
    }
}

@Composable
private fun ProfileSummaryStep(state: OnboardingUiState) {
    GamerHeader(
        title = "Профиль игрока",
        subtitle = playerArchetype(state),
    )
    SummaryRow("Цель", state.goal?.label ?: "Не выбрана")
    SummaryRow("FACEIT", faceitSummary(state))
    SummaryRow("Premier", state.premierRating.ifBlank { "Не указан" })
    SummaryRow("Приоритеты", state.weakSpots.joinToStringLabel(WeakSpot::label).ifBlank { "Будут уточнены первой тренировкой" })
    SummaryRow("Тренировки", "${state.trainingDuration.label}, ${state.trainingFrequency.label.lowercase()}")
}

@Composable
private fun PlanPreviewStep(state: OnboardingUiState) {
    GamerHeader(
        title = "Первые три дня",
        subtitle = "План адаптируется после каждой завершённой тренировки.",
    )
    val map = state.favoriteMaps.firstOrNull()?.label ?: "Mirage"
    SummaryRow("Сегодня", "${primaryWeakSpot(state)}, Aim, $map")
    SummaryRow("Завтра", "Spray, Utility")
    SummaryRow("Послезавтра", "Positioning, ${state.favoriteMaps.drop(1).firstOrNull()?.label ?: "Dust II"}")
}

@Composable
private fun FirstTrainingStep(state: OnboardingUiState) {
    GamerHeader(
        title = "Первая тренировка",
        subtitle = "Первая тренировка занимает всего ${firstTrainingMinutes(state)} минут.",
    )
    GamerPanel(accent = GamerAccentLime) {
        Text(
            text = "Начнём с короткой миссии: разогрев, главный слабый навык и один сценарий на любимой карте.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun <T> SingleChoiceStep(
    title: String,
    subtitle: String,
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    showHeader: Boolean = true,
) {
    if (showHeader) {
        GamerHeader(title = title, subtitle = subtitle)
    }
    options.forEach { option ->
        ChoiceCard(label(option), selected == option, GamerAccentLime) { onSelect(option) }
    }
}

@Composable
private fun <T> MultiChoiceStep(
    title: String,
    subtitle: String,
    options: List<T>,
    selected: Set<T>,
    label: (T) -> String,
    onToggle: (T) -> Unit,
) {
    GamerHeader(title = title, subtitle = subtitle)
    options.forEach { option ->
        ChoiceCard(label(option), option in selected, GamerAccentPink) { onToggle(option) }
    }
}

@Composable
private fun ChoiceCard(
    text: String,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    GamerPanel(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick),
        accent = if (selected) accent else MaterialTheme.colorScheme.outline,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (selected) GamerChip(text = "выбрано", accent = accent)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    GamerPanel {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun playerArchetype(state: OnboardingUiState): String = when {
    WeakSpot.UTILITY in state.weakSpots -> "Сильный стрелок, которому нужно усилить Utility."
    WeakSpot.MOVEMENT in state.weakSpots -> "Хороший aim, но movement забирает лишние дуэли."
    WeakSpot.POSITIONING in state.weakSpots -> "Механика есть — прокачаем позиционирование."
    WeakSpot.AIM in state.weakSpots -> "Entry Fragger с потенциалом, которому нужна стабильная стрельба."
    else -> "Собираем универсальный 14-дневный трек под твой ритм."
}

private fun primaryWeakSpot(state: OnboardingUiState): String =
    state.weakSpots.firstOrNull()?.label ?: "Movement"

private fun faceitSummary(state: OnboardingUiState): String =
    state.faceitPlayer?.let { player ->
        "Level ${player.skillLevel ?: "?"}, ELO ${player.faceitElo ?: "?"}"
    } ?: when (state.faceitStatus) {
        FaceitConnectStatus.SKIPPED -> "Не подключён"
        else -> "Будет подтянут из FACEIT API"
    }

private fun firstTrainingMinutes(state: OnboardingUiState): Int =
    (state.trainingDuration.minutes * 0.6f).roundToInt().coerceAtLeast(18)

private fun <T> Iterable<T>.joinToStringLabel(label: (T) -> String): String =
    joinToString(separator = ", ", transform = label)
