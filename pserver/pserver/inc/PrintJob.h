#pragma once

#include <string>
#include "PrintFunc.h"

using namespace std;

class PrintJob{
public:
	PrintJob() : content(""){};

	//PrintJob(const PrintFunc& printFunc, int reqCode, const string& cont, int id, const wstring& date) : func(printFunc), 
	//																							  req_code(reqCode),
	//																							  content(cont), 
	//																							  order_id(id), 
	//																							  order_date(date){};

	PrintJob(const wstring& printerName, int repeatAmount, wstring printType, 
			 const string& printContnet, int orderId, const wstring& date) : printer_name(printerName), 
																	         repeat(repeatAmount),
																			 print_type(printType),
		 																	 content(printContnet), 
																			 order_id(orderId), 
																			 order_date(date){};

	~PrintJob(){};
	PrintJob(const PrintJob& right) : printer_name(right.printer_name), 
									  repeat(right.repeat),
									  print_type(right.print_type), 
									  content(right.content),
									  order_id(right.order_id),
									  order_date(right.order_date)
	{};

	PrintJob& operator=(const PrintJob& right){ 
		printer_name = right.printer_name; 
		repeat = right.repeat;
		print_type = right.print_type; 
		content = right.content; 
		order_id = right.order_id; 
		order_date = right.order_date; 
		return *this;
	}

	//the function that the printer instance is going to do
	PrintFunc func;
	//the request function code 
	//int req_code;
	//the printer name
	wstring printer_name;
	//the printer type
	wstring print_type;
	//the order id 
	int order_id;
	//the order date
	wstring order_date;
	//the print content
	string content;
	//the amount to repeat
	int repeat;
};