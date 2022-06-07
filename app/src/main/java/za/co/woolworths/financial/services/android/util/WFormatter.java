package za.co.woolworths.financial.services.android.util;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;

import static com.google.common.base.Preconditions.checkArgument;

public class WFormatter {

    public static final String DATE_FORMAT_EEEE_COMMA_dd_MMMM  = "EEEE, dd MMMM";

    public static String addSpaceToDate(String value) {
        return value.replaceAll("/", " / ");
    }

    public static String amountFormat(int amount) {
        String[] split = String.valueOf(amount / 100).split("");
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;
        for (int i = split.length - 1; i >= 0; i--) {
            if (counter != 0 && counter % 3 == 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(split[i]);
            counter++;
        }
        return Utils.removeNegativeSymbol(String.format("R%s", stringBuilder.reverse().toString(), amount));
    }

    public static String formatAmountNoDecimal(int amount) {
        return Utils.removeNegativeSymbol(String.format("R%d", amount / 100));
    }

    public static String formatPercent(int amount) {
        return String.format("%d%%", amount / 100);
    }

    public static String formatVoucher(String voucherNumber) {
        String[] split = voucherNumber.split("");
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;
        for (int i = split.length - 1; i >= 0; i--) {
            if (counter != 0 && counter % 4 == 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(split[i]);
            counter++;
        }
        return stringBuilder.reverse().toString();
    }

    public static String formatDate(String validFromDate) throws ParseException {
        if (validFromDate == null) {
            return "N/A";
        }
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return new SimpleDateFormat("dd/MM/yyyy").format(m_ISO8601Local.parse(validFromDate));
    }


    public static String newDateFormat(String validFromDate) throws ParseException {
        if (validFromDate == null) {
            return "N/A";
        }
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return new SimpleDateFormat("dd/MM/yy").format(m_ISO8601Local.parse(validFromDate));
    }

    public static String formatDateTOddMMMYYYY(String validFromDate) throws ParseException {
        if (TextUtils.isEmpty(validFromDate)) {
            return "";
        }
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd");
        return new SimpleDateFormat("dd MMM yyyy").format(m_ISO8601Local.parse(validFromDate));
    }

    public static String formatMessagingDate(Date validDate) throws ParseException {
        long diff = getDateDiff(validDate);
        String day = "Today";
        if (diff == 1) {
            day = "Yesterday";
        } else if (diff > 1) {
            day = new SimpleDateFormat("dd MMM").format(validDate);
        }
        return day;
    }

    private static long getDateDiff(Date validDate) {
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy/MM/dd");
        Date today = new Date();
        long diff = 0;
        try {
            today = m_ISO8601Local.parse(m_ISO8601Local.format(today));

            diff = (today.getTime() - m_ISO8601Local.parse(m_ISO8601Local.format(validDate)).getTime()) / (86400000);
        } catch (Exception e) {
        }

        return Math.abs(diff);
    }

    public static String formatMeter(double meter) {
        double km = meter * .001;
        String metertToKm = String.valueOf(new DecimalFormat("##.#").format(km));
        return metertToKm;
    }

    public static String formatOfferingString(List<StoreOfferings> offerings) {

        String offeringString = TextUtils.join(" \u2022 ", offerings);
        return offeringString;
    }

    public static String formatOpenUntilTime(String openTime) {
        String resultTime;
        if (openTime.contains("-")) {
            String[] splitTime = openTime.split("-");
            resultTime = splitTime[1].trim();
        } else {
            resultTime = openTime;
        }
        return resultTime;
    }

    public static String roundDouble(double value) {
        DecimalFormat myFormatter = new DecimalFormat("00.00");
        return myFormatter.format(value);
    }

    public static String formatOrdersDate(String validFromDate) throws ParseException {
        if (validFromDate == null) {
            return "N/A";
        }
        DateFormat m_ISO8601Local = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return new SimpleDateFormat("dd MMMM yyyy").format(m_ISO8601Local.parse(validFromDate));
    }

    public static String formatStatementsDate(String validFromDate) throws ParseException {
        if (validFromDate == null) {
            return "N/A";
        }
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd");
        return new SimpleDateFormat("MMMM yyyy").format(m_ISO8601Local.parse(validFromDate));
    }

    public static Date parseDate(String date) {

        SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm");
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    public static Date convertStringToDate(String date) throws ParseException {
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd");
        return m_ISO8601Local.parse(date);
    }

    public static long checkIfDateisTomorrow(String date) throws ParseException {
        return getDateDiff(convertStringToDate(date));
    }

    public static String convertDayShortToLong(String day) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(day);
        return new SimpleDateFormat("EEEE").format(date);
    }

    public static String convertDayToShortDay(String day) throws ParseException {
        if (TextUtils.isEmpty(day)) return "";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(day);
        return new SimpleDateFormat("EEE dd MMM").format(date);
    }

    public static String convertMonthShortToLong(String date) throws ParseException {
        return (new SimpleDateFormat("LLLL", Locale.getDefault())).format(convertStringToDate(date));
    }

    public static String getDayAndFullDate(String date) throws ParseException {
        if (date == null)
            return "";
        return new SimpleDateFormat("EEEE dd MMMM, yyyy")
                .format((new SimpleDateFormat("yyyy-MM-dd"))
                        .parse(date));
    }

    public static String getDayAndFormatedDate(String date) throws ParseException {
        if (date == null)
            return "";
        return new SimpleDateFormat("EE, dd MMMM").format((new SimpleDateFormat("yyyy-MM-dd")).parse(date));
    }

    /**
     *
     * @param inputDate Input string date in format of yyyy-MM-dd
     * @param returnDateFormat Format of date value in which you want to returned by this function. Converted string schema
     *               eg. EEEE, dd MMMM
     * @return formatted string as per parameter format
     * @throws ParseException
     */
    public static String convertDateToFormat(String inputDate, String returnDateFormat) throws ParseException {
        if (TextUtils.isEmpty(inputDate))
            return "";
        return new SimpleDateFormat(returnDateFormat).format((new SimpleDateFormat("yyyy-MM-dd")).parse(inputDate));
    }

    public static String getDayOfMonthSuffix(final int day) {
        checkArgument(day >= 1 && day <= 31, "illegal day of month: " + day);
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String getFullMonthDate(String date) throws ParseException {
        if (date == null)
            return "";
        return new SimpleDateFormat("dd MMMM yyyy")
                .format((new SimpleDateFormat("yyyy-MM-dd"))
                        .parse(date));
    }

    public static String getFullMonthWithDate(String date) throws ParseException {
        if (date == null)
            return "";
        return new SimpleDateFormat("dd MMMM")
                .format(new SimpleDateFormat("EE, dd MMMM").parse(date));
    }

    public static String convertToFormatedDate(String date) throws ParseException {
        if (date == null)
            return "";
        return new SimpleDateFormat("EEE dd MMM")
                .format((new SimpleDateFormat("yyyy-MM-dd"))
                        .parse(date));
    }
}
