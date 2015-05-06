package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;

public class ReqLimitRemaining extends RequestPackage {

	public ReqLimitRemaining(Staff staff, Food.LimitRemainingBuilder builder) {
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.MAKE_LIMIT_REMAINING;
		fillBody(builder, 0);
	}

}
