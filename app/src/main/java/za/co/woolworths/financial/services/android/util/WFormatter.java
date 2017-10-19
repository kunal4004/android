package za.co.woolworths.financial.services.android.util;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;

public class WFormatter {

	public static String formatAmount(int amount) {
		String[] split = String.valueOf((amount / 100)).split("");
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (int i = split.length - 1; i > 0; i--) {
			if (counter != 0 && counter % 3 == 0) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(split[i]);
			counter++;
		}
		return String.format("R %s.%02d", stringBuilder.reverse().toString(), amount % 100);
	}

	public static String newAmountFormat(int amount) {
		String[] split = String.valueOf((amount / 100)).split("");
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (int i = split.length - 1; i > 0; i--) {
			if (counter != 0 && counter % 3 == 0) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(split[i]);
			counter++;
		}
		return String.format("R%s.%02d", stringBuilder.reverse().toString(), amount % 100);
	}

	public static String addSpaceToDate(String value) {
		return value.replaceAll("/", " / ");
	}

	public static String amountFormat(int amount) {
		String[] split = String.valueOf(amount / 100).split("");
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (int i = split.length - 1; i > 0; i--) {
			if (counter != 0 && counter % 3 == 0) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(split[i]);
			counter++;
		}
		return String.format("R%s", stringBuilder.reverse().toString(), amount);
	}

	public static String formatAmount(double amount) {
		String sAmount = roundDouble(amount);
		if (sAmount.contains(",")) {
			sAmount = sAmount.replace(",", ".");
		}
		double mAmount = Double.valueOf(sAmount);
		int mIntAmount = (int) (mAmount * 100);
		String[] split = String.valueOf((mIntAmount / 100)).split("");
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (int i = split.length - 1; i > 0; i--) {
			if (counter != 0 && counter % 3 == 0) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(split[i]);
			counter++;
		}
		return String.format("R %s.%02d", stringBuilder.reverse().toString(), mIntAmount % 100);
	}

	public static String formatAmount(String amount) {
		String sAmount = amount;
		if (sAmount.contains(",")) {
			sAmount = sAmount.replace(",", ".");
		}
		double mAmount = Double.valueOf(sAmount);
		int mIntAmount = (int) (mAmount * 100);
		String[] split = String.valueOf((mIntAmount / 100)).split("");
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (int i = split.length - 1; i > 0; i--) {
			if (counter != 0 && counter % 3 == 0) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(split[i]);
			counter++;
		}
		return String.format("R %s.%02d", stringBuilder.reverse().toString(), mIntAmount % 100);
	}

	public static String formatAmountNoDecimal(int amount) {
		return String.format("R%d", amount / 100);
	}

	public static String formatPercent(int amount) {
		return String.format("%d%%", amount / 100);
	}

	public static String formatVoucher(String voucherNumber) {
		String[] split = voucherNumber.split("");
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (int i = split.length - 1; i > 0; i--) {
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

	public static String formatDateTOddMMMMYYYY(String validFromDate) throws ParseException {
		if (validFromDate == null) {
			return "N/A";
		}
		DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return new SimpleDateFormat("dd MMMM yyyy").format(m_ISO8601Local.parse(validFromDate));
	}

	public static String formatMessagingDate(Date validDate) throws ParseException {
		DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy/MM/dd");
		Date today = new Date();
		long diff = 0;
		try {
			today = m_ISO8601Local.parse(m_ISO8601Local.format(today));

			diff = (today.getTime() - m_ISO8601Local.parse(m_ISO8601Local.format(validDate)).getTime()) / (86400000);
		} catch (Exception e) {
		}

		diff = Math.abs(diff);
		String days = "Today";
		if (diff == 1) {
			days = "Yesterday";
		} else if (diff > 1) {
			days = new SimpleDateFormat("dd MMM").format(validDate);
		}

		return days;
		//final DateFormat formatter = new SimpleDateFormat("dd MM yyyy");


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

}
