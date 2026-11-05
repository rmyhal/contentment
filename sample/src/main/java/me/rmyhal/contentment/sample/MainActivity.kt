package me.rmyhal.contentment.sample

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rmyhal.contentment.ContentLoadingIndicator
import me.rmyhal.contentment.Contentment
import me.rmyhal.contentment.rememberIndicatorVisible
import me.rmyhal.contentment.sample.DemoComponent.AnimatedIndicator
import me.rmyhal.contentment.sample.DemoComponent.ContentWithIndicator
import me.rmyhal.contentment.sample.DemoComponent.IndicatorOnly
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            contentAlignment = Alignment.Center,
          ) {
            DemoContent()
          }
        }
      }
    }
  }
}

@Composable
private fun DemoContent() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    var demoComponent by remember { mutableStateOf<DemoComponent>(ContentWithIndicator) }
    var minShowTimeMillis by remember { mutableLongStateOf(500L) }
    var delayMillis by remember { mutableLongStateOf(500L) }
    var autoHideMillis by remember { mutableLongStateOf(1000L) }
    var autoHideEnabled by remember { mutableStateOf(true) }
    var animationMillis by remember { mutableLongStateOf(300L) }
    val loadedState by remember { mutableStateOf(UiState.Loaded("Your fancy content")) }
    var uiState by remember { mutableStateOf<UiState>(loadedState) }
    val scope = rememberCoroutineScope()

    Text(text = "Contentment Demo")
    DemoComponentSelector(
      modifier = Modifier.padding(top = 12.dp),
      selectedComponent = demoComponent,
      onSelect = { demoComponent = it }
    )
    Spacer(modifier = Modifier.height(16.dp))
    DemoSlider(
      defaultValue = 5f,
      label = { Text("Min show time:") },
      onValueChanged = { minShowTimeMillis = it },
    )
    DemoSlider(
      defaultValue = 5f,
      label = { Text("Delay:") },
      onValueChanged = { delayMillis = it },
    )
    DemoSlider(
      defaultValue = 10f,
      label = {
        Checkbox(
          checked = autoHideEnabled,
          onCheckedChange = { autoHideEnabled = !autoHideEnabled },
        )
        Text(text = "Auto hide after:")
      },
      enabled = autoHideEnabled,
      onValueChanged = { autoHideMillis = it },
    )
    DemoSlider(
      defaultValue = 3f,
      label = { Text("Animation duration:") },
      enabled = demoComponent == AnimatedIndicator,
      onValueChanged = { animationMillis = it },
    )
    LoadingTimeline(
      modifier = Modifier.padding(top = 8.dp),
      loading = uiState.isLoading(),
      minShowTimeMillis = minShowTimeMillis,
      delayMillis = delayMillis,
    )
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      when (demoComponent) {
        is IndicatorOnly -> {
          ContentLoadingIndicator(
            loading = uiState.isLoading()
          ) {
            IndicatorContent()
          }
        }

        is AnimatedIndicator -> {
          AnimatedIndicatorDemo(
            loading = uiState.isLoading(),
            minShowTimeMillis = minShowTimeMillis,
            delayMillis = delayMillis,
            animationMillis = animationMillis,
          )
        }

        is ContentWithIndicator -> {
          Contentment(
            minShowTimeMillis = minShowTimeMillis,
            delayMillis = delayMillis,
          ) {
            when (val state = uiState) {
              is UiState.Loading -> indicator { IndicatorContent() }
              is UiState.Loaded -> content { Text(text = state.data) }
            }
          }
        }
      }
    }
    Button(
      modifier = Modifier.align(Alignment.CenterHorizontally),
      onClick = {
        uiState = if (uiState.isLoading()) loadedState else UiState.Loading
        if (uiState.isLoading() && autoHideEnabled) {
          scope.launch {
            delay(autoHideMillis.milliseconds)
            uiState = loadedState
          }
        }
      },
    ) {
      val text = if (uiState.isLoading()) "Stop" else "Start"
      Text(text = text)
    }
  }
}

@Composable
fun DemoComponentSelector(
  modifier: Modifier = Modifier,
  selectedComponent: DemoComponent,
  onSelect: (DemoComponent) -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .selectableGroup(),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    DemoComponentRow(
      selected = selectedComponent == ContentWithIndicator,
      component = ContentWithIndicator,
      onSelect = onSelect,
    )
    DemoComponentRow(
      selected = selectedComponent == IndicatorOnly,
      component = IndicatorOnly,
      onSelect = onSelect,
    )
    DemoComponentRow(
      selected = selectedComponent == AnimatedIndicator,
      component = AnimatedIndicator,
      onSelect = onSelect,
    )
  }
}

@Composable
fun DemoComponentRow(
  selected: Boolean,
  component: DemoComponent,
  onSelect: (DemoComponent) -> Unit,
) {
  Row(
    modifier = Modifier
      .selectable(
        selected = selected,
        onClick = { onSelect(component) },
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      selected = selected,
      onClick = null,
    )
    Text(text = component.toString())
  }
}

