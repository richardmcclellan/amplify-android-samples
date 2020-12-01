package com.amplifyframework.app.datastore

import android.content.Context
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.Status
import com.amplifyframework.datastore.generated.model.Todo
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class Dummy {
    companion object {
        fun todo(context: Context): Todo {
            return Todo.builder()
                .name(context.resources.getStringArray(R.array.random_todos).random())
                .status(Status.NOT_STARTED)
                .dueDate(randomDateTime())
                .build()
        }

        /**
         * Returns a random Temporal.DateTime between 1 and 24 hours from now.
         */
        fun randomDateTime(): Temporal.DateTime {
            val cal = GregorianCalendar()
            cal.add(Calendar.HOUR_OF_DAY, Random.nextInt(1,24))
            return Temporal.DateTime(cal.time,
                TimeUnit.MILLISECONDS.toSeconds(cal.timeZone.rawOffset.toLong()).toInt()
            )
        }

        fun randomString(arr: Array<String>): String {
            return arr[Random.nextInt(IntRange(0, arr.size - 1))]
        }

    }

}