package org.ofbiz.registry;

import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtilProperties;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.mail.MimeMessageWrapper;

import com.sun.mail.smtp.SMTPAddressFailedException;

public class FileServices {
	public static String module = FileServices.class.getName();
	public static Logger log = Logger.getLogger(FileServices.class);
	public static final String resource = "CommonUiLabels";

	public static Boolean updateFileStatus(GenericValue fileRequest, Delegator delegator) {
		String fileId = fileRequest.getString("fileId");
		log.info("FileId Has been Fetched Guyz ################################ " + fileId + "########################################");
		GenericValue file = null;
		try {
			file = delegator.findOne("RegistryFiles", UtilMisc.toMap("fileId", fileId), false);
			log.info("File Has been Fetched Guyz ################################ " + file + "########################################");
		} catch (GenericModelException e) {
			Debug.logError(e.toString(), "Cannot Primary Find File", module);
		}catch (GenericEntityException e) {
			Debug.logError(e, "Cannot Find File", module);
		}



		return true;
	}
	
	//================================ COUNTING FILE VOLUMES ====================================================
	
	public static String getFileVolumeCount(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String volumeCount = null;
		int count=0;
		List<GenericValue> volumELI = null;
		try {
			volumELI = delegator.findList("RegistryFileVolume", EntityCondition.makeCondition("partyId", partyId), null, null, null, false);
		
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (GenericValue genericValue : volumELI) {
			count++;
		}
		volumeCount= String.valueOf(count);
		log.info("NUMBER OF FILE VOLUMES ################################ " + volumeCount + "########################################");
				
		return volumeCount;
		
	}
	
	
	public static String getFileDocCount(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String nextDocFolioCount = null;
		int count=0;
		List<GenericValue> volumELI = null;
		try {
			volumELI = delegator.findList("RegistryDocuments", EntityCondition.makeCondition("partyId", partyId), null, null, null, false);
		
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (GenericValue genericValue : volumELI) {
			count++;
		}
		nextDocFolioCount= String.valueOf(count+1);
		log.info("NUMBER OF FILE VOLUMES ################################ " + nextDocFolioCount + "########################################");
				
		return nextDocFolioCount;
		
	}
	
	public static String getArchingDateCount(String docType, Date receiptDate) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String retentionPeriod = null;
		GenericValue DocumentType = null;
		String archiveDate= "NA";
	      try {
	    	  DocumentType = delegator.findOne("RegistryDocumentType", 
	             	UtilMisc.toMap("DocumentTypeId", docType), false);
	           	log.info("++++++++++++++carryOverLeaveGV++++++++++++++++" +DocumentType);
	             }
	       catch (GenericEntityException e) {
	            e.printStackTrace();;
	       }  
		if (DocumentType != null) {
			retentionPeriod=DocumentType.getString("retentionPeriod");
			
			
			log.info("RETENTION PERIOD ################################ " + retentionPeriod + "########################################");
			
			if(retentionPeriod == "PERMANENT"){
				 archiveDate= "NA";
				
			} 
			else{
				LocalDate reDate = new LocalDate(receiptDate);
				LocalDate bodDate = reDate.plusYears(Integer.valueOf(retentionPeriod));
				archiveDate= bodDate.toString();
				log.info("ARCHIVING DATE ################################ " + bodDate + "########################################");
			}
		}
		
		
		return archiveDate;
		
	}
	
	public static String getMemberStatusAndUpdatedDate(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		/*String partyId = new String(request.getParameter("partyId")).toString();*/	
		List<GenericValue> membersELI = null; 
		try {
			membersELI = delegator.findAll("Member", true);
			
			/*membersELI = delegator.findOne("Member", UtilMisc.toMap("partyId", partyId), false);*/
			
			log.info("MEMBERS ################################ " + membersELI + "########################################");
				
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String memberStatus = null ,updatedDate = null, partyId = null;
		/*Date updatedDate = null;*/
		
		for (GenericValue genericValue : membersELI) {
			memberStatus = genericValue.getString("isActive");
			updatedDate = genericValue.getString("lastUpdatedStamp");
			partyId = genericValue.getString("partyId");
			
			if(memberStatus.equalsIgnoreCase("")){
				memberStatus ="Y";
			}
			
		}
		
		log.info("STATUS ################################ " + memberStatus + "########################################");
		log.info("UPDATE DATE ################################ " + updatedDate + "########################################");
		
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFiles",
					UtilMisc.toMap("partyId", partyId), false);

			if (memberFiles != null) { // old records
				memberFiles.set("memberStatus", memberStatus);
				memberFiles.set("inactiveStartDate", updatedDate);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		
		return memberStatus;
	}
	
	
	public static String  getIsReadyToMoveToSemiActiveState(Date inactiveStartDate) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		int period = 0;
		int currentPeriod = 0;
		Date today = new Date();
		String state = null;
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
					UtilMisc.toMap("setupId", "1"), false);

			if (memberFiles != null) { // old records
				period = Integer.valueOf(memberFiles.getString("inactiveState"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		currentPeriod =calculateCalenderDaysBetweenDates(inactiveStartDate, today);
		
		if ((period*30) <= (currentPeriod)) {
			state = "MOVE-TO-SEMI-ACTIVE";
		} else {
			state = "NOT-YET";

		}
		log.info("+++++++++++++++++++++++period: "+period);
		log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
		log.info("+++++++++++++++++++++++state: "+state);
		
			   
	    return state;


	}
	
	public static String  getIsReadyToMoveToArchiveState(Date SemiActiveStartDate) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		int period = 0;
		int currentPeriod = 0;
		Date today = new Date();
		String state = null;
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
					UtilMisc.toMap("setupId", "1"), false);

			if (memberFiles != null) { // old records
				period = Integer.valueOf(memberFiles.getString("semiActiveState"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		currentPeriod =calculateCalenderDaysBetweenDates(SemiActiveStartDate, today);
		
		if ((period*30) <= (currentPeriod)) {
			state = "MOVE-TO-ARCHIVE";
		} else {
			state = "NOT-YET";

		}
		log.info("+++++++++++++++++++++++period: "+period);
		log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
		log.info("+++++++++++++++++++++++state: "+state);
		
			   
	    return state;


	}
	
	public static String  getIsReadyToMoveToDisposalState(Date ArchiveStartDate) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		int period = 0;
		int currentPeriod = 0;
		Date today = new Date();
		String state = null;
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
					UtilMisc.toMap("setupId", "1"), false);

			if (memberFiles != null) { // old records
				period = Integer.valueOf(memberFiles.getString("archiveState"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		currentPeriod =calculateCalenderDaysBetweenDates(ArchiveStartDate, today);
		
		if ((period*30) <= (currentPeriod)) {
			state = "MOVE-FOR-DISPOSAL";
		} else {
			state = "NOT-YET";

		}
		log.info("+++++++++++++++++++++++period: "+period);
		log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
		log.info("+++++++++++++++++++++++state: "+state);
		
			   
	    return state;


	}
	
	
		
		
		public static String getIsReadyToMoveToSemiActiveStateYYYY(HttpServletRequest request,	HttpServletResponse response) {
			Delegator delegator;
			delegator = DelegatorFactoryImpl.getDelegator(null);
			int period = 0;
			int currentPeriod = 0;
			Date today = new Date();
			String state = null;
			
			List<GenericValue> personsELI = null; 
			try {
				personsELI = delegator.findAll("RegistryFiles", true);
					
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			String partyId ="", inactiveStartDate = "", stageStatus; 
		
			for (GenericValue genericValue : personsELI) {
				partyId = genericValue.getString("partyId");
				inactiveStartDate = genericValue.getString("inactiveStartDate");
				stageStatus = genericValue.getString("stageStatus");

				if (stageStatus.equalsIgnoreCase("INACTIVE")) {
					
					try {
						GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
								UtilMisc.toMap("setupId", "1"), false);

						if (memberFiles != null) { // old records
							period = Integer.valueOf(memberFiles.getString("inactiveState"));
						}
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}	
					
					@SuppressWarnings("deprecation")
					Date inactive = null;
					try {
						inactive = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(inactiveStartDate));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					currentPeriod =calculateCalenderDaysBetweenDates(inactive, today);
					GenericValue party = null;
					try {
						party = delegator.findOne("RegistryFiles", UtilMisc.toMap("partyId", partyId), false);
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}	
					
					List<GenericValue> rolesToMove = null;
					try {
						rolesToMove = delegator.findByAnd("RegistryFiles",
								UtilMisc.toMap("partyId", partyId), null, false);
					} catch (GenericEntityException e) {
						
					}
					
					
					
					
					if ((period*30) <= (currentPeriod)) {
						/*party.set("isReadyForSemiActive", "Y");*/
						
						for (GenericValue attr : rolesToMove) {
							attr.set("isReadyForSemiActive", "Yeees");
						}
						
					} else {
						for (GenericValue attr : rolesToMove) {
							attr.set("isReadyForSemiActive", "Noooo");
						}
						

					}
					
					log.info("+++++++++++++++++++++++partyId: "+partyId);
					log.info("+++++++++++++++++++++++inactiveStartDate: "+inactiveStartDate);
					log.info("+++++++++++++++++++++++stageStatus: "+stageStatus);
					log.info("+++++++++++++++++++++++period: "+period);
					log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
					
				} else {

				}
				
			}
			//log.info("------------------------------------------------" +partyId);
			
			return partyId;
		}
		
		public static String getDurationBtnRequestAndIssue(String partyId) {
			int interDuration =0;
			String duration = null;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			 List<GenericValue> getActivityELI=null;
			 GenericValue activity = null;
			 Date actionDate = null;
			 Date today = new Date();
			
			EntityConditionList<EntityExpr> getActivity = EntityCondition.makeCondition(UtilMisc.toList(
					    EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("fileActionTypeId",EntityOperator.EQUALS, "Request")),EntityOperator.AND);

			try {
				List<String> orderByList = new ArrayList<String>();
				orderByList.add("-actionDate");
				
				
				getActivityELI = delegator.findList("RegistryFileLogs",
						 getActivity, null, orderByList, null, false);
				
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
				
			}
			
				String StringactionDate = null;
				if ((getActivityELI.size() > 0)) {
					activity = getActivityELI.get(0);
					StringactionDate = activity.getString("actionDate");

				}
				
				try {
					actionDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(StringactionDate));
				} catch (ParseException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				interDuration = calculateCalenderDaysBetweenDates(actionDate, today);
						duration = String.valueOf(interDuration);
			
						log.info("=================+++++++++++++++++++++++duration: "+duration);
			return duration;
			
		}
		
		
	
	public static int calculateCalenderDaysBetweenDates(Date startDate,	Date endDate) {
		int daysCount = 1;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);

		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
				daysCount++;
			

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		return daysCount;
	}
	
	public static int calculateWorkingNonHolidayDaysBetweenDates(Date startDate, Date endDate) {
		int daysCount = 0;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);
		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY) 
					
					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			}

			localDateStartDate = localDateStartDate.plusDays(1);
		}
		
		/*int noOfHolidays = getNumberOfHolidays(startDate, endDate);*/
		
		/*daysCount = daysCount - noOfHolidays;*/

		return daysCount;
	}
	
