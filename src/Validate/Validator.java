package src.Validate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Validator {
    // Giữ nguyên các REGEX chuẩn của em
    private static final String REGEX_ACCOUNT_CODE = "^\\d{9}$";
    private static final String REGEX_DATE = "^\\d{2}/\\d{2}/\\d{4}$";

    // 1. Kiểm tra Mã tài khoản (Chỉ trả về đúng/sai)
    public static boolean isValidAccountCode(String code) {
        return code != null && code.matches(REGEX_ACCOUNT_CODE);
    }

    // 2. Kiểm tra Số dương (Dùng cho tiền, lãi suất...)
    public static boolean isPositiveNumber(String strNum) {
        try {
            double value = Double.parseDouble(strNum);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 3. Kiểm tra định dạng Ngày tháng
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || !dateStr.matches(REGEX_DATE)) return false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false); // Bắt buộc ngày phải tồn tại (vd: không có ngày 30/02)
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // 4. Kiểm tra chuỗi rỗng
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
