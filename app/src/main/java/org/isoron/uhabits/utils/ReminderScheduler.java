/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.utils;

import android.app.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.models.*;

import java.util.*;

import javax.inject.*;

import static org.isoron.uhabits.utils.DateUtils.*;

@Singleton
public class ReminderScheduler
{
    @Inject
    protected PendingIntentFactory pendingIntentFactory;

    @Inject
    protected IntentScheduler intentScheduler;

    @Inject
    HabitLogger logger;

    @Inject
    public ReminderScheduler()
    {
        HabitsApplication.getComponent().inject(this);
    }

    public void schedule(@NonNull Habit habit, @Nullable Long reminderTime)
    {
        if (!habit.hasReminder()) return;
        Reminder reminder = habit.getReminder();
        if (reminderTime == null) reminderTime = getReminderTime(reminder);
        long timestamp = getStartOfDay(toLocalTime(reminderTime));

        PendingIntent intent = pendingIntentFactory.showReminder(habit,
                reminderTime, timestamp);
        intentScheduler.schedule(reminderTime, intent);
        logger.logReminderScheduled(habit, reminderTime);
    }

    public void schedule(@NonNull HabitList habits)
    {
        HabitList reminderHabits = habits.getFiltered(HabitMatcher.WITH_ALARM);
        for (Habit habit : reminderHabits)
            schedule(habit, null);
    }

    @NonNull
    private Long getReminderTime(@NonNull Reminder reminder)
    {
        Calendar calendar = DateUtils.getStartOfTodayCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);
        Long time = calendar.getTimeInMillis();

        if (DateUtils.getLocalTime() > time)
            time += AlarmManager.INTERVAL_DAY;

        return time;
    }
}
