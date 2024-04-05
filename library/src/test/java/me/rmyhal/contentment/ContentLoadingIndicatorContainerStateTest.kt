package me.rmyhal.contentment

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import me.rmyhal.contentment.IndicatorContainerValue.Hidden
import me.rmyhal.contentment.IndicatorContainerValue.Visible
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContentLoadingIndicatorContainerStateTest {

  private val scope = TestScope()
  private val clock = FakeClock(initialTimeMillis = System.currentTimeMillis())

  private val state = ContentLoadingIndicatorContainerState(
    scope = scope,
    minShowTimeMillis = MIN_SHOW_TIME_MILLIS,
    delayMillis = DELAY_MILLIS,
    currentTimeMillis = { clock.timeMillis }
  )

  @Test(expected = IllegalArgumentException::class)
  fun `sanity check for delay & minShowTime values`() {
    ContentLoadingIndicatorContainerState(
      scope = scope,
      minShowTimeMillis = -1L,
      delayMillis = -1L,
    )
  }

  @Test
  fun `must be visible for at least minShowTimeMillis after delay passed`() = scope.runTest {
    assertHidden()
    state.show()
    assertHidden()
    advanceTime(DELAY_MILLIS + 1L)
    assertVisible()
    state.hide()
    assertVisible()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertHidden()
  }

  @Test
  fun `must not be visible after calling hide until delay ends`() = scope.runTest {
    assertHidden()
    state.show()
    assertHidden()
    advanceTime(DELAY_MILLIS - 1L)
    assertHidden()
    state.hide()
    advanceTime(DELAY_MILLIS)
    assertHidden()
  }

  @Test
  fun `must be hidden instantly after minShowTime passes`() = scope.runTest {
    assertHidden()
    state.show()
    // because of using fakeClock and adjusting time manually
    // this time adjustment should be done in separate calls
    // so, new currentTimeMillis is picked up by `state`
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertVisible()
    state.hide()
    assertHidden()
  }

  @Test
  fun `double show doesn't reset delay`() = scope.runTest {
    state.show()
    advanceTime(DELAY_MILLIS - 1L)
    state.show()
    advanceTime(2L)
    assertVisible()
  }

  @Test
  fun `multiple show & hide cycles works correct`() {
    assertHidden()
    state.show()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertVisible()
    state.hide()
    assertHidden()
    state.show()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertVisible()
    state.hide()
    assertHidden()
  }

  @Test
  fun `must be visible for a diff time only after delay passes`() = scope.runTest {
    val halfMinShowTime = MIN_SHOW_TIME_MILLIS / 2L

    assertHidden()
    state.show()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(halfMinShowTime)
    assertVisible()
    state.hide()
    assertVisible()
    advanceTime(halfMinShowTime + 1L)
    assertHidden()
  }

  private fun assertHidden() {
    assertThat(state.effectiveContainerValue.value).isEqualTo(Hidden)
  }

  private fun assertVisible() {
    assertThat(state.effectiveContainerValue.value).isEqualTo(Visible)
  }

  private fun advanceTime(millis: Long) {
    clock.advanceTimeBy(millis)
    scope.advanceTimeBy(millis)
  }

  companion object {
    private const val MIN_SHOW_TIME_MILLIS = 100L
    private const val DELAY_MILLIS = 100L
  }
}