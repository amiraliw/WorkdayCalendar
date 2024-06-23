// Denne programmen kjores ved å kjøre "javac WorkdayCalendar.java && java WorkdayCalendar" fra terminalen.

import java.util.*;
import java.text.SimpleDateFormat;

public class WorkdayCalendar {
    private Calendar workdayStart;
    private Calendar workdayStop;
    private Set<Calendar> holidays = new HashSet<>();
    private Set<Calendar> recurringHolidays = new HashSet<>();
    private static final float WORKDAY_IN_MILLISECONDS = 28800000;
    

    public void setHoliday(Calendar date) {
        Calendar holiday = (Calendar) date.clone();
        holiday.set(Calendar.HOUR_OF_DAY, 0);
        holiday.set(Calendar.MINUTE, 0);
        holiday.set(Calendar.SECOND, 0);
        holiday.set(Calendar.MILLISECOND, 0);
        holidays.add(holiday);
    }

    public void setRecurringHoliday(Calendar date) {
        Calendar recurringHoliday = (Calendar) date.clone();
        recurringHoliday.set(Calendar.YEAR, 1970); // Arbitrary year to handle recurring holidays
        recurringHoliday.set(Calendar.HOUR_OF_DAY, 0);
        recurringHoliday.set(Calendar.MINUTE, 0);
        recurringHoliday.set(Calendar.SECOND, 0);
        recurringHoliday.set(Calendar.MILLISECOND, 0);
        recurringHolidays.add(recurringHoliday);
    }

    public void setWorkdayStartAndStop(Calendar start, Calendar stop) {
        workdayStart = (Calendar) start.clone();
        workdayStart.set(Calendar.YEAR, 1970); // Arbitrary year
        workdayStart.set(Calendar.MONTH, Calendar.JANUARY);
        workdayStart.set(Calendar.DAY_OF_MONTH, 1);
        workdayStop = (Calendar) stop.clone();
        workdayStop.set(Calendar.YEAR, 1970); // Arbitrary year
        workdayStop.set(Calendar.MONTH, Calendar.JANUARY);
        workdayStop.set(Calendar.DAY_OF_MONTH, 1);
    }

    public Date getWorkdayIncrement(Date startDate, float incrementInWorkdays) {
        
        Calendar current = Calendar.getInstance();
        
        current.setTime(startDate);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        
        if (incrementInWorkdays > 0) {
            return addWorkdays(current, incrementInWorkdays);
        } else {
            return subtractWorkdays(current, -incrementInWorkdays);
        }
    }

    private Date addWorkdays(Calendar current, float incrementInWorkdays) {
        if(isBeforeStart(current)){
            incrementInWorkdays -= 1;
        } else if (isBetweenStartAndStop(current)) {
            incrementInWorkdays -= getDifferenceInMilliSeconds(current, 16)/WORKDAY_IN_MILLISECONDS;
        } 
        
        while (incrementInWorkdays > 0) {
            current.add(Calendar.DAY_OF_MONTH, 1);
            if (isWorkday(current)) {
                incrementInWorkdays -= 1;
            }
        }

        return adjustTime(current, (incrementInWorkdays+1));
    }

    private Date subtractWorkdays(Calendar current, float incrementInWorkdays) {
        if(isAfterStop(current)){
            incrementInWorkdays -= 1;
        }else if (isBetweenStartAndStop(current)) {
            incrementInWorkdays -= getDifferenceInMilliSeconds(current, 16)/WORKDAY_IN_MILLISECONDS;
        } 

        while (incrementInWorkdays > 0) {
            current.add(Calendar.DAY_OF_MONTH, -1);
            if (isWorkday(current)) {
                incrementInWorkdays -= 1;
            }
        }

        return adjustTime(current, -incrementInWorkdays);
    }

