package me.rmyhal.contentment

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rmyhal.contentment.ContentmentState.Undefined
import me.rmyhal.contentment.ContentmentState.Visible.Content
import me.rmyhal.contentment.ContentmentState.Visible.Indicator

public class ContentmentScope internal constructor(
  private val scope: CoroutineScope,
  private val minShowTimeMillis: Long,
  private val delayMillis: Long,
  private val currentTimeMillis: () -> Long = { SystemClock.elapsedRealtime() },
) {
  internal var effectiveState = mutableStateOf<ContentmentState>(Undefined)
  private var state by mutableStateOf<ContentmentState>(Undefined)
  private var startTimeMillis by mutableLongStateOf(-1L)

  init {
    require(minShowTimeMillis > 0L) { "minShowTimeMillis must be > 0" }
    require(delayMillis > 0L) { "delayMillis must be > 0" }
  }

  public fun indicator(i: @Composable () -> Unit) {
    if (state is Indicator) return
    state = Indicator(i)
    scope.launch {
      delay(delayMillis)
      // if we still want to have Indicator after delay
      if (state is Indicator) {
        effectiveState.value = Indicator(i)
        startTimeMillis = currentTimeMillis()
      }
    }
  }

  public fun content(c: @Composable () -> Unit) {
    state = Content(c)
    if (effectiveState.value == state) return
    val diff = currentTimeMillis() - startTimeMillis
    if (diff >= minShowTimeMillis || startTimeMillis == -1L) {
      effectiveState.value = Content(c)
    } else {
      scope.launch {
        delay(minShowTimeMillis - diff)
        effectiveState.value = Content(c)
        startTimeMillis = -1L
      }
    }
  }
}

internal sealed interface ContentmentState {
  sealed class Visible(open val content: @Composable () -> Unit): ContentmentState {
    data class Indicator(override val content: @Composable () -> Unit): Visible(content)
    data class Content(override val content: @Composable () -> Unit): Visible(content)
  }
  data object Undefined: ContentmentState
}