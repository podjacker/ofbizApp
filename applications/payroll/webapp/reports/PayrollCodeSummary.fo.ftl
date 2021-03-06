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
    <#if codes?has_content>
	        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            PAYROLL CODE SUMMARY REPORT
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            ${period.name?if_exists}
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            ${element.name?if_exists} (${element.elementCode?if_exists})
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Payroll Codes Details -->
        
        <#function zebra index>
		  <#if (index % 2) == 0>
		    <#return "white" />
		  <#else>
		    <#return "#C0D9AF" />
		  </#if>
		</#function>

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll Number</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#assign count = 0>
                    <#list codes as codes>
                    	<#assign count = count + 1>
                        <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(codes_index)}">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(codes_index)}">
                                <fo:block>${codes.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(codes_index)}">
                                <fo:block>${codes.firstName?if_exists} ${codes.lastName?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(codes_index)}">
                                <fo:block>${codes.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#list>
                    <fo:table-row>
                    		<fo:table-cell padding="2pt">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold">
                                <fo:block>TOTAL</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold">
                                <fo:block>${TotalAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>

