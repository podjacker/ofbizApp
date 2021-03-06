package restcomponent;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.accountholdertransactions.Transaction;
import org.ofbiz.accountholdertransactions.model.ATMTransaction;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.msaccomanagement.MSaccoManagementServices;
import org.ofbiz.msaccomanagement.MSaccoStatus;

import restcomponent.model.Account;
import restcomponent.model.Application;
import restcomponent.model.Loan;
import restcomponent.model.MSaccoTransaction;
import restcomponent.model.QueryAccount;
import restcomponent.model.Results;
import restcomponent.model.TransactionResult;

import com.google.gson.Gson;

/***
 * @author Japheth Odonya @when Nov 13, 2014 9:00:17 PM MSacco Services
 * 
 *         MSacco Services - Balance - Withdrawal - Deposit - Loan Repayment
 * */
@Path("/msacco")
public class MSaccoServices {

	public static String QUERY_TYPE_NONE = "none";
	public static String QUERY_TYPE_ACCOUNTS = "ACCOUNTS";
	public static String QUERY_TYPE_ACCOUNT_BALANCES = "ACCOUNT_BALANCES";
	public static String QUERY_TYPE_LOANS = "LOANS";
	public static String QUERY_TYPE_MINISTATEMENT = "MINISTATEMENT";

	public static String TRANSACTION_LOANREPAYMENT = "Loan_Repayment";
	public static String TRANSACTION_WITHDRAWAL = "Withdrawal";
	public static String TRANSACTION_DEPOSIT = "Deposit ";
	
	private static Logger log = Logger
			.getLogger(MSaccoServices.class);

	/****
	 * Gets the Members with new MSacco Applications - Not sent to Cortec
	 * */
	@GET
	@Produces("application/json")
	@Path("/registration")
	public Response getRegistration() {
		

		// Get the MSaccoApplications that are active and have not been sent to
		// provider (Coretech) - sent is N
		List<GenericValue> listMsaccoApplicationELI = null;
		Long activeStatusId = MSaccoManagementServices
				.getCardStatusId("ACTIVE");

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> msaccoApplicationELI = null;
		EntityConditionList<EntityExpr> msaccoApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"cardStatusId", EntityOperator.EQUALS, activeStatusId),
						EntityCondition.makeCondition("sent",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);

