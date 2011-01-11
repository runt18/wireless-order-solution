#include "stdafx.h"
#include "../inc/ReqPrinterOTA.h"
#include "../inc/Mode.h"
#include "../inc/Type.h"

/****************************************************************************
 * The printer OTA request is as below
 * mode : type : seq : reserved : pin[6] : 0x00 : 0x00
 * <Header>
 * mode - PRINT
 * type - PRINTER_OTA
 * seq - auto calculated and filled in
 * reserved - 0x00
 * pin[6] - 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
 * len[2] - 0x00, 0x00
 ****************************************************************************/	
ReqPrinterOTA::ReqPrinterOTA(){
	header.mode = Mode::PRINT;
	header.type = Type::PRINTER_OTA;
}

ReqPrinterOTA::~ReqPrinterOTA(){

}