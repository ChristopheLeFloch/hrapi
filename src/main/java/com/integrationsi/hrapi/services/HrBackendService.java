package com.integrationsi.hrapi.services;


import com.hraccess.openhr.IHRUser;
import com.hraccess.openhr.msg.HRMsgDressSqlStatement;
import com.hraccess.openhr.msg.HRResultDressSqlStatement;


public class HrBackendService {

	public static String getDressingCode(IHRUser u, String role, String parameter, String activity, String cdstdo) {
		  HRMsgDressSqlStatement request = new HRMsgDressSqlStatement();
	        request.addStatement(cdstdo, "select A.NUDOSS from " + cdstdo + "00 A"); // Data extraction order
	        request.setRoleTemplate(role);
	        request.setRoleParameter(parameter);
	        request.setActivity(activity);

	        // Sending message via the session (synchronous task)
	        final StringBuilder sb = new StringBuilder();
	        HRResultDressSqlStatement result = (HRResultDressSqlStatement) u.getSession().sendMessage(request);

	        result.getStatementsAsList().forEach(s -> sb.append(s));
	        return sb.toString();
	}


}
