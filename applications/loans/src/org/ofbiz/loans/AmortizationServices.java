package org.ofbiz.loans;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
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

/***
 * Computing Armotization
 * 
 * @author Japheth Odonya @when Aug 7, 2014 12:21:09 AM
 * 
 **/
public class AmortizationServices {
	public static int ONEHUNDRED = 100;
	private static int ONE = 1;
	private static String LINEAR = "LINEAR";
	public static String REDUCING_BALANCE = "REDUCING_BALANCE";
	
	public static Logger log = Logger.getLogger(AmortizationServices.class);
	


	/****
	 * For the Loan Application Specified calculate the Armotization Schedule
	 **/
	public static String generateschedule(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		//Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = null;
		
		loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		
		if (loanApplicationId == null)
			loanApplicationId = String.valueOf((Long)request.getAttribute("loanApplicationId"));
		
		log.info("RRRRRRR The real application ID is "+loanApplicationId);
		GenericValue loanApplication = null, loanAmortization;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", Long.valueOf(loanApplicationId)),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		deleteExistingSchedule(delegator, loanApplicationId);

		/**
		 * Given: Loan Amount, Interest Rate and Payment Period Calculate the
		 * Monthly Payment (Straight Line)
		 * 
		 * **/
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		Long loanApplicationIdLog = Long.valueOf(loanApplicationId);
		
		BigDecimal bdTotalRepaidLoan = LoanServices.getLoansRepaidByLoanApplicationId(loanApplicationIdLog);
		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
				//.subtract(bdTotalRepaidLoan);
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
				"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		//openingRepaymentPeriod
		int iRepaymentPeriod;
		//if (loanApplication.getLong("openingRepaymentPeriod") != null){
		//	iRepaymentPeriod = loanApplication.getLong("openingRepaymentPeriod").intValue();
		//} else{
		iRepaymentPeriod = loanApplication.getLong("repaymentPeriod").intValue();
		//}
			
		 
		BigDecimal dbRepaymentPrincipalAmt, bdRepaymentInterestAmt;
		BigDecimal paymentAmount;

		/***
		 * Get Loan Product or Loan Type
		 * */
		GenericValue loanProduct = null;
		String loanProductId = loanApplication.getString("loanProductId");
		loanProductId = loanProductId.replaceAll(",", "");
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Determine the Deduction Type
		String deductionType = null;
		deductionType = loanProduct.getString("deductionType");

		if (deductionType.equals(REDUCING_BALANCE)) {
			paymentAmount = calculateReducingBalancePaymentAmount(dbLoanAmt,
					bdInterestRatePM, iRepaymentPeriod);
		} else {
			paymentAmount = calculateFlatRatePaymentAmount(dbLoanAmt,
					bdInterestRatePM, iRepaymentPeriod);
		}
		// This value will be changing as we go along
		BigDecimal bdPreviousBalance = dbLoanAmt;
		Long loanAmortizationId;

		int iAmortizationCount = 0;

		List<GenericValue> listTobeStored = new LinkedList<GenericValue>();
		
		

		Timestamp repaymentDate = null;
		repaymentDate = loanApplication.getTimestamp("repaymentStartDate");
		
		//Get Insurance Rate
		BigDecimal bdInsuranceRate = getInsuranceRate(loanApplication);
		BigDecimal bdInsuranceAmount;

		while (iAmortizationCount < iRepaymentPeriod) {
			iAmortizationCount++;
			loanAmortizationId = delegator.getNextSeqIdLong("LoanAmortization", 1);

			if (deductionType.equals(REDUCING_BALANCE)){
			bdRepaymentInterestAmt = bdPreviousBalance
					.multiply(bdInterestRatePM);
			} else{
				bdRepaymentInterestAmt = dbLoanAmt
						.multiply(bdInterestRatePM);
			}
			
			dbRepaymentPrincipalAmt = paymentAmount
					.subtract(bdRepaymentInterestAmt);
			
			
				bdPreviousBalance = bdPreviousBalance
					.subtract(dbRepaymentPrincipalAmt);
			
			//Insurance Amount = insuranceRate times balance divide by 100
			bdInsuranceAmount = bdInsuranceRate.multiply(bdPreviousBalance.setScale(6, RoundingMode.HALF_UP)).divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
			loanApplicationId = loanApplicationId.replaceAll(",", "");
			loanAmortization = delegator.makeValue("LoanAmortization", UtilMisc
					.toMap("loanAmortizationId", loanAmortizationId,
							"paymentNo", new Long(iAmortizationCount)
									.longValue(), "loanApplicationId",
							Long.valueOf(loanApplicationId), "paymentAmount", paymentAmount
									.setScale(6, RoundingMode.HALF_UP),
							"interestAmount", bdRepaymentInterestAmt.setScale(
									6, RoundingMode.HALF_UP),
									
							"insuranceAmount", bdInsuranceAmount,
							"principalAmount", dbRepaymentPrincipalAmt
									.setScale(6, RoundingMode.HALF_UP),
							"balanceAmount", bdPreviousBalance.setScale(6,
									RoundingMode.HALF_UP),
							"expectedPaymentDate", repaymentDate,
							"isAccrued", "N",
							"isPaid", "N"
					));
			listTobeStored.add(loanAmortization);

			repaymentDate = calculateNextPaymentDate(repaymentDate);
		}
		
		
		// Save the list
		try {
			delegator.storeAll(listTobeStored);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		
		
		updateLoanApplicationRepayments(delegator, loanApplicationId, paymentAmount, iRepaymentPeriod);
		Writer out;
		try {
			out = response.getWriter();
			out.write("");
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * Get Insurance Rate
	 * 
	 * **/
	public static BigDecimal getInsuranceRate(GenericValue loanApplication) {
		
		//Get the ID for Insurance in ProductCharge
		Delegator delegator = loanApplication.getDelegator();
		List<GenericValue> productChargeELI = null;
		String strName = "Insurance";
		try {
			productChargeELI = delegator.findList("ProductCharge",
					EntityCondition.makeCondition("name", strName), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (productChargeELI == null) {
			return BigDecimal.ZERO;
		}
		String productChargeId = "";
		BigDecimal bdInsuranceRate = BigDecimal.ZERO;
		for (GenericValue genericValue : productChargeELI) {
			//productChargeId
			productChargeId = genericValue.get("productChargeId").toString();
			//			bdTotalSavings = bdTotalSavings.add(AccHolderTransactionServices
			//					.getTotalSavings(genericValue.get("memberAccountId")
			//							.toString(), delegator));
		}
		
		//Get the Charge Amount for InsuranceRate in the Loan_Product_Charge
		//LoanProductCharge
		List<GenericValue> loanProductChargeELI = null;
		productChargeId = productChargeId.replaceAll(",", "");
		EntityConditionList<EntityExpr> chargesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"productChargeId", EntityOperator.EQUALS,
						Long.valueOf(productChargeId)), EntityCondition
						.makeCondition("loanProductId",
								EntityOperator.EQUALS, loanApplication.getLong("loanProductId"))),
						EntityOperator.AND);
		
		try {
			loanProductChargeELI = delegator.findList("LoanProductCharge",
					chargesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanProductCharge : loanProductChargeELI) {
			bdInsuranceRate = loanProductCharge.getBigDecimal("rateAmount");
		}
		
		return bdInsuranceRate;
	}

	/***
	 * Calculate the Per Month Payment, Assuming Straight Line
	 * 
	 * @author Japheth Odonya @when Aug 7, 2014 12:21:55 AM
	 * 
	 * */
	public static BigDecimal calculateReducingBalancePaymentAmount(
			BigDecimal loanAmt, BigDecimal interestRatePM, int repaymentPeriod) {

		BigDecimal bdPaymentAmount;
		// paymentAmount = (interestRatePM.multiply(loanAmt)).multiply((new
		// BigDecimal(1).add(interestRatePM.divide(new
		// BigDecimal(100)))).pow(repaymentPeriod));

		BigDecimal bdInterestByPrincipal = interestByPrincipal(interestRatePM,
				loanAmt);

		BigDecimal bdOnePlusInterestPowerPeriod = onePlusInterestPowerPeriod(
				interestRatePM, repaymentPeriod);

		BigDecimal bdOnePlusInterestPowerPeriodMinusOne = onePlusInterestPowerPeriodMinusOne(
				interestRatePM, repaymentPeriod);

		//This is for loans where interest is zero
		if (bdOnePlusInterestPowerPeriodMinusOne.compareTo(BigDecimal.ZERO) == 0){
			bdOnePlusInterestPowerPeriodMinusOne = new BigDecimal(1);
		}
		
		// paymentAmount = interestByPrincipal timesOnePlusInterestPowerPeriod
		// divideOnePlusInterestMinusOne
		bdPaymentAmount = bdInterestByPrincipal.multiply(
				bdOnePlusInterestPowerPeriod).divide(
				bdOnePlusInterestPowerPeriodMinusOne, 6, RoundingMode.HALF_UP);

		return bdPaymentAmount;
	}

	/**
	 * @author Japheth Odonya @when Aug 10, 2014 1:04:44 PM Calculcate Repayment
	 *         Amount for Flat Rate Loan Repayment Method
	 * */
	public static BigDecimal calculateFlatRatePaymentAmount(
			BigDecimal loanAmt, BigDecimal interestRatePM, int repaymentPeriod) {
		BigDecimal bdPaymentAmount;

		bdPaymentAmount = ((interestRatePM.multiply(loanAmt)
				.multiply(new BigDecimal(repaymentPeriod))).add(loanAmt))
				.divide(new BigDecimal(repaymentPeriod),  RoundingMode.HALF_UP);

		return bdPaymentAmount;
	}

	/***
	 * Product of Interest Rate and Principal
	 * */
	private static BigDecimal interestByPrincipal(BigDecimal interestRatePM,
			BigDecimal loanAmt) {
		return interestRatePM.multiply(loanAmt);
	}

	/***
	 * One Plus Interest Power Period
	 * */
	private static BigDecimal onePlusInterestPowerPeriod(
			BigDecimal interestRatePM, int repaymentPeriod) {
		return (interestRatePM.add(new BigDecimal(ONE))).pow(repaymentPeriod);
	}

	/***
	 * One Plus Interest Power Period Minus ONE
	 * **/
	private static BigDecimal onePlusInterestPowerPeriodMinusOne(
			BigDecimal interestRatePM, int repaymentPeriod) {
		return ((new BigDecimal(ONE).add(interestRatePM)).pow(repaymentPeriod))
				.subtract(new BigDecimal(ONE));
	}

	/***
	 * Delete the existing Armotization schedule for the regenetaion to happen
	 * */
	private static void deleteExistingSchedule(Delegator delegator,
			String loanApplicationId) {
		// Get the loan armotization entities for the loan application and
		// delete them
		log.info("######## Tyring to Delete ######## !!!");
		List<GenericValue> loanAmortizationELI = null; // =
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanAmortizationELI = delegator.findList("LoanAmortization",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<GenericValue> toDeleteList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanAmortizationELI) {
			toDeleteList.add(genericValue);
		}

		try {
			delegator.removeAll(toDeleteList);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	/***
	 * @author Japheth Odonya @when Aug 8, 2014 6:33:38 PM Add one month
	 **/
	public static Timestamp calculateNextPaymentDate(Timestamp repaymentDate) {
		LocalDateTime localRepaymentDate = new LocalDateTime(
				repaymentDate.getTime());
		localRepaymentDate = localRepaymentDate.plusMonths(1);

		// repaymentDate = localRepaymentDate.;
		repaymentDate = new Timestamp(localRepaymentDate.toDate().getTime());
		return repaymentDate;
	}
	
	/**
	 * @author Japheth Odonya  @when Aug 10, 2014 2:39:57 PM
	 * Update Loan Application with the Total Repayment Amount and the Per Month Amount 
	 * */
	public static void updateLoanApplicationRepayments(Delegator delegator, String loanApplicationId, BigDecimal paymentAmount, int iRepaymentPeriod){
		//Update Loan 
		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll("", "");
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		loanApplication.set("monthlyRepayment", paymentAmount.setScale(
				6, RoundingMode.HALF_UP));
		
		
		//Calculate Total Repayment Amount
		BigDecimal bdTotalRepayment = (paymentAmount.multiply(new BigDecimal(iRepaymentPeriod))).setScale(
				6, RoundingMode.HALF_UP);
		loanApplication.set("totalRepayment", bdTotalRepayment);
		
		try {
			delegator.store(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
