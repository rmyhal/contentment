package me.rmyhal.contentment

internal class FakeClock(
  initialTimeMillis: Long
) {

  var timeMillis: Long = initialTimeMillis

  fun advanceTimeBy(timeMillis: Long) {
    this.timeMillis += timeMillis
  }
}