	public static Date calculateEndWorkingDay(Date startDate, String noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());
		int duration = Integer.valueOf(noOfDays);

		// If this is happening on sunday or saturday push it to start on monday
		if (localDateEndDate.getDayOfWeek()== DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(1);
		}
		// Calculate End Date
		int count = 1;
		while (count < duration) {
			if (localDateEndDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
				localDateEndDate = localDateEndDate.plusDays(3);
			} else {
				localDateEndDate = localDateEndDate.plusDays(1);
			}
			count++;
		}
		log.info("UPDATE DATE ################################ " + localDateEndDate.toDate() + "########################################");

		return localDateEndDate.toDate();
		
		
	}
	
	public static String getDateOfReceivedFile(String partyId, String releasedTo) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String fileDat = null;
         List<GenericValue> dateELI = null;
		
		
		GenericValue lastDate = null;
		
		EntityConditionList<EntityExpr> dateConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
				EntityCondition.makeCondition("releasedTo",EntityOperator.EQUALS, releasedTo)),
					EntityOperator.AND);
		
		try {
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("-timeOut");

			dateELI = delegator.findList("RegistryFileMovement",dateConditions, null, orderByList, null, false);
			
			if (dateELI.size() > 0){
				lastDate = dateELI.get(0); 
				fileDat=lastDate.getString("timeOut");
			
			}
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
		
		Date fileDate = null;
		try {
			fileDate = (Date)(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fileDat));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		LocalDate localDateEndDate = new LocalDate(fileDate.getTime());
		return fileDat;
		
		
	}
	
	public static Boolean isOfficerWithFile(String officerPartyId, String memmberPayrol) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue file = null;
		try {
			file = delegator.findOne("RegistryFiles", UtilMisc.toMap("payrollNumber", memmberPayrol, "currentPossesser", officerPartyId), false);

			log.info("File Has been Fetched Guyz ###### File with payroll>>"+memmberPayrol+" And in possesion of>>"+officerPartyId);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (file == null) {
			
			return false;
		}
		
		return true;
	}
	
	public static String exampleSendEmail(HttpServletRequest request, HttpServletResponse response) {
			// Get the local Service dispatcher from the context
			// Note: Dont forget to import the org.ofbiz.service.* classes
			LocalDispatcher dispatcher =(LocalDispatcher) request.getAttribute("dispatcher");
			String errMsg = null;
			// The following are hardcoded as an example only
			// Your program would set these up from the context or from
			// database lookups
			String mailBody = "This is the body of my email";
			Map<String, String> sendMailContext = new HashMap<String, String>();
			sendMailContext.put("sendTo", "juliandan17@gmail.com");
			//sendMailContext.put("sendCC", "");
			//sendMailContext.put("sendBcc","" );
			sendMailContext.put("sendFrom", "juliandan7@gmail.com");
			sendMailContext.put("subject", "Testing emails sent from an OFBiz Event");
			sendMailContext.put("body", mailBody);
			try {
			// Call the sendMail Service and pass the sendMailContext
			// Map object.
			// Set timeout to 360 and wrap with a new transaction
			Map<String, Object> result = dispatcher.runSync("sendMail", sendMailContext,360, true);
			// Note easy way to get errors when they are returned
			// from another Service
			if (ModelService.RESPOND_ERROR.equals((String)
			result.get(ModelService.RESPONSE_MESSAGE))) {
			Map<String, Object> messageMap = UtilMisc.toMap("errorMessage",	result.get(ModelService.ERROR_MESSAGE));
			errMsg = "Problem sending this email";
			request.setAttribute("_ERROR_MESSAGE_", errMsg);
			return "error";
			}
			}
			catch (GenericServiceException e) {
			// For Events error messages are passed back
			Debug.logWarning(e, "", module);
			// as Request Attributes
			errMsg = "Problem with the sendMail Service call";
			request.setAttribute("_ERROR_MESSAGE_", errMsg);
			return "error";
			}
			return "success";
			}
	
	public static Map<String, Object>  sendMailForIssueFile(DispatchContext ctx, Map<String, ? extends Object> context) {
		Delegator delegator = ctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		  Map<String, Object> results = ServiceUtil.returnSuccess();
		 
		    String sendFrom = "juliandan7@gmail.com";
		    String sendTo = "juliandan17@gmail.com";
	        String sendType = (String) context.get("sendType");
	        String port = (String) context.get("port");
	        String socketFactoryClass = (String) context.get("socketFactoryClass");
	        String socketFactoryPort  = (String) context.get("socketFactoryPort");
	        String socketFactoryFallback  = (String) context.get("socketFactoryFallback");
	        String sendVia = (String) context.get("sendVia");
	        String authUser = (String) context.get("authUser");
	        String authPass = (String) context.get("authPass");
	        String messageId = (String) context.get("messageId");
	        String contentType = (String) context.get("contentType");
	        Boolean sendPartial = (Boolean) context.get("sendPartial");
	        Boolean isStartTLSEnabled = (Boolean) context.get("startTLSEnabled");
		  
	        boolean useSmtpAuth = false;
	        String subject = "Files";
	        subject = FlexibleStringExpander.expandString(subject, context);

	        String partyId = (String) context.get("partyId");
	        String body = "This is the body of my email";
	        String redirectAddress = UtilProperties.getPropertyValue("general.properties", "mail.notifications.redirectTo");
	        if (UtilValidate.isNotEmpty(redirectAddress)) {
	            String originalRecipients = " [To: " + sendTo + "]";
	            subject += originalRecipients;
	            sendTo = redirectAddress;
	        }
	        
	        // define some default
	        if (sendType == null || sendType.equals("mail.smtp.host")) {
	            sendType = "mail.smtp.host";
	            if (UtilValidate.isEmpty(sendVia)) {
	               // sendVia = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.relay.host", "localhost", delegator);
	                sendVia = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.relay.host", delegator);
	            }
	            if (UtilValidate.isEmpty(authUser)) {
	                authUser = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.auth.user", delegator);
	            }
	            if (UtilValidate.isEmpty(authPass)) {
	                authPass = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.auth.password", delegator);
	            }
	            if (UtilValidate.isNotEmpty(authUser)) {
	                useSmtpAuth = true;
	            }
	            if (UtilValidate.isEmpty(port)) {
	                port = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.port", delegator);
	            }
	            if (UtilValidate.isEmpty(socketFactoryPort)) {
	                socketFactoryPort = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.socketFactory.port", delegator);
	            }
	            if (UtilValidate.isEmpty(socketFactoryClass)) {
	                socketFactoryClass = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.socketFactory.class", delegator);
	            }
	            if (UtilValidate.isEmpty(socketFactoryFallback)) {
	                socketFactoryFallback = EntityUtilProperties.getPropertyValue("general.properties", "mail.smtp.socketFactory.fallback", "false", delegator);
	            }
	            if (sendPartial == null) {
	                sendPartial = EntityUtilProperties.propertyValueEqualsIgnoreCase("general.properties", "mail.smtp.sendpartial", "true", delegator) ? true : false;
	            }
	            if (isStartTLSEnabled == null) {
	                isStartTLSEnabled = EntityUtilProperties.propertyValueEqualsIgnoreCase("general.properties", "mail.smtp.starttls.enable", "true", delegator);
	            }
	        } else if (sendVia == null) {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonEmailSendMissingParameterSendVia", locale));
	        }

	        if (contentType == null) {
	            contentType = "text/html";
	        }

	       
	        results.put("contentType", contentType);
	        

	        Session session;
	        MimeMessage mail;
	        try {
	            Properties props = System.getProperties();
	            props.put(sendType, sendVia);
	            if (UtilValidate.isNotEmpty(port)) {
	                props.put("mail.smtp.port", port);
	            }
	            if (UtilValidate.isNotEmpty(socketFactoryPort)) {
	                props.put("mail.smtp.socketFactory.port", socketFactoryPort);
	            }
	            if (UtilValidate.isNotEmpty(socketFactoryClass)) {
	                props.put("mail.smtp.socketFactory.class", socketFactoryClass);
	                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	            }
	            if (UtilValidate.isNotEmpty(socketFactoryFallback)) {
	                props.put("mail.smtp.socketFactory.fallback", socketFactoryFallback);
	            }
	            if (useSmtpAuth) {
	                props.put("mail.smtp.auth", "true");
	            }
	            if (sendPartial != null) {
	                props.put("mail.smtp.sendpartial", sendPartial ? "true" : "false");
	            }
	            if (isStartTLSEnabled) {
	                props.put("mail.smtp.starttls.enable", "true");
	            }

	            session = Session.getInstance(props);
	            boolean debug = UtilProperties.propertyValueEqualsIgnoreCase("general.properties", "mail.debug.on", "Y");
	            session.setDebug(debug);

	            mail = new MimeMessage(session);
	            if (messageId != null) {
	                mail.setHeader("In-Reply-To", messageId);
	                mail.setHeader("References", messageId);
	            }
	            mail.setFrom(new InternetAddress(sendFrom));
	            mail.setSubject(subject, "UTF-8");
	            mail.setHeader("X-Mailer", "Apache OFBiz, The Apache Open For Business Project");
	            mail.setSentDate(new Date());
	            mail.addRecipients(Message.RecipientType.TO, sendTo);
	          
	            
	            
	            	 if (contentType.startsWith("text")) {
	                     mail.setText(body, "UTF-8", contentType.substring(5));
	                 } else {
	                     mail.setContent(body, contentType);
	                 }
	                 mail.saveChanges();
	            } catch (MessagingException e) {
	                Debug.logError(e, "MessagingException when creating message to [" + sendTo + "] from [" + sendFrom + "] subject [" + subject + "]", module);
	                Debug.logError("Email message that could not be created to [" + sendTo + "] had context: " + context, module);
	                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonEmailSendMessagingException", UtilMisc.toMap("sendTo", sendTo, "sendFrom", sendFrom, "subject", subject), locale));
	            }
	        

	        Transport trans = null;
	        try {
	            trans = session.getTransport("smtp");
	            if (!useSmtpAuth) {
	                trans.connect();
	            } else {
	                trans.connect(sendVia, authUser, authPass);
	            }
	            trans.sendMessage(mail, mail.getAllRecipients());
	            results.put("messageWrapper", new MimeMessageWrapper(session, mail));
	            results.put("messageId", mail.getMessageID());
	            trans.close();
	        } catch (SendFailedException e) {
	            // message code prefix may be used by calling services to determine the cause of the failure
	            Debug.logError(e, "[ADDRERR] Address error when sending message to [" + sendTo + "] from [" + sendFrom + "] subject [" + subject + "]", module);
	            List<SMTPAddressFailedException> failedAddresses = FastList.newInstance();
	            Exception nestedException = null;
	            while ((nestedException = e.getNextException()) != null && nestedException instanceof MessagingException) {
	                if (nestedException instanceof SMTPAddressFailedException) {
	                    SMTPAddressFailedException safe = (SMTPAddressFailedException) nestedException;
	                    Debug.logError("Failed to send message to [" + safe.getAddress() + "], return code [" + safe.getReturnCode() + "], return message [" + safe.getMessage() + "]", module);
	                    failedAddresses.add(safe);
	                    break;
	                }
	            }
	            Boolean sendFailureNotification = (Boolean) context.get("sendFailureNotification");
	            if (sendFailureNotification == null || sendFailureNotification) {
	                sendFailureNotification(ctx, context, mail, failedAddresses);
	                results.put("messageWrapper", new MimeMessageWrapper(session, mail));
	                try {
	                    results.put("messageId", mail.getMessageID());
	                    trans.close();
	                } catch (MessagingException e1) {
	                    Debug.logError(e1, module);
	                }
	            } else {
	                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonEmailSendAddressError", UtilMisc.toMap("sendTo", sendTo, "sendFrom", sendFrom, "sendCc",  "subject", subject), locale));
	            }
	        } catch (MessagingException e) {
	            // message code prefix may be used by calling services to determine the cause of the failure
	            Debug.logError(e, "[CON] Connection error when sending message to [" + sendTo + "] from [" + sendFrom + "] subject [" + subject + "]", module);
	            Debug.logError("Email message that could not be sent to [" + sendTo + "] had context: " + context, module);
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonEmailSendConnectionError", UtilMisc.toMap("sendTo", sendTo, "sendFrom", sendFrom,  "subject", subject), locale));
	        }
	        return results;

		}
	

	
	 public static void sendFailureNotification(DispatchContext dctx, Map<String, ? extends Object> context, MimeMessage message, List<SMTPAddressFailedException> failures) {
	        Locale locale = (Locale) context.get("locale");
	        Map<String, Object> newContext = FastMap.newInstance();
	        newContext.put("userLogin", context.get("userLogin"));
	        newContext.put("sendFailureNotification", false);
	        newContext.put("sendFrom", context.get("sendFrom"));
	        newContext.put("sendTo", context.get("sendFrom"));
	        newContext.put("subject", UtilProperties.getMessage(resource, "CommonEmailSendUndeliveredMail", locale));
	        StringBuilder sb = new StringBuilder();
	        sb.append(UtilProperties.getMessage(resource, "CommonEmailDeliveryFailed", locale));
	        sb.append("/n/n");
	        for (SMTPAddressFailedException failure : failures) {
	            sb.append(failure.getAddress());
	            sb.append(": ");
	            sb.append(failure.getMessage());
	            sb.append("/n/n");
	        }
	        sb.append(UtilProperties.getMessage(resource, "CommonEmailDeliveryOriginalMessage", locale));
	        sb.append("/n/n");
	        List<Map<String, Object>> bodyParts = FastList.newInstance();
	        bodyParts.add(UtilMisc.<String, Object>toMap("content", sb.toString(), "type", "text/plain"));
	        Map<String, Object> bodyPart = FastMap.newInstance();
	        bodyPart.put("content", sb.toString());
	        bodyPart.put("type", "text/plain");
	        try {
	            bodyParts.add(UtilMisc.<String, Object>toMap("content", message.getDataHandler()));
	        } catch (MessagingException e) {
	            Debug.logError(e, module);
	        }
	        try {
	            dctx.getDispatcher().runSync("sendMailMultiPart", newContext);
	        } catch (GenericServiceException e) {
	            Debug.logError(e, module);
	        }
	    }
}