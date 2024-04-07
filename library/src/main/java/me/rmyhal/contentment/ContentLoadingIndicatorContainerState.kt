package me.rmyhal.contentment

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.rmyhal.contentment.IndicatorContainerValue.Visible
import me.rmyhal.contentment.IndicatorContainerValue.Hidden
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ContentLoadingIndicatorContainerState(
  private val scope: CoroutineScope,
  private val minShowTimeMillis: Long,
  private val delayMillis: Long,
  val currentTimeMillis: () -> Long = { SystemClock.elapsedRealtime() },
) {

  init {
    require(minShowTimeMillis > 0) { "minShowTimeMillis must be > 0" }
    require(delayMillis > 0) { "delayMillis must be > 0" }
  }

  // not using "by" to return State<...> to the client
  var effectiveContainerValue = mutableStateOf<IndicatorContainerValue>(Hidden)
  private var containerValue by mutableStateOf<IndicatorContainerValue>(Hidden)
  private var startTimeMillis by mutableLongStateOf(-1L)

  fun show() {
    if (containerValue == Visible) return
    containerValue = Visible
    scope.launch {
      delay(delayMillis)
      // if we still want to be Visible after delay
      if (containerValue == Visible) {
        effectiveContainerValue.value = Visible
        startTimeMillis = currentTimeMillis()
      }
    }
  }

  fun hide() {
    containerValue = Hidden
    if (effectiveContainerValue.value is Hidden) return
    val diff = currentTimeMillis() - startTimeMillis
    if (diff >= minShowTimeMillis || startTimeMillis == -1L) {
      effectiveContainerValue.value = Hidden
      startTimeMillis = -1L
    } else {
      scope.launch {
        delay(minShowTimeMillis - diff)
        effectiveContainerValue.value = Hidden
        startTimeMillis = -1L
      }
    }
  }
}

sealed interface IndicatorContainerValue {
  data object Visible : IndicatorContainerValue
  data object Hidden : IndicatorContainerValue
}