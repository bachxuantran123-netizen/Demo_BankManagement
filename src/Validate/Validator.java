package src.Validate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Validator {
    private static final String REGEX_ACCOUNT_CODE = "^\\d{9}$";
    private static final String REGEX_DATE = "^\\d{2}/\\d{2}/\\d{4}$";

    public static boolean isValidAccountCode(String code) {
        return code != null && code.matches(REGEX_ACCOUNT_CODE);
    }

    public static boolean isPositiveNumber(String strNum) {
        try {
            double value = Double.parseDouble(strNum);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || !dateStr.matches(REGEX_DATE))
            return false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isFutureDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date inputDate = sdf.parse(dateStr);
            Date today = new Date();
            return inputDate.after(today);
        } catch (ParseException e) {
            return false;
        }
    }
}
