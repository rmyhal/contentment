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
    requestContent()
    assertContent()
  }

  @Test
  fun `indicator must be visible for at least minShowTimeMillis after delay passed`() {
    assertHidden()
    requestIndicator()
    assertHidden()
    advanceTime(DELAY_MILLIS + 1L)
    assertIndicator()
    requestContent()
    assertIndicator()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertContent()
  }

  @Test
  fun `indicator must not be visible after calling content before delay ends`() {
    assertHidden()
    requestIndicator()
    assertHidden()
    advanceTime(DELAY_MILLIS - 1L)
    assertHidden()
    requestContent()
    advanceTime(DELAY_MILLIS)
    assertContent()
  }

  @Test
  fun `content must be visible after minShowTime passes`() {
    assertHidden()
    requestIndicator()
    // because of using fakeClock and adjusting time manually
    // this time adjustment should be done in separate calls
    // so, new currentTimeMillis is picked up by `scope`
    advanceTime(DELAY_MILLIS + 1L)
    requestContent()
    assertIndicator()
    advanceTime(MIN_SHOW_TIME_MILLIS + 1L)
    assertContent()
  }

  @Test
  fun `multiple indicator call doesn't reset delay`() {
    requestIndicator()
    advanceTime(DELAY_MILLIS - 1L)
    requestIndicator()
    advanceTime(2L)
    assertIndicator()
  }

  @Test
  fun `multiple indicator & content cycles work correct`() {
    assertHidden()
    requestIndicator()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertIndicator()
    requestContent()
    assertContent()
    requestIndicator()
    assertContent()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(MIN_SHOW_TIME_MILLIS)
    assertIndicator()
    requestContent()
    assertContent()
  }

  @Test
  fun `indicator must be visible for a diff time only after delay passes`() {
    val halfMinShowTime = MIN_SHOW_TIME_MILLIS / 2L

    assertHidden()
    requestIndicator()
    advanceTime(DELAY_MILLIS + 1L)
    advanceTime(halfMinShowTime)
    assertIndicator()
    requestContent()
    assertIndicator()
    advanceTime(halfMinShowTime + 1L)
    assertContent()
  }

  private fun assertHidden() {
    assertThat(scope.resolve()).isEqualTo(Undefined)
  }

  private fun assertIndicator() {
    assertThat(scope.resolve()).isInstanceOf(Visible.Indicator::class.java)
  }

  private fun assertContent() {
    assertThat(scope.resolve()).isInstanceOf(Visible.Content::class.java)
  }

  private fun requestIndicator() {
    scope.indicator { }
    scope.commit()
  }

  private fun requestContent() {
    scope.content { }
    scope.commit()
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