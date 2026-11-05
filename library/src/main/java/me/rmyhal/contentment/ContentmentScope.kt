package me.rmyhal.contentment

import android.os.SystemClock
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import me.rmyhal.contentment.ContentmentState.Undefined
import me.rmyhal.contentment.ContentmentState.Visible.Content
import me.rmyhal.contentment.ContentmentState.Visible.Indicator

public class ContentmentScope internal constructor(
  scope: CoroutineScope,
  minShowTimeMillis: Long,
  delayMillis: Long,
  currentTimeMillis: () -> Long = { SystemClock.elapsedRealtime() },
) {
  private val timer = ContentmentTimer<ContentmentState.Visible>(
    scope = scope,
    minShowTimeMillis = minShowTimeMillis,
    delayMillis = delayMillis,
    currentTimeMillis = currentTimeMillis,
  )

  /** The latest request from [indicator] or [content]. Applied to the timer by [commit]. */
  private var requested: ContentmentState.Visible? = null

  public fun indicator(i: @Composable () -> Unit) {
    // Each recomposition passes a new lambda instance, which would otherwise count as a new loading value.
    if (requested is Indicator) return
    requested = Indicator(i)
  }

  public fun content(c: @Composable () -> Unit) {
    requested = Content(c)
  }

  /** What to show for the latest request right now. Doesn't change state. */
  internal fun resolve(): ContentmentState {
    val request = requested ?: return Undefined
    return timer.resolve(request, isLoading = request is Indicator) ?: Undefined
  }

  /** Applies the latest request to the timer. */
  internal fun commit() {
    val request = requested ?: return
    timer.update(request, isLoading = request is Indicator)
  }
}

internal sealed interface ContentmentState {
  sealed interface Visible : ContentmentState {
    val renderable: @Composable () -> Unit

    data class Indicator(override val renderable: @Composable () -> Unit) : Visible
    data class Content(override val renderable: @Composable () -> Unit) : Visible
  }

  data object Undefined : ContentmentState
}