		try {
			msaccoApplicationELI = delegator.findList("MSaccoApplication",
					msaccoApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		int maxSize = 10;
		
		if ((msaccoApplicationELI != null) && (msaccoApplicationELI.size() > 0)){
			if (msaccoApplicationELI.size() < 10){
				maxSize = msaccoApplicationELI.size();
			}
		}
		
		
		if ((msaccoApplicationELI != null) && (msaccoApplicationELI.size() > 0)){
			msaccoApplicationELI = msaccoApplicationELI.subList(0, maxSize);
		}

		// GenericValue msaccoApplication = null;
		Application application = null;
		List<Application> listApplication = new ArrayList<Application>();
		for (GenericValue genericValue : msaccoApplicationELI) {

			application = new Application();
			application.setTelephone(genericValue
					.getString("mobilePhoneNumber"));
			application.setType("New_Application");
			application.setApplicantName(AccHolderTransactionServices
					.getMemberNames(genericValue.getLong("partyId")));
			application.setAccounts(getMemberAccounts(genericValue
					.getLong("partyId")));
			listApplication.add(application);
		}
		
		for (GenericValue msaccoapp : msaccoApplicationELI) {
			addServiceLog(msaccoapp.getString("mobilePhoneNumber"), "Query Registration", null, null);
			
			//sent
			msaccoapp.set("sent", "Y");
			
			try {
				delegator.createOrStore(msaccoapp);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		addServiceLog("", "Registration Call", null, null);
		
		

		Gson gson = new Gson();
		String json = gson.toJson(listApplication);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/query/{phoneNumber}/{type}")
	public Response query(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("type") String type) {

		QueryAccount queryAccount = new QueryAccount();
		queryAccount.setTelephoneNo(phoneNumber);
		queryAccount.setQueryType(type);

		if (type.equalsIgnoreCase(QUERY_TYPE_NONE)) {
			// Process for NONE
		} else if (type.equalsIgnoreCase(QUERY_TYPE_ACCOUNTS)) {
			// process for ACCOUNTS
			queryAccount.setListBalances(addAccountBalancesNoCharging(phoneNumber));
			addServiceLog(phoneNumber, "Query Accounts", null, null);
		} else if (type.equalsIgnoreCase(QUERY_TYPE_ACCOUNT_BALANCES)) {
			// process for BALANCES
			queryAccount.setListBalances(addAccountBalances(phoneNumber));
			addServiceLog(phoneNumber, "Query Balances", null, null);
		} else if (type.equalsIgnoreCase(QUERY_TYPE_LOANS)) {
			
			// process for loans
			queryAccount.setListLoans(addLoans(phoneNumber));
			addServiceLog(phoneNumber, "Query Loans", null, null);
		} else if (type.equalsIgnoreCase(QUERY_TYPE_MINISTATEMENT)) {
			// process for MINISTATEMENT
			queryAccount.setListMinistatement(addMinistatement(phoneNumber));
			addServiceLog(phoneNumber, "Query Ministatement", null, null);
		}

		Results results = new Results();
		results.setResultCode("0");
		results.setResultDesc("Success");

		// queryAccount.

		queryAccount.setResults(results);

		Gson gson = new Gson();
		String json = gson.toJson(queryAccount);

		return Response.ok(json).type("application/json").build();
	}

	private List<Transaction> addMinistatement(String phoneNumber) {

		Long memberId = MSaccoManagementServices.getMemberId(phoneNumber);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId))), EntityOperator.AND);
		List<GenericValue> accountTransactionELI = null;
		List<String> orderByList = new ArrayList<String>();
		orderByList.add("createdStamp DESC");
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Transaction transaction;

		/***
		 * public String transactionType; public BigDecimal transactionAmount;
		 * public Timestamp createdStamp;
		 * 
		 * public String accountNo; public String accountName;
		 * 
		 * **/
		GenericValue memberAccount = null;

		List<Transaction> listTransactions = new ArrayList<Transaction>();
		Timestamp dateCreated;

		for (GenericValue genericValue : accountTransactionELI) {
			transaction = new Transaction();

			transaction.setTransactionType(genericValue
					.getString("transactionType"));
			transaction.setTransactionAmount(genericValue
					.getBigDecimal("transactionAmount"));

			// genericValue.getT
			dateCreated = new Timestamp(genericValue.getTimestamp(
					"createdStamp").getTime());
			DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
			transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));
			try {
				memberAccount = delegator
						.findOne("MemberAccount", UtilMisc.toMap(
								"memberAccountId",
								genericValue.getLong("memberAccountId")), false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			transaction.setAccountName(memberAccount.getString("accountName"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			listTransactions.add(transaction);
		}

		return listTransactions;
	}

	private List<Loan> addLoans(String phoneNumber) {
		// TODO Auto-generated method stub
		// LoanServices.getLoansRepaid(memberId)
		// Get loans in disbursement given a memberId
		Long memberId = MSaccoManagementServices.getMemberId(phoneNumber);

		// Get Loan Applications for the memberId with Status DISBURSED
		List<GenericValue> listLoanApplicationELI = getDisbursedLoans(memberId);
		List<Loan> listLoan = new ArrayList<Loan>();
		Loan loan = null;
		for (GenericValue genericValue : listLoanApplicationELI) {
			loan = new Loan();
			loan.setLoanNo(genericValue.getString("loanNo"));
			loan.setLoanAmt(genericValue.getString("loanAmt"));
			loan.setLoanType(getLoanType(genericValue.getLong("loanProductId")));
			loan.setLoanBalance(LoansProcessingServices
					.getTotalLoanBalancesByLoanApplicationId(
							genericValue.getLong("loanApplicationId"))
					.doubleValue());
			listLoan.add(loan);
		}
		return listLoan;
	}

	private String getLoanType(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return loanProduct.getString("name");
	}

	private List<GenericValue> getDisbursedLoans(Long memberId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> listLoanApplicationELI = null;
		Long disbursedStatusId = LoanServices.getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						disbursedStatusId), EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId)),
						EntityOperator.AND);

		try {
			listLoanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return listLoanApplicationELI;
	}

	private List<Account> addAccountBalances(String phoneNumber) {
		// String partyId
		// MSaccoManagementServices.getMSaccoAccount(phoneNumber)
		// MSaccoManagementServices.getMemberAccountId(phoneNumber)
		// AccHolderTransactionServices.getM
		Long memberAccountId = null;
		memberAccountId = MSaccoManagementServices
				.getMemberAccountId(phoneNumber);
		List<Account> listAccount = new ArrayList<Account>();
		Account account = new Account();
		account.setTelephoneNo(phoneNumber);
		account.setQuery_type("accounts");
		
		List<Long> listMemberAccountId = new ArrayList<Long>();
		//AccHolderTransactionServices.

		if (memberAccountId != null) {
			//Post MSacco Enquiry Charge
			BigDecimal bdMsaccoEnquiryChargeAmt = new BigDecimal(10);
			BigDecimal bdMsaccoEnquiryExcise = new BigDecimal(1);
			Long partyId = LoanUtilities.getMemberAccount(memberAccountId).getLong("partyId");
			String memberBranchId = LoanUtilities.getMemberBranchId(partyId.toString());
			String acctgTransId = AccHolderTransactionServices.creatAccountTransRecordMsaccoEnquiry(null, null, partyId, memberBranchId);
			String glAccountTypeId = "MEMBER_DEPOSIT";
			String memberPartyId = LoanUtilities
					.getMemberPartyIdFromMemberAccountId(memberAccountId);

			AccHolderTransactionServices.memberTransactionDeposit(
					bdMsaccoEnquiryChargeAmt, memberAccountId, null,
					"MSACCOENQUIRY", null, null,
					acctgTransId, null, null);
			//Post MSacco Enquiry Excise
			AccHolderTransactionServices.memberTransactionDeposit(
					bdMsaccoEnquiryExcise, memberAccountId, null,
					"MSACCOENQUIRYEXCISE", null, null,
					acctgTransId, null, null);
			
			
			//Post to the GL
			
			//Dr Member Savings
			Long entrySequence = 0L;
			entrySequence = entrySequence + 1;
			String savingsAccountGLAccountId = LoanUtilities
					.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
			BigDecimal bdTotalBalanceCharge = bdMsaccoEnquiryChargeAmt.add(bdMsaccoEnquiryExcise);
			AccHolderTransactionServices.createAccountPostingEntry(
					bdTotalBalanceCharge, acctgTransId, "D",
					savingsAccountGLAccountId, entrySequence.toString(), memberBranchId);
			
			//Cr Msacco commission
			entrySequence = entrySequence + 1;
			
			String msaccoCommissionAccountId = getMsaccoCommissionChargeAccount();
			AccHolderTransactionServices.createAccountPostingEntry(
					bdMsaccoEnquiryChargeAmt, acctgTransId, "C",
					msaccoCommissionAccountId, entrySequence.toString(), memberBranchId);
			
			//Credit Excise
			entrySequence = entrySequence + 1;
			String exciseDutyAccountId = getMsaccoExciseDutyAccount();
			AccHolderTransactionServices.createAccountPostingEntry(
					bdMsaccoEnquiryExcise, acctgTransId, "C",
					exciseDutyAccountId, entrySequence.toString(), memberBranchId);
			
			listMemberAccountId = AccHolderTransactionServices.getMemberAccountIdsAll(AccHolderTransactionServices
					.getMemberAccount(memberAccountId).getLong("partyId"));
			
			//getMemberAccountIdsWithdrawable
			
//			listMemberAccountId = AccHolderTransactionServices.getMemberAccountIdsWithdrawable(AccHolderTransactionServices
//					.getMemberAccount(memberAccountId).getLong("partyId"));
//			

			for (Long currentMemberAccountId : listMemberAccountId) {
				
				
				account = new Account();
				account.setTelephoneNo(phoneNumber);
				account.setQuery_type("accounts");
				
				GenericValue memberAccount = AccHolderTransactionServices
						.getMemberAccount(currentMemberAccountId);

				account.setAccountNo(memberAccount.getString("accountNo"));
				account.setAccountName(memberAccount.getString("accountName"));
				account.setAccountType(AccHolderTransactionServices.getAccountProductName(currentMemberAccountId));
				String memberAccountIdString = currentMemberAccountId.toString();
				memberAccountIdString = memberAccountIdString.replaceFirst(",", "");
				account.setAccountBalance(AccHolderTransactionServices
						.getTotalBalanceNow(memberAccountIdString).doubleValue());
				listAccount.add(account);
				
			}
			
		}

		
		return listAccount;

	}

	private String getMsaccoExciseDutyAccount() {
		// TODO Auto-generated method stub
		//msaccoCommissionAccountId
		//Msacco Enquiry Commission
		List<GenericValue> chargesELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			chargesELI = delegator.findList("ProductCharge",
					EntityCondition.makeCondition("name", "Excise Duty"), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		String chargeAccountId = "";
		for (GenericValue genericValue : chargesELI) {
			chargeAccountId = genericValue.getString("chargeAccountId");
		}
		return chargeAccountId;
	}

	private String getMsaccoCommissionChargeAccount() {
		// TODO Auto-generated method stub
		//msaccoCommissionAccountId
		//Msacco Enquiry Commission
		List<GenericValue> chargesELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			chargesELI = delegator.findList("ProductCharge",
					EntityCondition.makeCondition("name", "Msacco Enquiry Commission"), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		String chargeAccountId = "";
		for (GenericValue genericValue : chargesELI) {
			chargeAccountId = genericValue.getString("chargeAccountId");
		}
		return chargeAccountId;
	}

	private List<Account> getMemberAccounts(Long partyId) {
		List<GenericValue> memberAccountELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Account account = null;
		List<Account> listAccount = new ArrayList<Account>();
		for (GenericValue genericValue : memberAccountELI) {
			account = new Account();
			account.setAccountNo(genericValue.get("accountNo").toString());
			account.setAccountName(genericValue.get("accountName").toString());

			listAccount.add(account);
		}
		return listAccount;
	}

	@GET
	@Produces("application/json")
	@Path("/balance/{phoneNumber}")
	public Response getBalance(@PathParam("phoneNumber") String phoneNumber) {
		
		BigDecimal bdBalance = null;
		// Long memberAccountId =
		// MSaccoManagementServices.getMemberAccountId(phoneNumber);

		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		if (msaccoStatus.getStatus().equals("SUCCESS")) {
			bdBalance = AccHolderTransactionServices.getTotalBalance(String
					.valueOf(msaccoStatus.getMsaccoApplication().getLong(
							"memberAccountId")), new Timestamp(Calendar
					.getInstance().getTimeInMillis()));
		}
		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);

		if (bdBalance != null)
			transaction.setAmount(bdBalance);
		transaction.setStatus(msaccoStatus.getStatus());

		addServiceLog(phoneNumber, "Query Balance", null, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/withdrawal/{phoneNumber}/{amount}/{transactionId}/{reference}/{withdrawalStage}")
	public Response withdrawal(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("transactionId") String transactionId,
			@PathParam("reference") String reference,
			@PathParam("withdrawalStage") String withdrawalStage) {

		
		/****
		 * Withdrawal Stage
		 * 
		 * Withdrawal_Request
		 * Withdrawal_Confirm
		 * Withdrawal_Decline
		 * 
		 * */

//		if (withdrawalStage.equals("Withdrawal_Confirm")) {
//			AccHolderTransactionServices.WITHDRAWALOK = "OK";
//		} else {
//			AccHolderTransactionServices.WITHDRAWALOK = "FALSE";
//		}

		// withdrawalStage

		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		TransactionResult transaction = new TransactionResult();
		ATMTransaction atmtransaction = new ATMTransaction();
		// transaction.setPhoneNumber(phoneNumber);
		// transaction.setStatus(msaccoStatus.getStatus());
		transaction.setTelephoneNo(phoneNumber);

		if (msaccoStatus.getStatus().equals("SUCCESS")) {
			String memberAccountId = msaccoStatus.getMsaccoApplication()
					.getLong("memberAccountId").toString();

			// .getCardApplication()
			// .getLong("memberAccountId").toString();
			memberAccountId = memberAccountId.replaceAll(",", "");
			System.out.println("AAAAAAAAAAAAA Account ID " + memberAccountId);
			log.info("PPPPPPP partnerTransactionId 1 "+transactionId);
			// Check if Member Has Enough Money - Limit, charges
			atmtransaction = AccHolderTransactionServices
					.cashWithdrawal(amount, String.valueOf(memberAccountId),
							"MSACCOWITHDRAWAL",
							withdrawalStage, reference, transactionId);
			// transaction.setStatus("NOTENOUGHMONEY");
			// transaction.setStatus(atmStatus.getStatus());
			// transaction.setCardNumber(cardNumber);
			// transaction.setCardStatusId(atmStatus.getCardStatusId());
			// transaction.setCardStatus(atmStatus.getCardStatus());
			// transaction.setMemberAccountId(msaccoStatus.getMsaccoApplication()
			// .getLong("memberAccountId"));
			// getMemberAccount(atmStatus.getCardApplication().getLong("memberAccountId"));
			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(msaccoStatus.getMsaccoApplication()
							.getLong("memberAccountId"));
			// transaction.setMemberAccountId(memberAccount
			// .getLong("memberAccountId"));
			Account account = new Account();
			account.setAccountNo(memberAccount.getString("accountNo"));
			account.setAccountName(memberAccount.getString("accountName"));

			// transaction.setAccount(account);
			// transaction.set
			// transaction.setReference(reference);
			transaction.setTransactionId(transactionId);
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));
			transaction.setTransactionId(transactionId);

			if (reference.equals("NOREFENCE")) {
				reference = "";
			}
			transaction.setReference(reference);

			Results results = new Results();
			if ((atmtransaction.getStatus().equals("SUCCESS")) || (atmtransaction.getStatus().equals("CONFIRMSUCCESSFUL")) || (atmtransaction.getStatus().equals("DECLINESUCCESSFUL"))) {
				results.setResultCode("0");
				results.setResultDesc("Success");
			} else {
				results.setResultCode("-1");
				results.setResultDesc("Failure");
				
			}
			
			msaccoStatus.setStatus(atmtransaction.getStatus());
			// transaction.g
			// transaction.setResults(results);
			transaction.setResults(results);
			transaction.setTranstype("Withdrawal");
		}
		
		addServiceLog(phoneNumber, withdrawalStage, amount, msaccoStatus.getStatus());

		Gson gson = new Gson();
		String json = gson.toJson(transaction);
		//AccHolderTransactionServices.WITHDRAWALOK = "OK";
		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/deposit/{phoneNumber}/{amount}/{transactionId}/{reference}")
	public Response deposit(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("transactionId") String transactionId,
			@PathParam("reference") String reference) {
		
		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		MSaccoTransaction msaccotransaction = new MSaccoTransaction();
		msaccotransaction.setPhoneNumber(phoneNumber);
		msaccotransaction.setStatus(msaccoStatus.getStatus());

		TransactionResult transaction = new TransactionResult();
		transaction.setTelephoneNo(phoneNumber);

		if (msaccoStatus.getStatus().equals("SUCCESS")) {
			AccHolderTransactionServices.cashDeposit(amount, msaccoStatus
					.getMsaccoApplication().getLong("memberAccountId"), null,
					"MSACCODEPOSIT");

			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(msaccoStatus.getMsaccoApplication()
							.getLong("memberAccountId"));

			Account account = new Account();
			account.setAccountNo(memberAccount.getString("accountNo"));
			account.setAccountName(memberAccount.getString("accountName"));

			// transaction.setAccount(account);
			// transaction.setAccountNo(memberAccount.getString("accountNo"));
			// transaction.setAccountName(memberAccount.getString("accountName"));
			// transaction.setReference(reference);
			transaction.setTransactionId(transactionId);
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));
			transaction.setReference(reference);

			Results results = new Results();
			if (msaccoStatus.getStatus().equals("SUCCESS")) {
				results.setResultCode("0");
				results.setResultDesc("Success");
			} else {
				results.setResultCode("-1");
				results.setResultDesc("Failure");
			}
			transaction.setResults(results);
			transaction.setTranstype("Deposit");
		} else {
			msaccotransaction.setStatus("CANNOTDEPOSIT");
		}
		
		addServiceLog(phoneNumber, "MSacco Deposir", amount, msaccotransaction.getStatus());

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/loanrepayment/{phoneNumber}/{transactionId}/{reference}")
	public Response loanrepayment(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("transactionId") String transactionId,
			@PathParam("reference") String reference) {
		addServiceLog(phoneNumber, "Loans Query", null, null);
		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);
		transaction.setStatus(msaccoStatus.getStatus());
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanRepayment = delegator.makeValue("LoanRepayment");
		if (msaccoStatus.getStatus().equals("SUCCESS")) {
			// LoanRepayments
			// loanRepayment.set("transactionAmount", value);
		} else {
			transaction.setStatus("NOLOANSTOREPAY");
		}

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/loanrepayment/{phoneNumber}/{loanNumber}/{amount}/{transactionId}/{reference}")
	public Response loanrepayment(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("loanNumber") String loanNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("transactionId") String transactionId,
			@PathParam("reference") String reference) {
		
		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		TransactionResult transaction = new TransactionResult();
		transaction.setTelephoneNo(phoneNumber);

		// LoansProcessingServices
		MSaccoTransaction msaccotransaction = new MSaccoTransaction();
		msaccotransaction.setPhoneNumber(phoneNumber);
		msaccotransaction.setStatus(msaccoStatus.getStatus());
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanRepayment = delegator.makeValue("LoanRepayment");
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		if (msaccoStatus.getStatus().equals("SUCCESS")) {
			// LoanRepayments
			String partyId = msaccoStatus.getMsaccoApplication()
					.getLong("partyId").toString();
			String loanApplicationId = LoanServices.getLoan(loanNumber)
					.getLong("loanApplicationId").toString();
			partyId = partyId.replaceAll(",", "");
			loanApplicationId = loanApplicationId.replaceFirst(",", "");
			loanRepayment.set("loanRepaymentId", loanRepaymentId);
			loanRepayment.set("transactionAmount", amount);
			loanRepayment.set(
					"totalInterestDue",
					LoanRepayments.getTotalInterestDue(partyId,
							loanApplicationId).subtract(
							LoanRepayments.getTotalInterestPaid(partyId,
									loanApplicationId)));
			loanRepayment.set(
					"totalInsuranceDue",
					LoanRepayments.getTotalInsuranceDue(partyId,
							loanApplicationId).subtract(
							LoanRepayments.getTotalInsurancePaid(partyId,
									loanApplicationId)));
			loanRepayment.set(
					"totalPrincipalDue",
					LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId).subtract(
							LoanRepayments.getTotalPrincipalPaid(partyId,
									loanApplicationId)));
			loanRepayment.set("partyId", Long.valueOf(partyId));
			loanRepayment.set("loanApplicationId",
					Long.valueOf(loanApplicationId));

			loanRepayment.set("isActive", "Y");
			loanRepayment.set("createdBy", "dmin");
			loanRepayment.set("loanNo", loanNumber);

			Map<String, String> userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");

			LoanRepayments.repayLoan(loanRepayment, userLogin);

			// transaction.setReference(reference);
			transaction.setTransactionId(transactionId);

			Results results = new Results();
			if (msaccoStatus.getStatus().equals("SUCCESS")) {
				results.setResultCode("0");
				results.setResultDesc("Success");
			} else {
				results.setResultCode("-1");
				results.setResultDesc("Failure");
			}
			transaction.setResults(results);
			transaction.setTranstype("LoanRepayment");
		} else {
			msaccotransaction.setStatus("NOLOANSTOREPAY");
		}
		
		addServiceLog(phoneNumber, "Loan Repayment", amount, msaccotransaction.getStatus());

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	private void addServiceLog(String phoneNo, String transactionType,
			BigDecimal transactionAmount, String transactionStatus) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue msaccoTransactionsLog = null;
		String msaccoTransactionsLogId = delegator
				.getNextSeqId("MsaccoTransactionsLog");
		msaccoTransactionsLog = delegator.makeValidValue(
				"MsaccoTransactionsLog", UtilMisc.toMap(
						"msaccoTransactionsLogId", msaccoTransactionsLogId,
						"phoneNo", phoneNo, "transactionType", transactionType,
						"transactionAmount", transactionAmount, "transactionStatus", transactionStatus));
		try {
			delegator.createOrStore(msaccoTransactionsLog);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	
	//Query Accounts, no charging
	private List<Account> addAccountBalancesNoCharging(String phoneNumber) {
		// String partyId
		// MSaccoManagementServices.getMSaccoAccount(phoneNumber)
		// MSaccoManagementServices.getMemberAccountId(phoneNumber)
		// AccHolderTransactionServices.getM
		Long memberAccountId = null;
		memberAccountId = MSaccoManagementServices
				.getMemberAccountId(phoneNumber);
		List<Account> listAccount = new ArrayList<Account>();
		Account account = new Account();
		account.setTelephoneNo(phoneNumber);
		account.setQuery_type("accounts");
		
		List<Long> listMemberAccountId = new ArrayList<Long>();
		//AccHolderTransactionServices.

		if (memberAccountId != null) {
			//Post MSacco Enquiry Charge
			
			listMemberAccountId = AccHolderTransactionServices.getMemberAccountIdsWithdrawable(AccHolderTransactionServices
					.getMemberAccount(memberAccountId).getLong("partyId"));
			
			for (Long currentMemberAccountId : listMemberAccountId) {
				
				
				account = new Account();
				account.setTelephoneNo(phoneNumber);
				account.setQuery_type("accounts");
				
				GenericValue memberAccount = AccHolderTransactionServices
						.getMemberAccount(currentMemberAccountId);

				account.setAccountNo(memberAccount.getString("accountNo"));
				account.setAccountName(memberAccount.getString("accountName"));
				account.setAccountType(AccHolderTransactionServices.getAccountProductName(currentMemberAccountId));
				String memberAccountIdString = currentMemberAccountId.toString();
				memberAccountIdString = memberAccountIdString.replaceFirst(",", "");
				account.setAccountBalance(AccHolderTransactionServices
						.getTotalBalanceNow(memberAccountIdString).doubleValue());
				listAccount.add(account);
				
			}
			
		}

		
		return listAccount;

	}

}
