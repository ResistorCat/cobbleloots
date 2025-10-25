package dev.ripio.cobbleloots.data.custom.filter;

import dev.ripio.cobbleloots.Cobbleloots;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CobblelootsDateFilter {
    private final String from;
    private final String to;

    public CobblelootsDateFilter(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean test() {
        boolean isFromEmpty = from == null || from.isEmpty();
        boolean isToEmpty = to == null || to.isEmpty();

        if (isFromEmpty && isToEmpty) {
            return true;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            MonthDay currentDay = MonthDay.from(LocalDate.now());

            //Cobbleloots.LOGGER.info("[DEBUG] Date Filter | Current: {} | From: {} | To: {}", currentDay.format(formatter), from, to);

            MonthDay fromDay = !isFromEmpty ? MonthDay.parse(from, formatter) : null;
            MonthDay toDay = !isToEmpty ? MonthDay.parse(to, formatter) : null;

            if (fromDay != null && toDay != null) {
                // Both dates are specified
                if (fromDay.isAfter(toDay)) {
                    // Range wraps around the year (e.g., Dec 1 to Jan 31)
                    return !currentDay.isBefore(fromDay) || !currentDay.isAfter(toDay);
                } else {
                    // Normal range (e.g., Mar 1 to May 31)
                    return !currentDay.isBefore(fromDay) && !currentDay.isAfter(toDay);
                }
            } else if (fromDay != null) {
                // Only 'from' is specified
                return !currentDay.isBefore(fromDay);
            } else if (toDay != null) {
                // Only 'to' is specified
                return !currentDay.isAfter(toDay);
            }

            return true;
        } catch (DateTimeParseException e) {
            Cobbleloots.LOGGER.error("Invalid date format in date filter. Please use 'MM-dd'.", e);
            return false;
        }
    }
}
