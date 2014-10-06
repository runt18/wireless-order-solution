package com.wireless.util;

public class StringHtml {

	public static enum ConvertTo{
		TO_HTML("To HTML"),
		TO_NORMAL("To Normal");
		
		private final String desc;
		
		private ConvertTo(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private final String val;
	
	public StringHtml(String raw, ConvertTo convertTo){
		if(convertTo == ConvertTo.TO_HTML){
			val = raw.replaceAll("&amp;", "&")
				     .replaceAll("&lt;", "<")
				     .replaceAll("&gt;", ">")
				     .replaceAll("&quot;", "\"")
				     .replaceAll("\r&#10;", "　\n")
				     .replaceAll("&#10;", "　\n")
				     .replaceAll("&#032;", " ")
				     .replaceAll("&#039;", "'")
				     .replaceAll("&#033;", "!");
				     
		}else if(convertTo == ConvertTo.TO_NORMAL){
			val = raw.replaceAll("&", "&amp;")
					 .replaceAll("<", "&lt;")
					 .replaceAll(">", "&gt;")
					 .replaceAll("\"", "&quot;")
					 .replaceAll("\n\r", "&#10;")
					 .replaceAll("\r\n", "&#10;")
					 .replaceAll("\n", "&#10;")
					 .replaceAll(" ", "&#032;")
					 .replaceAll("'", "&#039;")
					 .replaceAll("!", "&#033;");
		}else{
			throw new IllegalArgumentException("");
		}
	}
	
	
	@Override
	public String toString(){
		return this.val;
	}
	
}
