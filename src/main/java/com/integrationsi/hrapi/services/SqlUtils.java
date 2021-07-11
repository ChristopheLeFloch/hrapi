package com.integrationsi.hrapi.services;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;


public class SqlUtils {

    
    public static String getSqlNudossList(List<Integer> nudossList) {
        // on initialise la liste à -1 car sql developper n'aime pas la liste vide
        String sqlNudossList = "(-1";
        for (int nudoss : nudossList) {
            sqlNudossList=sqlNudossList+','+nudoss;
        }
        sqlNudossList=sqlNudossList + ")";
        return sqlNudossList;
    }

    public static String getSqlCodesFromList(List<String> codes) {
        String codesList = "(NULL";
        if (codes!=null) {
            for (String code : codes) codesList=codesList+","+"'"+code+"'";
        }
        codesList=codesList + ") ";
        return codesList;
    }

	public static String getSqlDateFormat(Date date) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			String day=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day="0"+day;};
			String month=String.valueOf(calendar.get(Calendar.MONTH)+1);
			if (month.length() == 1) {month="0"+month;};
			String year=String.valueOf(calendar.get(Calendar.YEAR));
			
			String dateString=String.valueOf(day)
								+ String.valueOf(month)
								+ String.valueOf(year);
			

			return "to_date('"+dateString+"',"+"'DDMMYYYY')";		
	}
    
    
    

    
}