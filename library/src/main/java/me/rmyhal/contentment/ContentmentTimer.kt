package me.rmyhal.contentment

import android.os.SystemClock
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Applies `delayMillis` and `minShowTimeMillis` to a stream of values.
 *
 * - A loading value is shown only after it stays requested for `delayMillis`.
 * - Once shown, it stays for at least `minShowTimeMillis` before a content value replaces it.
 * - Content values otherwise show right away.
 *
 * [update] changes state and schedules work. [resolve] only reads state, so it is safe to call during composition.
 * [effective] is `null` until the first value is shown.
 */
internal class ContentmentTimer<T : Any>(
  private val scope: CoroutineScope,
  private val minShowTimeMillis: Long,
  private val delayMillis: Long,
  private val currentTimeMillis: () -> Long = { SystemClock.elapsedRealtime() },
) {
  private val effectiveValue = mutableStateOf<T?>(null)
  val effective: State<T?> get() = effectiveValue

  private var target: T? = null
  private var isTargetLoading = false
  private var isLoadingShown = false
  private var loadingShownAtMillis = 0L

  /** Either the delay before showing loading, or the wait before replacing it with content. */
  private var pendingJob: Job? = null

  init {
    require(minShowTimeMillis >= 0L) { "minShowTimeMillis must be >= 0, was $minShowTimeMillis" }
    require(delayMillis >= 0L) { "delayMillis must be >= 0, was $delayMillis" }
  }

  /**
   * The value to show for [value] right now, as [update] would decide it, without changing state.
   */
  fun resolve(value: T, isLoading: Boolean): T? = when {
    isLoading == isLoadingShown -> value
    !isLoading && remainingMinShowMillis() <= 0L -> value
    else -> effectiveValue.value
  }

  fun update(value: T, isLoading: Boolean) {
    target = value
    if (isLoading) requestLoading(value) else requestContent(value)
  }

  private fun requestLoading(value: T) {
    val wasTargetLoading = isTargetLoading
    isTargetLoading = true
    when {
      // Loading is shown: cancels a pending switch to content.
      isLoadingShown -> {
        cancelPending()
        effectiveValue.value = value
      }
      // The running delay shows the latest target.
      wasTargetLoading -> Unit
      else -> {
        cancelPending()
        pendingJob = scope.launch {
          delay(delayMillis.milliseconds)
          isLoadingShown = true
          loadingShownAtMillis = currentTimeMillis()
          effectiveValue.value = target
        }
      }
    }
  }

  private fun requestContent(value: T) {
    isTargetLoading = false
    if (!isLoadingShown) {
      cancelPending()
      effectiveValue.value = value
      return
    }
    val remainingMillis = remainingMinShowMillis()
    when {
      remainingMillis <= 0L -> {
        cancelPending()
        showContent()
      }
      // The scheduled switch shows the latest target.
      pendingJob?.isActive == true -> Unit
      else -> pendingJob = scope.launch {
        delay(remainingMillis.milliseconds)
        showContent()
      }
    }
  }

  private fun remainingMinShowMillis(): Long = minShowTimeMillis - (currentTimeMillis() - loadingShownAtMillis)

  private fun showContent() {
    isLoadingShown = false
    effectiveValue.value = target
  }

  private fun cancelPending() {
    pendingJob?.cancel()
    pendingJob = null
  }
}
