package org.jahia.modules.strava.utils;

/**
 * Created by Quentin on 27/09/15.
 */
public class StravaUtils {

    public static String displayMovingTime(String moving_time) {
        int totalSeconds = Integer.parseInt(moving_time);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        String moving_time_display;
        if (hours != 0) {
            moving_time_display = hours + ":" + displayNumberTwoDigits(minutes) + ":" + displayNumberTwoDigits(seconds);
        } else {
            if (minutes != 0) {
                moving_time_display = minutes + ":" + displayNumberTwoDigits(seconds);
            } else {
                moving_time_display = "" + seconds;
            }
        }
        return moving_time_display;
    }

    public static String displayNumberTwoDigits(int number) {
        if (number <= 9) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

}
