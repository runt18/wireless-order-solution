// on page load function
function billsOnLoad() {

	var Request = new URLParaQuery();
	pin = Request["pin"];

	// update the operator name
	getOperatorName(pin);

	// get the bills
	billQuery(0, 1, "");
};