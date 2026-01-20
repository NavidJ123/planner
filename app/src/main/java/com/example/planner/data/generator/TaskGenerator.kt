import java.time.*
import java.util.UUID
import kotlin.math.min
import com.example.planner.data.entity.*
object TaskGenerator {

    // Mon=1<<0 ... Sun=1<<6
    private fun maskHasDay(mask: Int, dow: DayOfWeek): Boolean {
        val idx = dow.value - 1
        return (mask and (1 shl idx)) != 0
    }

    private fun makeInstance(t: TaskTemplateEntity, anchor: LocalDate): TaskInstanceEntity {
        val due = anchor.plusDays(t.dueOffsetDays.toLong())
        return TaskInstanceEntity(
            id = UUID.randomUUID().toString(),
            templateId = t.id,
            title = t.title,
            courseId = t.courseId,
            priority = t.priority,
            startDate = anchor,
            endDate = due,     // bar spans anchor -> due (inclusive)
            dueDate = due
        )
    }

    fun generateInstances(t: TaskTemplateEntity, from: LocalDate, to: LocalDate): List<TaskInstanceEntity> {
        // We generate anchors within template window, then create instances from those anchors.
        val windowStart = maxOf(t.startDate, from)
        val windowEnd = minOf(t.endDate, to)
        if (windowStart.isAfter(windowEnd)) return emptyList()

        val out = mutableListOf<TaskInstanceEntity>()

        when (t.freq) {
            RecurrenceFreq.NONE -> {
                // For NONE: interpret startDate as the single anchor (or you can store a separate anchorDate in template if you prefer)
                val anchor = t.startDate
                if (!anchor.isBefore(windowStart) && !anchor.isAfter(windowEnd)) {
                    out += makeInstance(t, anchor)
                }
            }

            RecurrenceFreq.DAILY -> {
                var d = windowStart
                while (!d.isAfter(windowEnd)) {
                    out += makeInstance(t, d)
                    d = d.plusDays(t.interval.toLong())
                }
            }

            RecurrenceFreq.WEEKLY, RecurrenceFreq.BIWEEKLY -> {
                val weekStep = if (t.freq == RecurrenceFreq.BIWEEKLY) 2L else t.interval.toLong()

                // Find the first week anchor to iterate from.
                // We'll start at the Monday of the week that contains windowStart (or earlier) then step by weekStep.
                var weekAnchor = windowStart.with(DayOfWeek.MONDAY)
                if (weekAnchor.isAfter(windowStart)) weekAnchor = weekAnchor.minusWeeks(1)

                while (!weekAnchor.isAfter(windowEnd)) {
                    for (i in 0..6) {
                        val d = weekAnchor.plusDays(i.toLong())
                        if (d.isBefore(windowStart) || d.isAfter(windowEnd)) continue
                        if (t.daysOfWeekMask != 0 && maskHasDay(t.daysOfWeekMask, d.dayOfWeek)) {
                            out += makeInstance(t, d)
                        }
                    }
                    weekAnchor = weekAnchor.plusWeeks(weekStep)
                }
            }

            RecurrenceFreq.MONTHLY -> {
                // Simple monthly: same day-of-month as windowStart's day (or template start's day)
                val dom = t.startDate.dayOfMonth
                var d = windowStart.withDayOfMonth(min(dom, windowStart.lengthOfMonth()))
                if (d.isBefore(windowStart)) d = d.plusMonths(1)

                while (!d.isAfter(windowEnd)) {
                    out += makeInstance(t, d)
                    d = d.plusMonths(t.interval.toLong())
                    d = d.withDayOfMonth(min(dom, d.lengthOfMonth()))
                }
            }
        }

        return out
    }
}
