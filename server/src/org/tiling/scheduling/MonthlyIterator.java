package org.tiling.scheduling;

import java.util.Calendar;
import java.util.Date;

/**
 * A MonthlyIterator class returns a sequence of dates on subsequent days
 * representing the same time each month.
 */
public class MonthlyIterator implements ScheduleIterator {
    private final Calendar calendar = Calendar.getInstance();

    public MonthlyIterator(int dayOfMonth, int hourOfDay, int minute, int second) {
        this(dayOfMonth, hourOfDay, minute, second, new Date());
    }

    public MonthlyIterator(int dayOfMonth, int hourOfDay, int minute, int second, Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        if(!calendar.getTime().before(date)) {
            calendar.add(Calendar.MONTH, -1);
            //System.out.println(calendar.getTime());
        }
    }

    public Date next() {
        calendar.add(Calendar.MONTH, 1);
        //System.out.println(calendar.getTime());
        return calendar.getTime();
    }

}
