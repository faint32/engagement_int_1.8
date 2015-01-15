package com.netease.util.date;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

class ISO8601DateFormat extends DateFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public StringBuffer format(Date date, StringBuffer buffer,
			FieldPosition field) {
		String rfc822 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
		return buffer.append(rfc822.substring(0, rfc822.length() - 2))
			.append(':').append(rfc822.substring(rfc822.length() - 2));
	}

	@Override
	public Date parse(String str, ParsePosition position) {
		int index = position.getIndex();
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		int year = 1900;
		int month = 1;
		int day = 1;
		int hour = 0;
		int minute = 0;
		int second = 0;
		
		int thour = 0;
		int tminute = 0;
		
		final int len = str.length() - index;
		if (len != 4 && len != 7 && len != 10 && len != 17 && len != 20
				&& len < 22) {
			throw new IllegalArgumentException("");
		}

		int num = 0, sign = 1;
		int start, end, log;
		char ch;
		end = 4;
		start = 0;
		log = 0;
		while (start < len) {
			ch = str.charAt(start++);
			if (!Character.isDigit(ch)) {
				throw new IllegalArgumentException(Integer.toString(start - 1));
			}
			num = 10 * num + ((int) (ch - '0'));
			if (start == end) {
				switch (log) {
				case 0:
					year = num;
					break;
				case 1:
					month = num;
					break;
				case 2:
					day = num;
					break;
				case 3:
					hour = num;
					break;
				case 4:
					minute = num;
					break;
				case 5:
					second = num;
					break;
				case 6:
					break;
				case 7:
					thour = sign * num;
					break;
				case 8:
					tminute = num;
					break;
				default:
					break;
				}
				num = 0;
				if (start == len)
					break;
				switch (log) {
				case 0:
				case 1:
					if (str.charAt(start++) != '-')
						break;
					end = start + 2;
					break;
				case 2:
					if (str.charAt(start++) != 'T')
						break;
					end = start + 2;
					break;
				case 3:
				case 7:
					if (str.charAt(start++) != ':')
						break;
					end = start + 2;
					break;
				case 4:
					ch = str.charAt(start++);
					if (ch == ':') {
						end = start + 2;
					} else if (ch == '+' || ch == '-') {
						sign = (ch == '-') ? -1 : 1;
						log += 2;
						end = start + 2;
					} else if (ch == 'Z') {
						break;
					} else
						break;
					break;
				case 5:
					ch = str.charAt(start++);
					if (ch == '.') {
						end = start;
						while (Character.isDigit(str.charAt(++end)))
							/* NOP */;
					} else if (ch == '+' || ch == '-') {
						sign = (ch == '-') ? -1 : 1;
						log += 1;
						end = start + 2;
					} else if (ch == 'Z') {
						break;
					} else
						break;
					break;
				case 6:
					ch = str.charAt(start++);
					if (ch == '+' || ch == '-') {
						sign = (ch == '-') ? -1 : 1;
						end = start + 2;
					} else if (ch == 'Z') {
						break;
					} else
						break;
					break;
				// case 8:
				default:
					break;
				}
				++log;
			}
		}
		position.setIndex(start);
		TimeZone timezone = TimeZone.getTimeZone("GMT+" + thour + ":" + (tminute < 10 ? "0" + tminute : tminute));
		calendar.setTimeZone(timezone);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		
		return calendar.getTime();
	}

}
