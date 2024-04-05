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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import me.rmyhal.contentment.ContentLoadingIndicatorContainer
import kotlin.math.roundToLong


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Content(
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

@Composable
fun Content(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Demo()

  }
}

@Composable
private fun Demo() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    var minShowTimeMillis by remember { mutableLongStateOf(500L) }
    var delayMillis by remember { mutableLongStateOf(500L) }
    var autoHideMillis by remember { mutableLongStateOf(700L) }
    var autoHideEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Text(text = "ContentLoadingIndicatorContainer")
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
      defaultValue = 7f,
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
    ) {
      ContentLoadingIndicatorContainer(
        loading = isLoading,
        modifier = Modifier.align(Alignment.Center),
        minShowTimeMillis = minShowTimeMillis,
        delayMillis = delayMillis,
      ) {
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
          LaunchedEffect(key1 = isLoading) {
            while (true) {
              delay(100L)
              displaying += 100L
            }
          }
          Text(text = "$displaying")
        }
      }
    }
    Button(
      modifier = Modifier.align(Alignment.CenterHorizontally),
      onClick = {
        isLoading = !isLoading
        if (isLoading && autoHideEnabled) {
          scope.launch {
            delay(autoHideMillis)
            isLoading = false
          }
        }
      },
    ) {
      val text = if (isLoading) "Hide" else "Show"
      Text(text = text)
    }
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