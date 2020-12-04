package com.amplifyframework.app.datastore

import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.Status
import com.amplifyframework.datastore.generated.model.Todo
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class Dummy {
    companion object {
        fun todo(): Todo {
            return Todo.builder()
                .name(randomTodoName())
                .status(Status.NOT_STARTED)
                .dueDate(randomDateTime())
                .build()
        }

        /**
         * Returns a random name for a Todo, composed of a verb concatenated with a thing.
         */
        private fun randomTodoName(): String {
            val verbs = arrayOf(
                "Mop", "Sweep", "Clean", "Brush", "Wipe",
                "Dust", "Scrub", "Polish", "Shine", "Wash")
            val things = arrayOf(
                "Cabinets", "Floors", "Walls", "Ceiling", "Blinds",
                "Table", "Desk", "Chair", "Lamp", "Clothes")
            return "${randomString(verbs)} ${randomString(things)}"
        }

        /**
         * Returns a random Temporal.DateTime between 1 and 24 hours from now.
         */
        private fun randomDateTime(): Temporal.DateTime {
            val cal = GregorianCalendar()
            cal.add(Calendar.HOUR_OF_DAY, Random.nextInt(1,24))
            return Temporal.DateTime(cal.time,
                TimeUnit.MILLISECONDS.toSeconds(cal.timeZone.rawOffset.toLong()).toInt()
            )
        }

        private fun randomString(arr: Array<String>): String {
            return arr[Random.nextInt(IntRange(0, arr.size - 1))]
        }
    }
}