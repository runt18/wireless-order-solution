#pragma once

#include <string>
#include "PrintFunc.h"

using namespace std;

class PrintJob{
public:
	PrintJob() : content(""){};
	PrintJob(const PrintFunc& printFunc, int reqCode, const string& cont, int id, wstring date) : func(printFunc), req_code(reqCode),
		content(cont), order_id(id), order_date(date){};
	~PrintJob(){};
	PrintJob(const PrintJob& right) : func(right.func), 
		req_code(right.req_code), 
		order_id(right.order_id),
		order_date(right.order_date),
		content(right.content)
	{};

	PrintJob& operator=(const PrintJob& right){ 
		func = right.func; 
		content = right.content; 
		order_id = right.order_id; 
		order_date = right.order_date; 
		req_code = right.req_code; 
		return *this;
	}

	//the function that the printer instance is going to do
	PrintFunc func;
	//the request function code 
	int req_code;
	//the order id 
	int order_id;
	//the order date
	wstring order_date;
	//the print content
	string content;
};