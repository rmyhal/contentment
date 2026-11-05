package me.rmyhal.contentment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Returns [state] with [delayMillis] and [minShowTimeMillis] applied.
 *
 * - A loading state, as decided by [isLoading], is returned after it stays requested for [delayMillis].
 * A loading state that ends earlier is never returned.
 * - A returned loading state is returned for at least [minShowTimeMillis].
 * - Other states are returned immediately.
 *
 * Example: `AnimatedContent(targetState = rememberContentment(uiState, isLoading = { it is Loading }))`.
 *
 * @param state The latest state.
 * @param isLoading Whether a state is a loading state.
 * @param minShowTimeMillis The minimum time a loading state is returned for, once returned. `0` sets no minimum.
 * @param delayMillis The time a loading state must stay requested before it's returned.
 * `0` skips the delay.
 * @return The state to show, or `null` while the first state is loading and [delayMillis] hasn't passed.
 */
@Composable
public fun <T : Any> rememberContentment(
  state: T,
  isLoading: (T) -> Boolean,
  minShowTimeMillis: Long = ContentmentDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentmentDefaults.DelayMillis,
): T? {
  val scope = rememberCoroutineScope()
  val timer = remember(minShowTimeMillis, delayMillis) {
    ContentmentTimer<T>(
      scope = scope,
      minShowTimeMillis = minShowTimeMillis,
      delayMillis = delayMillis,
    )
  }
  val loading = isLoading(state)
  SideEffect { timer.update(state, loading) }
  return timer.resolve(state, loading)
}

/**
 * Returns [loading] with [delayMillis] and [minShowTimeMillis] applied.
 *
 * - Returns `true` after [loading] stays `true` for [delayMillis].
 * - Once `true`, returns `true` for at least [minShowTimeMillis].
 *
 * Example: `AnimatedVisibility(visible = rememberIndicatorVisible(loading)) { CircularProgressIndicator() }`.
 *
 * @param loading Whether the content is loading.
 * @param minShowTimeMillis The minimum time to return `true` for, once returned. `0` sets no minimum.
 * @param delayMillis The time [loading] must stay `true` before `true` is returned. `0` skips the delay.
 */
@Composable
public fun rememberIndicatorVisible(
  loading: Boolean,
  minShowTimeMillis: Long = ContentmentDefaults.MinShowTimeMillis,
  delayMillis: Long = ContentmentDefaults.DelayMillis,
): Boolean = rememberContentment(
  state = loading,
  isLoading = { it },
  minShowTimeMillis = minShowTimeMillis,
  delayMillis = delayMillis,
) ?: false
