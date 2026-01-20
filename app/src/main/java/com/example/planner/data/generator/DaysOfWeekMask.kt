import java.time.DayOfWeek

object DaysOfWeekMask {
    // Mon=1<<0 ... Sun=1<<6
    fun of(vararg days: DayOfWeek): Int {
        var mask = 0
        for (d in days) {
            val idx = d.value - 1
            mask = mask or (1 shl idx)
        }
        return mask
    }
}
