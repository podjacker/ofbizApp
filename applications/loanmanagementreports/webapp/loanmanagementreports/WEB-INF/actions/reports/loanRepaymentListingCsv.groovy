import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;


context.startDate = startDate
context.endDate = endDate
loanProductId = parameters.loanProductId

println "####################### START DATE: "+ startDate
println "####################### END DATE: "+ endDate
println "####################### loanProductId: " + loanProductId

listBuilder = []
loansRepaymentsList = []

//GET ALL LOAN PRODUCTS
loanProducts = delegator.findList('LoanProduct',  null, null,["loanProductId"],null,false)

summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDate));
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDate));

loansRepayments = delegator.findList('LoanRepayment',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["createdStamp"],null,false)

loansRepayments.each { repayment ->
  listBuilder = [
    loanNo:repayment.loanNo,
    partyId:repayment.partyId,
    transactionAmount:repayment.transactionAmount,
  ]
  loansRepaymentsList.add(listBuilder)
}

context.loanApps = loansRepaymentsList
