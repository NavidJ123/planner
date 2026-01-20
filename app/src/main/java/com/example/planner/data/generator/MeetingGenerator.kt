import java.time.*
import java.util.UUID
import com.example.planner.data.entity.*

object MeetingGenerator {

    fun generateInstances(
        course: CourseEntity,
        patterns: List<CourseMeetingPatternEntity>,
        skips: Set<LocalDate>,
        generateFrom: LocalDate = course.startDate,
        generateTo: LocalDate = course.endDate
    ): List<CourseMeetingInstanceEntity> {
        if (course.isVirtual) return emptyList()

        val out = mutableListOf<CourseMeetingInstanceEntity>()
        val start = maxOf(course.startDate, generateFrom)
        val end = minOf(course.endDate, generateTo)

        for (p in patterns) {
            val dow = DayOfWeek.of(p.dayOfWeek)
            var d = start.with(dow)
            if (d.isBefore(start)) d = d.plusWeeks(1)

            while (!d.isAfter(end)) {
                if (!skips.contains(d)) {
                    val sdt = LocalDateTime.of(d, p.startTime)
                    val edt = LocalDateTime.of(d, p.endTime)
                    out += CourseMeetingInstanceEntity(
                        id = UUID.randomUUID().toString(),
                        courseId = course.id,
                        patternId = p.id,
                        startDateTime = sdt,
                        endDateTime = edt
                    )
                }
                d = d.plusWeeks(1)
            }
        }
        return out
    }
}
