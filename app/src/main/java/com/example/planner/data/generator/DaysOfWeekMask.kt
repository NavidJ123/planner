package com.example.planner.data.generator

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

    // --- ADD THIS FUNCTION ---
    fun toDays(mask: Int): List<DayOfWeek> {
        val out = mutableListOf<DayOfWeek>()
        for (d in DayOfWeek.values()) {
            val idx = d.value - 1
            if ((mask and (1 shl idx)) != 0) {
                out.add(d)
            }
        }
        return out
    }
}