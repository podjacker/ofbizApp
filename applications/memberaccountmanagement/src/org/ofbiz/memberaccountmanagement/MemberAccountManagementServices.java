package org.ofbiz.memberaccountmanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class MemberAccountManagementServices {
	
	private static Logger log = Logger
			.getLogger(MemberAccountManagementServices.class);

	
	public static String getMemberAccounts(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId = (String) request.getParameter("partyId");
		partyId = partyId.replaceAll(",", "");
		List<GenericValue> memberAccountELI = null;
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId))
						),
						EntityOperator.AND);
		try {
			memberAccountELI = delegator.findList("MemberAccount", memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (memberAccountELI == null){
			result.put("", "No Accounts");
		}
		
		GenericValue accountProduct;
		for (GenericValue genericValue : memberAccountELI) {
			accountProduct = LoanUtilities.getAccountProduct(genericValue.getLong("accountProductId"));
			result.put(genericValue.get("memberAccountId").toString(), genericValue.get("accountNo")+" - "+accountProduct.getString("name")+" - "+genericValue.getString("accountName"));
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}
	
	
	//Member Account Voucher 
	/***
	 * Add a decrease for sourceMemberAccountId
	 * 
	 * Add an increase for destMemberAccountId
	 * 
	 * Amount is amount
	 * 
	 * */
	public static String createMemberAccountVoucherTransaction(Map<String, String> userLogin, Long memberAccountVoucherId){
		//Find MemberAccountVoucher
		GenericValue memberAccountVoucher = getMemberAccountVoucher(memberAccountVoucherId);
		//Decrease the source
		log.info("//////////////////////// Posting the voucher");
		BigDecimal amount = memberAccountVoucher.getBigDecimal("amount");
		Long memberAccountId = memberAccountVoucher.getLong("sourceMemberAccountId");
		String transactionType = "MEMBERACCOUNTJVDEC";
		
		//AccHolderTransactionServices.cashDeposit(amount, memberAccountId, userLogin, transactionType);
		
		AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, memberAccountVoucherId);
		memberAccountId = memberAccountVoucher.getLong("destMemberAccountId");
		transactionType = "MEMBERACCOUNTJVINC";

		AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, memberAccountVoucherId);

		log.info("//////////////////////// Posted the voucher");
		
		
		return "success";
	}
	
	
	private static GenericValue getMemberAccountVoucher(Long memberAccountVoucherId) {
		GenericValue memberAccountVoucher = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountVoucher = delegator.findOne("MemberAccountVoucher",
					UtilMisc.toMap("memberAccountVoucherId", memberAccountVoucherId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return memberAccountVoucher;
	}


}
