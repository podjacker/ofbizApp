<script type="text/javascript">

   jQuery(document).ready(function(){


		
	jQuery('select[name="sourceMemberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         getSourceAccountBalance(reqUrl, memberAccountId);
        });
      
     jQuery('select[name="destMemberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         getDestinationAccountBalance(reqUrl, memberAccountId);
        });

     });
     
   function getSourceAccountBalance(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				 $('input[name="amountInSource"]').val(data.amountInSource);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function getDestinationAccountBalance(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				 $('input[name="amountInDestination"]').val(data.amountInDestination);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
     
   
  
    
 function getSourceMemberAccounts(partyId){
    	 var reqUrl = '/memberaccountmanagement/control/memberaccountlist';
         sourceMemberAccounts(reqUrl, partyId);
    }
    
     function sourceMemberAccounts(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="sourceMemberAccountId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function getDestinationMemberAccounts(partyId){
    	 var reqUrl = '/memberaccountmanagement/control/memberaccountlist';
         destinationMemberAccounts(reqUrl, partyId);
    }
    
     function destinationMemberAccounts(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="destMemberAccountId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
   
   </script>