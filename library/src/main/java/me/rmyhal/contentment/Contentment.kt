package me.rmyhal.contentment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Manages content loading gracefully with customizable behavior for displaying loading indicator.
 * Allows specifying minimum display time and delay before showing the loading indicator.
 *
 * If the content finishes loading before the [delayMillis] threshold, the [ContentmentScope.indicator] will not be shown.
 * If the content loading exceeds the [delayMillis] threshold, the [ContentmentScope.indicator] will be displayed,
 * and [ContentmentScope.content] will appear only after the [minShowTimeMillis] duration has passed.
 *
 * @param minShowTimeMillis The minimum amount of time to wait before hiding the [ContentmentScope.indicator].
 * @param delayMillis The amount of time to wait before showing the [ContentmentScope.indicator].
 * @param builder a wrapper to provide `@Composable` content for [ContentmentScope.content] and [ContentmentScope.indicator].
 */
@Composable
public fun Contentment(
  minShowTimeMillis: Long = ContentmentDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentmentDefaults.DelayMillis,
  builder: ContentmentScope.() -> Unit,
) {
  val state by rememberContentmentState(
    minShowTimeMillis = minShowTimeMillis,
    delayMillis = delayMillis,
    builder = builder,
  )
  when (val value = state) {
    is ContentmentState.Visible -> value.renderable()
    is ContentmentState.Undefined -> Unit
  }
}

@Composable
internal fun rememberContentmentState(
  minShowTimeMillis: Long = ContentmentDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentmentDefaults.DelayMillis,
  builder: ContentmentScope.() -> Unit,
): State<ContentmentState> {
  val scope = rememberCoroutineScope()
  return remember(key1 = minShowTimeMillis, key2 = delayMillis) {
    ContentmentScope(
      scope = scope,
      minShowTimeMillis = minShowTimeMillis,
      delayMillis = delayMillis,
    )
  }
    .apply(builder)
    .effectiveState
}