@Composable
private fun AnimatedIndicatorDemo(
  loading: Boolean,
  minShowTimeMillis: Long,
  delayMillis: Long,
  animationMillis: Long,
) {
  val animationSpec = tween<Float>(durationMillis = animationMillis.toInt())
  AnimatedVisibility(
    visible = rememberIndicatorVisible(
      loading = loading,
      minShowTimeMillis = minShowTimeMillis,
      delayMillis = delayMillis,
    ),
    enter = fadeIn(animationSpec) + scaleIn(animationSpec),
    exit = fadeOut(animationSpec) + scaleOut(animationSpec),
  ) {
    IndicatorContent()
  }
}

/**
 * Time since loading last started. Stops when loading has ended and the indicator is hidden.
 */
@Composable
private fun LoadingTimeline(
  loading: Boolean,
  minShowTimeMillis: Long,
  delayMillis: Long,
  modifier: Modifier = Modifier,
) {
  val indicatorVisible = rememberIndicatorVisible(
    loading = loading,
    minShowTimeMillis = minShowTimeMillis,
    delayMillis = delayMillis,
  )
  // null until loading starts for the first time.
  var timeline by remember { mutableStateOf<Timeline?>(null) }
  var nowMillis by remember { mutableLongStateOf(0L) }

  LaunchedEffect(loading) {
    val timeMillis = SystemClock.elapsedRealtime()
    timeline = if (loading) Timeline(startedAtMillis = timeMillis) else timeline?.copy(loadedAtMillis = timeMillis)
  }
  LaunchedEffect(indicatorVisible) {
    val timeMillis = SystemClock.elapsedRealtime()
    timeline = timeline?.let {
      when {
        indicatorVisible -> it.copy(indicatorShownAtMillis = timeMillis, indicatorHiddenAtMillis = null)
        it.indicatorShownAtMillis != null -> it.copy(indicatorHiddenAtMillis = timeMillis)
        else -> it
      }
    }
  }

  val finished = timeline == null || (!loading && !indicatorVisible)
  LaunchedEffect(finished) {
    if (finished) {
      nowMillis = SystemClock.elapsedRealtime()
      return@LaunchedEffect
    }
    while (true) {
      withFrameMillis { nowMillis = SystemClock.elapsedRealtime() }
    }
  }

  Column(modifier = modifier) {
    Text(
      text = "Timer: ${timeline?.elapsed(nowMillis) ?: 0L}ms",
      style = MaterialTheme.typography.titleMedium,
    )
    val current = timeline
    if (current == null) {
      Text(text = "Not started")
    } else {
      Text(text = "loading = true: ${current.loadingRange()}")
      Text(text = "Indicator visible: ${current.indicatorRange()}")
    }
  }
}

private data class Timeline(
  val startedAtMillis: Long,
  val loadedAtMillis: Long? = null,
  val indicatorShownAtMillis: Long? = null,
  val indicatorHiddenAtMillis: Long? = null,
) {
  fun elapsed(nowMillis: Long): Long = nowMillis - startedAtMillis

  fun loadingRange(): String = range(startedAtMillis, loadedAtMillis)

  fun indicatorRange(): String = when {
    indicatorShownAtMillis != null -> range(indicatorShownAtMillis, indicatorHiddenAtMillis)
    loadedAtMillis != null -> "not shown"
    else -> "waiting for delay"
  }

  private fun range(fromMillis: Long, toMillis: Long?): String =
    "${elapsed(fromMillis)}ms → ${toMillis?.let { "${elapsed(it)}ms" } ?: "…"}"
}

@Composable
private fun IndicatorContent() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    CircularProgressIndicator(
      modifier = Modifier
        .size(50.dp)
        .align(Alignment.End),
    )

    var displaying by remember { mutableLongStateOf(0L) }
    LaunchedEffect(key1 = Unit) {
      while (true) {
        delay(75.milliseconds)
        displaying += 75L
      }
    }
    Text(text = "$displaying")
  }
}

@Composable
private fun DemoSlider(
  defaultValue: Float,
  label: @Composable () -> Unit,
  enabled: Boolean = true,
  onValueChanged: (Long) -> Unit,
) {
  var sliderPosition by remember { mutableFloatStateOf(defaultValue) }
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    label()
    Text(text = "${(sliderPosition.roundToLong() * 100)}ms")
  }
  Slider(
    value = sliderPosition,
    enabled = enabled,
    steps = 14,
    valueRange = 0f..15f,
    onValueChange = { sliderPosition = it },
    onValueChangeFinished = { onValueChanged(sliderPosition.roundToLong() * 100L) }
  )
}

sealed interface DemoComponent {
  data object IndicatorOnly : DemoComponent
  data object ContentWithIndicator : DemoComponent
  data object AnimatedIndicator : DemoComponent
}

sealed interface UiState {
  data class Loaded(val data: String) : UiState
  data object Loading : UiState

  fun isLoading() = this is Loading
}