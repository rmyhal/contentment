package me.rmyhal.contentment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

/**
 * If the content finishes loading before the [delayMillis] threshold, the [ContentmentScope.indicator] will not be shown.
 * If the content loading exceeds the [delayMillis] threshold, the [ContentmentScope.indicator] will be displayed,
 * and [ContentmentScope.content] will appear only after the [minShowTimeMillis] duration has passed.
 *
 * @param minShowTimeMillis The minimum amount of time to wait before hiding the [ContentmentScope.indicator].
 * `0` sets no minimum.
 * @param delayMillis The amount of time to wait before showing the [ContentmentScope.indicator].
 * `0` skips the delay.
 * @param builder a wrapper to provide `@Composable` content for [ContentmentScope.content] and [ContentmentScope.indicator].
 */
@Composable
public fun Contentment(
  minShowTimeMillis: Long = ContentmentDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentmentDefaults.DelayMillis,
  builder: ContentmentScope.() -> Unit,
) {
  val contentmentScope = rememberContentmentScope(
    minShowTimeMillis = minShowTimeMillis,
    delayMillis = delayMillis,
  ).apply(builder)
  SideEffect { contentmentScope.commit() }
  when (val value = contentmentScope.resolve()) {
    is ContentmentState.Visible -> value.renderable()
    is ContentmentState.Undefined -> Unit
  }
}

@Composable
private fun rememberContentmentScope(
  minShowTimeMillis: Long,
  delayMillis: Long,
): ContentmentScope {
  val scope = rememberCoroutineScope()
  return remember(key1 = minShowTimeMillis, key2 = delayMillis) {
    ContentmentScope(
      scope = scope,
      minShowTimeMillis = minShowTimeMillis,
      delayMillis = delayMillis,
    )
  }
}
