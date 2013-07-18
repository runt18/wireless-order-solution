package com.wireless.db.printScheme;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.protocol.Terminal;

public class PrinterDao {

	public static int insert(DBCon dbCon, Terminal term, Printer.InsertBuilder builder) throws SQLException{
		return 0;
	}
	
	public static void deleteById(DBCon dbCon, int printerId){
		
	}
	
	public static List<Printer> getPrinters(DBCon dbCon, Terminal term){
		return null;
	}
}
