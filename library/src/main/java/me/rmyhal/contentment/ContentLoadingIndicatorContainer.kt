package me.rmyhal.contentment

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import me.rmyhal.contentment.IndicatorContainerValue.Hidden
import me.rmyhal.contentment.IndicatorContainerValue.Visible

/**
 * Container that waits a minimum time to be dismissed before showing [indicator].
 * Once visible, the [indicator] will be visible for a minimum amount of time to avoid "flashes" in the UI.
 *
 * Compose adaptation of XML view [androidx.core.widget.ContentLoadingProgressBar].
 *
 * @param loading Whether the content is loading.
 * @param minShowTimeMillis The minimum amount of time to wait before hiding the [indicator].
 * @param delayMillis The amount of time to wait before showing the [indicator].
 * @param indicator The content to show while the content is loading.
 */
@Composable
fun ContentLoadingIndicatorContainer(
  loading: Boolean,
  modifier: Modifier = Modifier,
  minShowTimeMillis: Long = ContentLoadingDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentLoadingDefaults.DelayMillis,
  indicator: @Composable () -> Unit,
) {
  val state by rememberContentLoadingIndicatorContainerState(
    loading = loading,
    minShowTimeMillis = minShowTimeMillis,
    delayMillis = delayMillis,
  )
  when (state) {
    Visible -> Box(
      modifier = modifier
        .semantics { stateDescription = ContentLoadingDefaults.ContentLoadingIndicatorVisibleDescription }
    ) { indicator() }

    Hidden -> Unit
  }
}

@Composable
fun rememberContentLoadingIndicatorContainerState(
  loading: Boolean,
  minShowTimeMillis: Long = ContentLoadingDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentLoadingDefaults.DelayMillis,
): State<IndicatorContainerValue> {
  val scope = rememberCoroutineScope()
  return remember(key1 = minShowTimeMillis, key2 = delayMillis) {
    ContentLoadingIndicatorContainerState(
      scope = scope,
      minShowTimeMillis = minShowTimeMillis,
      delayMillis = delayMillis,
    )
  }.apply {
    if (loading) {
      show()
    } else {
      hide()
    }
  }.effectiveContainerValue
}