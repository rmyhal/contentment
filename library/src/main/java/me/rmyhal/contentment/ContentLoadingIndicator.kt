package me.rmyhal.contentment

import androidx.compose.runtime.Composable

/**
 * Wrapper that waits a minimum time to be dismissed before showing [indicator].
 * Once visible, the [indicator] will be visible for a minimum amount of time to avoid "flashes" in the UI.
 *
 * Compose adaptation of XML view [androidx.core.widget.ContentLoadingProgressBar].
 *
 * @param loading Whether the content is loading.
 * @param minShowTimeMillis The minimum amount of time to wait before hiding the [indicator]. `0` sets no minimum.
 * @param delayMillis The amount of time to wait before showing the [indicator]. `0` skips the delay.
 * @param indicator The content to show while the content is loading.
 */
@Composable
public fun ContentLoadingIndicator(
  loading: Boolean,
  minShowTimeMillis: Long = ContentmentDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentmentDefaults.DelayMillis,
  indicator: @Composable () -> Unit,
) {
  if (rememberIndicatorVisible(loading, minShowTimeMillis, delayMillis)) {
    indicator()
  }
}
