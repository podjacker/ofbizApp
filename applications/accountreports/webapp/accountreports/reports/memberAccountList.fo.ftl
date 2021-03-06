<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#escape x as x?xml>
    <#if myAccountsList?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            Member Accounts
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block margin-left="0.4in" text-decoration="underline" font-size="10pt" text-align="left"  font-weight="bold" >
            Member Accounts Listing
        </fo:block>


        <#-- fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.firstName} ${member.middleName} ${member.lastName}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">ID Number:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.idNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.payrollNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
        </fo:list-block -->
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="40pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <#-- fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Account No.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Member</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Member No.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>ID No.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Station</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Product</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Book Balance</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Available Balance</fo:block>
                        </fo:table-cell>
                        <#-- fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Principal Due</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Principal Paid</fo:block>
                        </fo:table-cell -->
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#assign count=0>
                	<#assign totalBalance=0>
                	<#assign totalBalanceAvailable=0>
                	<#-- assign totalDisbursed=0 -->
                	
                    <#list myAccountsList as accountBalance>
                        <fo:table-row>
                        	<#assign count = count + 1>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${accountBalance.accountNo?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              <#-- assign loanPartyId = loan.partyId />  
                              <#assign member = delegator.findOne("Member", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", loanPartyId), true)/ -->
                                ${accountBalance.firstName?if_exists} ${accountBalance.middleName?if_exists} ${accountBalance.lastName?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${accountBalance.payrollNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
								                           
                                <fo:block> 
                                <#-- assign loanBalance = loan.loanAmt - Static["org.ofbiz.loans.LoanServices"].getLoansRepaidByLoanApplicationId(loan.loanApplicationId)>
                                <#assign totalLoans = totalLoans+loanBalance -->
                                <#-- ${expectation.remitanceDescription?if_exists} 
                                Kshs.  ${loanBalance?string(",##0.00")} --> 
                                ${accountBalance.memberNumber?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	<#-- assign loanProductId = loan.loanProductId />
                                	<#assign loanProduct = delegator.findOne("LoanProduct", Static["org.ofbiz.base.util.UtilMisc"].toMap("loanProductId", loanProductId), true)/ -->
                                	<#-- if (expectation.isReceived = 'Y') && (expectation.amount??)  >
                                	
                                		
                                		 Kshs.  ${expectation.amount?string(",##0.00")}
								  ${loanProduct.code} 
								</#if -->
								${accountBalance.idNumber?if_exists}
								
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              
                                <#-- if (expectation.isReceived = 'N') && (expectation.amount??)>
								    Kshs.  ${expectation.amount?string(",##0.00")} 
								    ${loan.loanAmt}
								</#if -->
								
								<#-- assign totalDisbursed = totalDisbursed + loan.loanAmt -->
								<#if accountBalance.stationId?? >	
                               		<#assign stationId = accountBalance.stationId />
                              		<#assign stationIdString = stationId.toString() />
                              		<#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationIdString), true)/>
                                
								${station.name?if_exists}
								
								</#if>
                               </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              <#-- assign stationId = member.stationId />
                              <#assign stationIdString = stationId.toString() />
                              <#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationIdString), true)/ -->
                                <#-- if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.00")} 
								    ${station.stationNumber} 
								</#if -->
								${accountBalance.name?if_exists}
                               </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign memberAccountId = accountBalance.memberAccountId>
                            	<#assign memberAccountIdStr = memberAccountId.toString()>
                            	<#assign accountBalanceAmt = Static["org.ofbiz.accountholdertransactions.AccHolderTransactionServices"].getBookBalanceNow(memberAccountIdStr)/>
                            	<#assign accountAvailableBalanceAmt = Static["org.ofbiz.accountholdertransactions.AccHolderTransactionServices"].getTotalBalanceNow(memberAccountIdStr)/>
                                <fo:block>
                                Kshs.  ${accountBalanceAmt?string(",##0.00")}
                                
                                <#assign totalBalance = totalBalance + accountBalanceAmt>
                                <#assign totalBalanceAvailable = totalBalanceAvailable + accountAvailableBalanceAmt>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>Kshs. ${accountAvailableBalanceAmt?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${loanRepayment.totalPrincipalDue?if_exists}</fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${loanRepayment.principalAmount?if_exists}</fo:block>
                            </fo:table-cell -->
                        </fo:table-row>
                        
                    </#list>
                    <fo:table-row>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                               
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                 
                                </fo:block>
                            </fo:table-cell>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                TOTAL BALANCE
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                Kshs.  ${totalBalance?string(",##0.00")}	
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                Kshs.  ${totalBalanceAvailable?string(",##0.00")}	
                                </fo:block>
                            </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>