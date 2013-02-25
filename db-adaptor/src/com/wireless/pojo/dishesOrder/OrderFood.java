package com.wireless.pojo.dishesOrder;

import com.wireless.pojo.menuMgr.CancelReason;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.pojo.menuMgr.TasteBasic;
import com.wireless.pojo.menuMgr.TasteGroup;
import com.wireless.protocol.Taste;
import com.wireless.util.DateUtil;

public class OrderFood extends FoodBasic{
	private long orderID;					// 所属账单编号
	private float count;					// 数量
	private float discount;					// 折扣率
	private boolean isTemporary = false;	// 是否临时菜
	private long orderDate = 0;				// 日期时间
	private String waiter;					// 服务员
	private TasteGroup tasteGroup;			// 口味
	private float totalPrice;				// 总价
	private CancelReason cancelReason;		// 退菜信息
	private short hangStatus = FoodBasic.FOOD_NORMAL;	// 菜品状态  0:正常,1:叫起,2:即起
	
	public OrderFood(com.wireless.protocol.OrderFood pt){
		this();
		this.setFoodName(pt.getName());
		this.setFoodID(pt.getFoodId());
		this.setAliasID(pt.getAliasId());
		this.getKitchen().setKitchenID(pt.getKitchen().getId());
		this.getKitchen().setDept(null);
		this.setOrderID(pt.getOrderId());
		this.setCount(pt.getCount());
		this.setUnitPrice(pt.getPrice());
		this.setStatus(pt.getStatus());
		this.setDiscount(pt.getDiscount()); 
		this.setTemporary(pt.isTemp());
		this.setOrderDate(pt.getOrderDate()); 
		this.setWaiter(pt.getWaiter());
		this.setHangStatus(pt.hangStatus);
		this.setTotalPrice(pt.calcPriceWithTaste());
		this.setCancelReason(new CancelReason(pt.getCancelReason()));
		if(pt.hasTaste()){
			// 
			TasteGroup tg = new TasteGroup();
			tg.getNormalTaste().setTasteName(pt.getTasteGroup().getTastePref());
			tg.getNormalTaste().setTastePrice(pt.getTasteGroup().getTastePrice());
			if(pt.getTasteGroup().getTmpTaste() != null){
				tg.getTempTaste().setTasteID(pt.getTasteGroup().getTmpTaste().getTasteId());
				tg.getTempTaste().setTasteAliasID(pt.getTasteGroup().getTmpTaste().getAliasId());
				tg.getTempTaste().setTasteName(pt.getTasteGroup().getTmpTaste().getPreference());
				tg.getTempTaste().setTastePrice(pt.getTasteGroup().getTmpTaste().getPrice());
			}
			// 
			for(Taste normalTaste : pt.getTasteGroup().getNormalTastes()){
				TasteBasic tb = new TasteBasic();
				tb.setTasteID(normalTaste.getTasteId());
				tb.setTasteAliasID(normalTaste.getAliasId());
				tb.setTasteCategory(normalTaste.getCategory());
				tg.addTaste(tb);
			}
			this.setTasteGroup(tg);
		}else{
			this.getTasteGroup().getNormalTaste().setTasteName(com.wireless.protocol.TasteGroup.NO_TASTE_PREF);
			this.getTasteGroup().getNormalTaste().setTastePrice(0);
		}
	}
	
	public OrderFood(){
		super();
		this.tasteGroup = new TasteGroup();
		this.cancelReason = null;
	}
	
	public String getOrderDateFormat(){
		return this.orderDate > 0 ? DateUtil.format(this.orderDate) : null;
	}
	
	public long getOrderID() {
		return orderID;
	}

	public void setOrderID(long orderID) {
		this.orderID = orderID;
	}

	public float getCount() {
		return count;
	}
	public void setCount(float count) {
		this.count = count;
	}
	public float getDiscount() {
		if(isSpecial() == true || isGift() == true){
			discount = 1.00f;  // 特价和赠送菜品不打折
		}
		return discount;
	}
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	public boolean isTemporary() {
		return isTemporary;
	}
	public void setTemporary(boolean isTemporary) {
		this.isTemporary = isTemporary;
	}
	public long getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}
	public String getWaiter() {
		return waiter;
	}
	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}
	public short getHangStatus() {
		return hangStatus;
	}
	public void setHangStatus(short hangStatus) {
		this.hangStatus = hangStatus;
	}
	public float getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}
	public TasteGroup getTasteGroup() {
		return tasteGroup;
	}
	public void setTasteGroup(TasteGroup tasteGroup) {
		this.tasteGroup = tasteGroup;
	}
	public CancelReason getCancelReason() {
		return cancelReason;
	}
	public void setCancelReason(CancelReason cancelReason) {
		this.cancelReason = cancelReason;
	}
	// 添加口味 (快捷操作) 等同于 TasteGroup.addTaste(FoodTaste ft);
	public void addTaste(TasteBasic ft){
		this.tasteGroup.addTaste(ft);
	}
	// 口味价钱 (快捷显示) 等同于 TasteGroup.getNormalTaste().getTastePrice()
	public double getTastePrice(){
		return this.tasteGroup != null && this.tasteGroup.getNormalTaste() != null ? this.tasteGroup.getNormalTaste().getTastePrice() : 0;
	}
	// 口味 (快捷显示) 等同于 TasteGroup.getNormalTaste().getTasteName()
	public String getTastePref(){
		return this.tasteGroup != null && this.tasteGroup.getNormalTaste() != null && this.tasteGroup.getNormalTaste().getTasteName() != null && this.tasteGroup.getNormalTaste().getTasteName().trim().length() > 0 ? this.tasteGroup.getNormalTaste().getTasteName() : "无口味";
	}
	
}
