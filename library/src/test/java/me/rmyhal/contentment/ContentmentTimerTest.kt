package me.rmyhal.contentment

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContentmentTimerTest {

  private val coroutineScope = TestScope()
  private val clock = FakeClock(initialTimeMillis = 1_000L)

  private val timer = ContentmentTimer<String>(
    scope = coroutineScope,
    minShowTimeMillis = MIN_SHOW_TIME_MILLIS,
    delayMillis = DELAY_MILLIS,
    currentTimeMillis = { clock.timeMillis },
  )

  @Test(expected = IllegalArgumentException::class)
  fun `rejects negative minShowTimeMillis`() {
    ContentmentTimer<String>(coroutineScope, minShowTimeMillis = -1L, delayMillis = DELAY_MILLIS)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `rejects negative delayMillis`() {
    ContentmentTimer<String>(coroutineScope, minShowTimeMillis = MIN_SHOW_TIME_MILLIS, delayMillis = -1L)
  }

  @Test
  fun `zero delay shows loading without waiting`() {
    val timer = ContentmentTimer<String>(
      scope = coroutineScope,
      minShowTimeMillis = MIN_SHOW_TIME_MILLIS,
      delayMillis = 0L,
      currentTimeMillis = { clock.timeMillis },
    )
    timer.update("loading", isLoading = true)
    coroutineScope.runCurrent()

    assertThat(timer.effective.value).isEqualTo("loading")
  }

  @Test
  fun `zero minShowTime replaces loading as soon as it ends`() {
    val timer = ContentmentTimer<String>(
      scope = coroutineScope,
      minShowTimeMillis = 0L,
      delayMillis = DELAY_MILLIS,
      currentTimeMillis = { clock.timeMillis },
    )
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS + 1L)
    assertThat(timer.effective.value).isEqualTo("loading")

    timer.update("content", isLoading = false)

    assertThat(timer.effective.value).isEqualTo("content")
  }

  @Test
  fun `content shows immediately when loading never started`() {
    timer.update("content", isLoading = false)

    assertThat(timer.effective.value).isEqualTo("content")
  }

  @Test
  fun `nothing shows until delay passes`() {
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS - 1L)

    assertThat(timer.effective.value).isNull()
  }

  @Test
  fun `loading shows after delay`() {
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS + 1L)

    assertThat(timer.effective.value).isEqualTo("loading")
  }

  @Test
  fun `content before delay skips loading entirely`() {
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS / 2)
    timer.update("content", isLoading = false)
    advanceTime(DELAY_MILLIS * 2)

    assertThat(timer.effective.value).isEqualTo("content")
  }

  @Test
  fun `loading stays for minShowTime once shown`() {
    showLoading()
    timer.update("content", isLoading = false)
    advanceTime(MIN_SHOW_TIME_MILLIS - 1L)
    assertThat(timer.effective.value).isEqualTo("loading")

    advanceTime(2L)
    assertThat(timer.effective.value).isEqualTo("content")
  }

  @Test
  fun `content shows immediately when minShowTime already passed`() {
    showLoading()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    timer.update("content", isLoading = false)

    assertThat(timer.effective.value).isEqualTo("content")
  }

  @Test
  fun `delayed content is the latest content value`() {
    showLoading()
    timer.update("content 1", isLoading = false)
    advanceTime(MIN_SHOW_TIME_MILLIS / 2)
    timer.update("content 2", isLoading = false)
    advanceTime(MIN_SHOW_TIME_MILLIS)

    assertThat(timer.effective.value).isEqualTo("content 2")
  }

  @Test
  fun `loading again during minShowTime keeps loading and drops the pending content`() {
    showLoading()
    timer.update("content", isLoading = false)
    advanceTime(MIN_SHOW_TIME_MILLIS / 2)
    timer.update("loading", isLoading = true)
    advanceTime(MIN_SHOW_TIME_MILLIS * 2)

    assertThat(timer.effective.value).isEqualTo("loading")
  }

  @Test
  fun `repeated loading updates do not restart the delay`() {
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS - 1L)
    timer.update("loading", isLoading = true)
    advanceTime(2L)

    assertThat(timer.effective.value).isEqualTo("loading")
  }

  @Test
  fun `shown loading value follows updates`() {
    showLoading()
    timer.update("loading 50%", isLoading = true)

    assertThat(timer.effective.value).isEqualTo("loading 50%")
  }

  @Test
  fun `content stays while loading again until delay passes`() {
    timer.update("content 1", isLoading = false)
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS - 1L)
    assertThat(timer.effective.value).isEqualTo("content 1")

    advanceTime(2L)
    assertThat(timer.effective.value).isEqualTo("loading")
  }

  @Test
  fun `minShowTime counts from the second time loading is shown`() {
    showLoading()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    timer.update("content", isLoading = false)
    showLoading()
    timer.update("content", isLoading = false)

    assertThat(timer.effective.value).isEqualTo("loading")
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertThat(timer.effective.value).isEqualTo("content")
  }

  @Test
  fun `resolve returns content immediately before update`() {
    assertThat(timer.resolve("content", isLoading = false)).isEqualTo("content")
  }

  @Test
  fun `resolve returns nothing for loading before delay passes`() {
    timer.update("loading", isLoading = true)

    assertThat(timer.resolve("loading", isLoading = true)).isNull()
  }

  @Test
  fun `resolve returns the latest loading value while loading is shown`() {
    showLoading()

    assertThat(timer.resolve("loading 50%", isLoading = true)).isEqualTo("loading 50%")
  }

  @Test
  fun `resolve keeps loading until minShowTime passes`() {
    showLoading()

    assertThat(timer.resolve("content", isLoading = false)).isEqualTo("loading")
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertThat(timer.resolve("content", isLoading = false)).isEqualTo("content")
  }

  @Test
  fun `resolve does not change state`() {
    showLoading()
    timer.resolve("content", isLoading = false)
    advanceTime(MIN_SHOW_TIME_MILLIS * 2)

    assertThat(timer.effective.value).isEqualTo("loading")
  }

  private fun showLoading() {
    timer.update("loading", isLoading = true)
    advanceTime(DELAY_MILLIS + 1L)
    assertThat(timer.effective.value).isEqualTo("loading")
  }

  private fun advanceTime(millis: Long) {
    clock.advanceTimeBy(millis)
    coroutineScope.advanceTimeBy(millis)
  }

  private companion object {
    const val MIN_SHOW_TIME_MILLIS = 100L
    const val DELAY_MILLIS = 100L
  }
}
