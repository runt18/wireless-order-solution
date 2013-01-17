package com.wireless.pojo.dishesOrder;

import java.text.SimpleDateFormat;

import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.pojo.menuMgr.TasteBasic;
import com.wireless.pojo.menuMgr.TasteGroup;
import com.wireless.protocol.Taste;

public class OrderFood extends FoodBasic{
	private float count;					// 数量
	private float discount;					// 折扣率
	private boolean isTemporary = false;	// 是否临时菜
	private long orderDate = 0;				// 日期时间
	private String waiter;					// 服务员
	private TasteGroup tasteGroup;			// 口味
	private float totalPrice;				// 总价
	private short hangStatus = FoodBasic.FOOD_NORMAL;	// 菜品状态  0:正常,1:叫起,2:即起
	
	public OrderFood(com.wireless.protocol.OrderFood protocol){
		this();
		this.setFoodName(protocol.getName());
		this.setFoodID(protocol.getFoodId());
		this.setAliasID(protocol.getAliasId());
		this.getKitchen().setKitchenID(protocol.getKitchen().getId());
		this.getKitchen().setDept(null);
		this.setCount(protocol.getCount());
		this.setUnitPrice(protocol.getPrice());
		this.setStatus(protocol.getStatus());
		this.setDiscount(protocol.getDiscount()); 
		this.setTemporary(protocol.isTemporary);
		this.setOrderDate(protocol.getOrderDate()); 
		this.setWaiter(protocol.getWaiter());
		this.setHangStatus(protocol.hangStatus);
		this.setTotalPrice(protocol.calcPriceWithTaste());
		if(protocol.hasTaste()){
			// 
			TasteGroup tg = new TasteGroup();
			tg.getNormalTaste().setTasteName(protocol.getTasteGroup().getTastePref());
			tg.getNormalTaste().setTastePrice(protocol.getTasteGroup().getTastePrice());
			if(protocol.getTasteGroup().getTmpTaste() != null){
				tg.getTempTaste().setTasteID(protocol.getTasteGroup().getTmpTaste().tasteID);
				tg.getTempTaste().setTasteAliasID(protocol.getTasteGroup().getTmpTaste().aliasID);
				tg.getTempTaste().setTasteName(protocol.getTasteGroup().getTmpTaste().getPreference());
				tg.getTempTaste().setTastePrice(protocol.getTasteGroup().getTmpTaste().getPrice());
			}
			// 
			for(Taste normalTaste : protocol.getTasteGroup().getNormalTastes()){
				TasteBasic tb = new TasteBasic();
				tb.setTasteID(normalTaste.tasteID);
				tb.setTasteAliasID(normalTaste.aliasID);
				tb.setTasteCategory(normalTaste.category);
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
	}
	
	public String getOrderDateFormat(){
		return this.orderDate > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.orderDate) : null;
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
		this.tasteGroup = tasteGroup == null ? new TasteGroup() : tasteGroup;
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
		return this.tasteGroup != null && this.tasteGroup.getNormalTaste() != null && this.tasteGroup.getNormalTaste().getTasteName().trim().length() > 0 ? this.tasteGroup.getNormalTaste().getTasteName() : "无口味";
	}
	
}
