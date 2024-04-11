package me.rmyhal.contentment.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rmyhal.contentment.ContentLoadingIndicator
import me.rmyhal.contentment.Contentment
import me.rmyhal.contentment.sample.DemoComponent.ContentWithIndicator
import me.rmyhal.contentment.sample.DemoComponent.IndicatorOnly
import kotlin.math.roundToLong

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
    var uiState by remember { mutableStateOf<UiState>(UiState.Loading) }
    val loadedState by remember { mutableStateOf(UiState.Loaded("Your fancy content")) }
    val scope = rememberCoroutineScope()

    // single-shot auto-hide
    LaunchedEffect(key1 = Unit) {
      delay(autoHideMillis)
      uiState = loadedState
    }

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
            delay(autoHideMillis)
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
  Row(
    modifier = modifier
      .fillMaxWidth()
      .selectableGroup(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        delay(75)
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
}

sealed interface UiState {
  data class Loaded(val data: String) : UiState
  data object Loading : UiState

  fun isLoading() = this is Loading
}