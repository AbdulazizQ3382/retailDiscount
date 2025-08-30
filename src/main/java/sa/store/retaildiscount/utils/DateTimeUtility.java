package sa.store.retaildiscount.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtility {


    public static String convertDateToDateTimeFormat(String dateString) {

        if(isDateTimeFormat(dateString)) {
            return dateString;
        }

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        LocalDate localDate = LocalDate.parse(dateString, inputFormatter);

        LocalDateTime localDateTime = localDate.atStartOfDay();

        return localDateTime.format(outputFormatter);

    }


    public static boolean isDateTimeFormat(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setLenient(false);
        try {
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
