// Get all the keys from document
var keys = document.querySelectorAll('.calculator span');
var operators = ['+', '-', 'x', '÷'];


// Add onclick event to all the keys and perform operations
for(var i = 0; i < keys.length; i++) {
	keys[i].onclick = function(e) {
		// Get the input and button values
		//var input = document.querySelector('.screen');
		var input = $('#'+focusInput);
		var inputVal = input.val();
		var btnVal = this.innerHTML;
		
		// Now, just append the key values (btnValue) to the input string and finally use javascript's eval function to get the result
		// If clear key is pressed, erase everything
		if(btnVal == 'C') {
			input.val('');
			decimalAdded = false;
		}
		
		// Now only the decimal problem is left. We can solve it easily using a flag 'decimalAdded' which we'll set once the decimal is added and prevent more decimals to be added once it's set. It will be reset when an operator, eval or clear key is pressed.
		else if(btnVal == '.') {
		    if (inputVal.indexOf('.') == -1) {
		    	input.val(inputVal + btnVal);
		    }
		}
		
		//排除意外按键
		else if(btnVal.length > 1){
			
		}
		
		else if(btnVal == '+'){
			input.val(parseFloat(inputVal) + 1);
		}
		
		else if(btnVal == '-'){
			input.val(parseFloat(inputVal) - 1);
		}
		
		// if any other key is pressed, just append it
		else {
			if(inputVal == ''){
				input.val(btnVal);
			}else{
				input.val(inputVal + btnVal);
			}
			
		}
		
		//触发key input事件, eg:数字键盘触发搜索餐台
		if(numKeyBoardFireEvent){
			numKeyBoardFireEvent();
		}
		
		// prevent page jumps
		e.preventDefault();
	} ;
}