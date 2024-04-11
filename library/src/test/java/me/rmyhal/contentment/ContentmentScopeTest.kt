package me.rmyhal.contentment

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import me.rmyhal.contentment.ContentmentState.Undefined
import me.rmyhal.contentment.ContentmentState.Visible
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContentmentScopeTest {

  private val coroutineScope = TestScope()
  private val clock = FakeClock(initialTimeMillis = System.currentTimeMillis())

  private val scope = ContentmentScope(
    scope = coroutineScope,
    minShowTimeMillis = MIN_SHOW_TIME_MILLIS,
    delayMillis = DELAY_MILLIS,
    currentTimeMillis = { clock.timeMillis }
  )

  @Test(expected = IllegalArgumentException::class)
  fun `sanity check for delay & minShowTime values`() {
    ContentmentScope(
      scope = coroutineScope,
      minShowTimeMillis = -1L,
      delayMillis = -1L,
    )
  }

  @Test
  fun `content must be visible instantly if indicator not called`() {
    assertHidden()
    scope.content { }
    assertContent()
  }

  @Test
  fun `indicator must be visible for at least minShowTimeMillis after delay passed`() {
    assertHidden()
    scope.indicator { }
    assertHidden()
    advanceTime(DELAY_MILLIS + 1L)
    assertIndicator()
    scope.content { }
    assertIndicator()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertContent()
  }

  @Test
  fun `indicator must not be visible after calling content before delay ends`() {
    assertHidden()
    scope.indicator { }
    assertHidden()
    advanceTime(DELAY_MILLIS - 1L)
    assertHidden()
    scope.content { }
    advanceTime(DELAY_MILLIS)
    assertContent()
  }

  @Test
  fun `content must be visible after minShowTime passes`() {
    assertHidden()
    scope.indicator { }
    // because of using fakeClock and adjusting time manually
    // this time adjustment should be done in separate calls
    // so, new currentTimeMillis is picked up by `scope`
    advanceTime(DELAY_MILLIS + 1L)
    scope.content { }
    assertIndicator()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertContent()
  }

  @Test
  fun `multiple indicator call doesn't reset delay`() {
    scope.indicator { }
    advanceTime(DELAY_MILLIS - 1L)
    scope.indicator { }
    advanceTime(2L)
    assertIndicator()
  }

  @Test
  fun `multiple indicator & content cycles work correct`() {
    assertHidden()
    scope.indicator { }
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertIndicator()
    scope.content { }
    assertContent()
    scope.indicator { }
    assertContent()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertIndicator()
    scope.content { }
    assertContent()
  }

  @Test
  fun `indicator must be visible for a diff time only after delay passes`() {
    val halfMinShowTime = MIN_SHOW_TIME_MILLIS / 2L

    assertHidden()
    scope.indicator { }
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(halfMinShowTime)
    assertIndicator()
    scope.content { }
    assertIndicator()
    advanceTime(halfMinShowTime + 1L)
    assertContent()
  }

  private fun assertHidden() {
    assertThat(scope.effectiveState.value).isEqualTo(Undefined)
  }

  private fun assertIndicator() {
    assertThat(scope.effectiveState.value).isInstanceOf(Visible.Indicator::class.java)
  }

  private fun assertContent() {
    assertThat(scope.effectiveState.value).isInstanceOf(Visible.Content::class.java)
  }

  private fun advanceTime(millis: Long) {
    clock.advanceTimeBy(millis)
    coroutineScope.advanceTimeBy(millis)
  }

  companion object {
    private const val MIN_SHOW_TIME_MILLIS = 100L
    private const val DELAY_MILLIS = 100L
  }
}