    private Date adjustTime(Calendar current, float fraction) {
        long startMillis = workdayStart.getTimeInMillis();
        long stopMillis = workdayStop.getTimeInMillis();
        long workdayLength = stopMillis - startMillis;
        long fractionMillis = (long) (workdayLength * fraction);

        current.set(Calendar.HOUR_OF_DAY, workdayStart.get(Calendar.HOUR_OF_DAY));
        current.set(Calendar.MINUTE, workdayStart.get(Calendar.MINUTE));
        current.set(Calendar.SECOND, workdayStart.get(Calendar.SECOND));
        current.set(Calendar.MILLISECOND, workdayStart.get(Calendar.MILLISECOND));

        current.add(Calendar.MILLISECOND, (int) fractionMillis);

        if (current.get(Calendar.HOUR_OF_DAY) < workdayStart.get(Calendar.HOUR_OF_DAY)) {
            current.set(Calendar.HOUR_OF_DAY, workdayStart.get(Calendar.HOUR_OF_DAY));
        } else if (current.get(Calendar.HOUR_OF_DAY) >= workdayStop.get(Calendar.HOUR_OF_DAY)) {
            current.set(Calendar.HOUR_OF_DAY, workdayStop.get(Calendar.HOUR_OF_DAY));
            current.set(Calendar.MINUTE, workdayStop.get(Calendar.MINUTE));
        }

        return current.getTime();
    }

    private boolean isWorkday(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false;
        }

        Calendar checkDate = (Calendar) date.clone();
        checkDate.set(Calendar.HOUR_OF_DAY, 0);
        checkDate.set(Calendar.MINUTE, 0);
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);

        if (holidays.contains(checkDate)) {
            return false;
        }

        checkDate.set(Calendar.YEAR, 1970);
        if (recurringHolidays.contains(checkDate)){
            return false;
        }

        return true;
    }

    private long getDifferenceInMilliSeconds(Calendar current, int targetHour) {
        Calendar target = (Calendar) current.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long diffInMillis = target.getTimeInMillis() - current.getTimeInMillis();
        return diffInMillis ;
    }

    private boolean isBetweenStartAndStop(Calendar calendar) { // mellom 8 og 16
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int startWorkdayHour = workdayStart.get((Calendar.HOUR_OF_DAY));
        int satrtWorkdayMinute = workdayStart.get((Calendar.MINUTE));
        int endWorkdayHour = workdayStop.get((Calendar.HOUR_OF_DAY));
        int endWorkdayMinute = workdayStop.get((Calendar.MINUTE));
        
        if (hour > startWorkdayHour && hour < endWorkdayHour) {
            return true;
        } else if (hour == startWorkdayHour && minute > satrtWorkdayMinute) {
            return true;
        } else if (hour == endWorkdayHour && minute == endWorkdayMinute) {
            return true;
        }
        return false;
    }

    private boolean isBeforeStart (Calendar calendar) { // foer 8
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int startWorkdayHour = workdayStart.get((Calendar.HOUR_OF_DAY));
        int satrtWorkdayMinute = workdayStart.get((Calendar.MINUTE));
        
        if (hour < startWorkdayHour) {
            return true;
        }  else if (hour == startWorkdayHour && minute == satrtWorkdayMinute) {
            return true;
        } 
        return false;
    }

    private boolean isAfterStop (Calendar calendar) { // etter 16
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int endWorkdayHour = workdayStop.get((Calendar.HOUR_OF_DAY));
        int endWorkdayMinute = workdayStop.get((Calendar.MINUTE));

        if (hour > endWorkdayHour) {
            return true;
        } else if (hour == endWorkdayHour && minute >= endWorkdayMinute) {
            return true;
        } 
        return false;
    }

    public static void main(String[] args) {

        WorkdayCalendar workdayCalendar = new WorkdayCalendar();
        workdayCalendar.setWorkdayStartAndStop(
                new GregorianCalendar(2023, Calendar.JANUARY, 1, 8, 0),
                new GregorianCalendar(2023, Calendar.JANUARY, 1, 16, 0));
        workdayCalendar.setRecurringHoliday(
                new GregorianCalendar(2023, Calendar.MAY, 17, 0, 0));
        workdayCalendar.setHoliday(
                new GregorianCalendar(2023, Calendar.MAY, 27, 0, 0));
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date start = new GregorianCalendar(2023, Calendar.MAY, 24, 7,3 ).getTime();
        float increment =  8.276628f;

        System.out.println(
                f.format(start) + " med tillegg av " +
                        increment + " arbeidsdager er " +
                        f.format(workdayCalendar.getWorkdayIncrement(start, increment)));
    }
}