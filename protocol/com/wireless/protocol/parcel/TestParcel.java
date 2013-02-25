package com.wireless.protocol.parcel;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import com.wireless.protocol.CancelReason;
import com.wireless.protocol.Department;
import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodStatistics;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Region;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;

public class TestParcel {

	@Test
	public void testComplexFoodParcel(){
		Food foodToParcel = new Food();
		
		foodToParcel.setAliasId(1);
		foodToParcel.setPrice(2.45f);
		foodToParcel.getKitchen().setAliasId((short)1);
		foodToParcel.setHot(true);
		foodToParcel.setSpecial(true);
		foodToParcel.setName("测试菜品");
		foodToParcel.setImage("238f91a1.jpg");
		foodToParcel.setStatistics(new FoodStatistics(15367));
		foodToParcel.setPopTastes(new Taste[]{
				new Taste(0, 1, 0), 
				new Taste(0, 2, 0),
				new Taste(0, 3, 0),
				new Taste(0, 4, 0),
				new Taste(0, 5, 0),
				new Taste(0, 6, 0),
				new Taste(0, 7, 0),
				new Taste(0, 8, 0),
				new Taste(0, 9, 0),
				new Taste(0, 10, 0)
			});
		foodToParcel.setChildFoods(new Food[]{
				new Food(0, 1, 0),
				new Food(0, 2, 0),
				new Food(0, 3, 0),
				new Food(0, 4, 0),
				new Food(0, 5, 0),
				new Food(0, 6, 0),
				new Food(0, 7, 0),
				new Food(0, 8, 0),
				new Food(0, 9, 0),
				new Food(0, 10, 0)
		});
		
		Parcel p = new Parcel();
		foodToParcel.writeToParcel(p, Food.FOOD_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		Food parcelableFood = new Food();
		parcelableFood.createFromParcel(p2);
		
		// Check the food alias id 
		Assert.assertEquals("food alias id", foodToParcel.getAliasId(), parcelableFood.getAliasId());
		
		// Check the food unit price
		Assert.assertEquals("food unit price", foodToParcel.getPrice(), parcelableFood.getPrice());
		
		// Check the kitchen to food
		Assert.assertEquals("kitchen to food", foodToParcel.getKitchen(), parcelableFood.getKitchen());
		
		// Check the status to food
		Assert.assertEquals("hot flag to food", foodToParcel.isHot(), parcelableFood.isHot());
		Assert.assertEquals("special flag to food", foodToParcel.isSpecial(), parcelableFood.isSpecial());
		Assert.assertEquals("combo flag to food", foodToParcel.isCombo(), parcelableFood.isCombo());
		Assert.assertEquals("current price flag to food", foodToParcel.isCurPrice(), parcelableFood.isCurPrice());
		Assert.assertEquals("gift flag to food", foodToParcel.isGift(), parcelableFood.isGift());
		Assert.assertEquals("recommend flag to food", foodToParcel.isRecommend(), parcelableFood.isRecommend());
		Assert.assertEquals("sell out flag to food", foodToParcel.isSellOut(), parcelableFood.isSellOut());
		Assert.assertEquals("weight flag to food", foodToParcel.isWeigh(), parcelableFood.isWeigh());
		
		// Check the name to food
		Assert.assertEquals("food name", foodToParcel.getName(), parcelableFood.getName());
		
		// Check the image to food
		Assert.assertEquals("food image", foodToParcel.getImage(), parcelableFood.getImage());

		// Check the the order count
		Assert.assertEquals("order count to food", foodToParcel.statistics.getOrderCnt(), parcelableFood.statistics.getOrderCnt());
		
		// Check the pop tastes
		Assert.assertEquals(foodToParcel.getPopTastes().length, parcelableFood.getPopTastes().length);
		for(int i = 0; i < foodToParcel.getPopTastes().length; i++){
			Assert.assertEquals("pop tastes to food", foodToParcel.getPopTastes()[i], parcelableFood.getPopTastes()[i]);
		}
		
		// Check the child foods
		Assert.assertEquals(foodToParcel.getChildFoods().length, parcelableFood.getChildFoods().length);
		for(int i = 0; i < foodToParcel.getChildFoods().length; i++){
			Assert.assertEquals("children to food", foodToParcel.getChildFoods()[i], parcelableFood.getChildFoods()[i]);
		}
	}
	
	@Test
	public void testComplexTasteParcel(){
		Taste tasteToParcel = new Taste();
		
		tasteToParcel.setAliasId(1);
		tasteToParcel.setCategory(Taste.CATE_STYLE);
		tasteToParcel.setCalc(Taste.CALC_RATE);
		tasteToParcel.setType(Taste.TYPE_RESERVED);
		tasteToParcel.setPrice(2.3f);
		tasteToParcel.setRate(0.2f);
		tasteToParcel.setPreference("测试口味");
		
		Parcel p = new Parcel();
		tasteToParcel.writeToParcel(p, Taste.TASTE_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		
		Taste parcelableTaste = new Taste();
		parcelableTaste.createFromParcel(p2);
		
		//Check the taste alias id
		Assert.assertEquals("taste alias id", tasteToParcel.getAliasId(), parcelableTaste.getAliasId());
		
		//Check the taste category
		Assert.assertEquals("taste category", tasteToParcel.getCategory(), parcelableTaste.getCategory());
		
		//Check the taste calculate type
		Assert.assertEquals("taste calculate type", tasteToParcel.getCalc(), parcelableTaste.getCalc());

		//Check the taste type
		Assert.assertEquals("taste type", tasteToParcel.getType(), parcelableTaste.getType());

		//Check the taste price
		Assert.assertEquals("taste price", tasteToParcel.getPrice(), parcelableTaste.getPrice());
		
		//Check the taste rate
		Assert.assertEquals("taste rate", tasteToParcel.getRate(), parcelableTaste.getRate());

		//Check the taste preference
		Assert.assertEquals("taste preference", tasteToParcel.getPreference(), parcelableTaste.getPreference());

	}
	
	@Test
	public void testComplexKitchenParcel(){
		Kitchen kitchenToParcel = new Kitchen();
		
		kitchenToParcel.setAliasId(Kitchen.KITCHEN_10);
		kitchenToParcel.getDept().setId(Department.DEPT_10);
		kitchenToParcel.setAllowTemp(true);
		kitchenToParcel.setType(Kitchen.TYPE_RESERVED);
		kitchenToParcel.setName("测试厨房");
		
		Parcel p = new Parcel();
		kitchenToParcel.writeToParcel(p, Kitchen.KITCHEN_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());

		Kitchen parcelableKitchen = new Kitchen();
		parcelableKitchen.createFromParcel(p2);
		
		// Check the kitchen alias id
		Assert.assertEquals("kitchen alias id", kitchenToParcel.getAliasId(), parcelableKitchen.getAliasId());
		
		// Check the associated department
		Assert.assertEquals("associated department", kitchenToParcel.getDept(), parcelableKitchen.getDept());
		
		// Check the flag to allow temporary
		Assert.assertEquals("flag to allow temporary", kitchenToParcel.isAllowTemp(), parcelableKitchen.isAllowTemp());
		
		// Check the kitchen type
		Assert.assertEquals("kitchen type", kitchenToParcel.getType(), parcelableKitchen.getType());
		
		// Check the kitchen name
		Assert.assertEquals("kitchen name", kitchenToParcel.getName(), parcelableKitchen.getName());
	}
	
	@Test
	public void testComplexDeptParcel(){
		Department deptToParcel = new Department();
		
		deptToParcel.setId(Department.DEPT_10);
		deptToParcel.setType(Department.TYPE_RESERVED);
		deptToParcel.setName("测试部门");
		
		Parcel p = new Parcel();
		deptToParcel.writeToParcel(p, Department.DEPT_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		Department parcelableDept = new Department();
		parcelableDept.createFromParcel(p2);
		
		// Check the department id
		Assert.assertEquals("dept id", deptToParcel.getId(), parcelableDept.getId());
		
		// Check the department type
		Assert.assertEquals("dept type", deptToParcel.getType(), parcelableDept.getType());
		
		// Check the department name
		Assert.assertEquals("dept name", deptToParcel.getName(), parcelableDept.getName());
		
	}
	
	@Test
	public void testComplexCancelReasonParcel(){
		CancelReason crToParcel = new CancelReason();
		
		crToParcel.setId(1);
		crToParcel.setReason("测试原因");
		
		Parcel p = new Parcel();
		crToParcel.writeToParcel(p, CancelReason.CR_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		
		CancelReason parcelableCR = new CancelReason();
		parcelableCR.createFromParcel(p2);
		
		// Check the cancel reason id
		Assert.assertEquals("cancel reason id", crToParcel.getId(), parcelableCR.getId());
		
		// Check the cancel reason description
		Assert.assertEquals("cancel reason", crToParcel.getReason(), parcelableCR.getReason());
	}
	
	@Test
	public void testComplexDiscountParcel(){
		Discount discountToParcel = new Discount();
		
		discountToParcel.setId(Math.round((float)Math.random()));
		discountToParcel.setLevel(Math.round((float)Math.random()));
		discountToParcel.setStatus(Math.round((float)Math.random()));
		discountToParcel.setName("测试折扣方案");
		discountToParcel.setPlans(new DiscountPlan[]{
			new DiscountPlan(new Kitchen(), 0.1f),	
			new DiscountPlan(new Kitchen(), 0.2f),	
			new DiscountPlan(new Kitchen(), 0.3f),	
			new DiscountPlan(new Kitchen(), 0.4f),	
			new DiscountPlan(new Kitchen(), 0.5f),	
		});
		
		Parcel p = new Parcel();
		discountToParcel.writeToParcel(p, Discount.DISCOUNT_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		
		Discount parcelableDiscount = new Discount();
		parcelableDiscount.createFromParcel(p2);
		
		// Check the discount id
		Assert.assertEquals("discount id", discountToParcel.getId(), parcelableDiscount.getId());
		
		// Check the discount level
		Assert.assertEquals("discount level", discountToParcel.getLevel(), parcelableDiscount.getLevel());
		
		// Check the discount status
		Assert.assertEquals("discount status", discountToParcel.getStatus(), parcelableDiscount.getStatus());
		
		// Check the discount name
		Assert.assertEquals("discount name", discountToParcel.getName(), parcelableDiscount.getName());
		
		// Check the associated discount plans
		Assert.assertEquals(discountToParcel.getPlans().length, parcelableDiscount.getPlans().length);
		for(int i = 0; i < discountToParcel.getPlans().length; i++){
			Assert.assertEquals("the kitchen to plans[" + i + "]", discountToParcel.getPlans()[i].getKitchen(), parcelableDiscount.getPlans()[i].getKitchen());
			Assert.assertEquals("the rate to plans[" + i + "]", discountToParcel.getPlans()[i].getRate(), parcelableDiscount.getPlans()[i].getRate());
		}
	}
	
	@Test
	public void testComplexRestaurantParcel(){
		Restaurant restToParcel = new Restaurant();
		
		restToParcel.setId(1);
		restToParcel.setName("测试餐厅");
		restToParcel.setOwner("测试持有人");
		restToParcel.setInfo("adfasdftesfsd");
		restToParcel.setPwd("adbadyrw353423d");
		restToParcel.setPwd2("3gsh456dsg345q4adf");
		restToParcel.setPwd3("23523zvafja;jp2aopidjf0pqjjaf");
		restToParcel.setPwd4("ad;jfp897345ujhbn'a8puhaq34");
		restToParcel.setPwd5("203974hjnvnjsdup98q23hhalshdf");
		
		Parcel p = new Parcel();
		restToParcel.writeToParcel(p, Restaurant.RESTAURANT_PARCELABLE_COMPLEX);
		
		Restaurant parceableRest = new Restaurant();
		parceableRest.createFromParcel(new Parcel(p.marshall()));
		
		// Check the restaurant id
		Assert.assertEquals("restaurant id", restToParcel.getId(), parceableRest.getId());
		// Check the restaurant name
		Assert.assertEquals("restaurant name", restToParcel.getName(), parceableRest.getName());
		// Check the restaurant owner
		Assert.assertEquals("restaurant onwer", restToParcel.getOwner(), parceableRest.getOwner());
		// Check the restaurant info
		Assert.assertEquals("restaurant info", restToParcel.getInfo(), parceableRest.getInfo());
		// Check the pwd
		Assert.assertEquals("restaurant pwd", restToParcel.getPwd(), parceableRest.getPwd());
		// Check the pwd2
		Assert.assertEquals("restaurant pwd2", restToParcel.getPwd2(), parceableRest.getPwd2());
		// Check the pwd3
		Assert.assertEquals("restaurant pwd3", restToParcel.getPwd3(), parceableRest.getPwd3());
		// Check the pwd4
		Assert.assertEquals("restaurant pwd4", restToParcel.getPwd4(), parceableRest.getPwd4());
		// Check the pwd5
		Assert.assertEquals("restaurant pwd5", restToParcel.getPwd5(), parceableRest.getPwd5());
		
	}
	
	@Test
	public void testComplexRegionParcel(){
		Region regionToParcel = new Region();
		regionToParcel.setRegionId(Region.REGION_10);
		regionToParcel.setName("测试区域");
		
		Parcel p = new Parcel();
		regionToParcel.writeToParcel(p, Region.REGION_PARCELABLE_COMPLEX);
		
		Region parcelableRegon = new Region();
		parcelableRegon.createFromParcel(new Parcel(p.marshall()));
		
		// Check the region id
		Assert.assertEquals("region id", regionToParcel.getRegionId(), parcelableRegon.getRegionId());
		
		// Check the region name
		Assert.assertEquals("region name", regionToParcel.getName(), parcelableRegon.getName());
	}

	@Test
	public void testComplexTableParcel(){
		Table tableToParcel = new Table();
		
		tableToParcel.setAliasId(100);
		tableToParcel.setName("测试餐台");
		tableToParcel.regionID = Region.REGION_10;
		tableToParcel.setServiceRate(0.2f);
		tableToParcel.setMinimumCost(23.4f);
		tableToParcel.setStatus(Table.TABLE_IDLE);
		tableToParcel.setCategory(Table.TABLE_MERGER_CHILD);
		tableToParcel.setCustomNum(13);
		
		Parcel p = new Parcel();
		tableToParcel.writeToParcel(p, Table.TABLE_PARCELABLE_COMPLEX);
		
		Table parcelableTable = new Table();
		parcelableTable.createFromParcel(new Parcel(p.marshall()));
		
		// Check the table alias id
		Assert.assertEquals("table alias id", tableToParcel.getAliasId(), parcelableTable.getAliasId());
		
		// Check the table name
		Assert.assertEquals("table name", tableToParcel.getName(), parcelableTable.getName());
		
		// Check the associated region id
		Assert.assertEquals("table region id", tableToParcel.regionID, parcelableTable.regionID);
		
		// Check the service rate
		Assert.assertEquals("table service rate", tableToParcel.getServiceRate(), parcelableTable.getServiceRate());
		
		// Check the minimum cost
		Assert.assertEquals("table minimum cost", tableToParcel.getMinimumCost(), parcelableTable.getMinimumCost());
		
		// Check the table status
		Assert.assertEquals("table status", tableToParcel.getStatus(), parcelableTable.getStatus());
		
		// Check the table category
		Assert.assertEquals("table category", tableToParcel.getCategory(), parcelableTable.getCategory());
		
		// Check the table custom number
		Assert.assertEquals("table custom number", tableToParcel.getCustomNum(), parcelableTable.getCustomNum());
	}

	@Test
	public void testComplexTasteGroupParcel(){
		TasteGroup tgToParcel = new TasteGroup();
		
		tgToParcel.setGroupId(100);
		
		tgToParcel.addTaste(new Taste(0, 100, 0));
		tgToParcel.addTaste(new Taste(0, 101, 0));
		tgToParcel.addTaste(new Taste(0, 102, 0));
		tgToParcel.addTaste(new Taste(0, 103, 0));
		tgToParcel.addTaste(new Taste(0, 104, 0));
		
		Taste tmpTaste = new Taste();
		tmpTaste.setAliasId(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		tgToParcel.setTmpTaste(tmpTaste);
		
		Parcel p = new Parcel();
		tgToParcel.writeToParcel(p, TasteGroup.TG_PARCELABLE_COMPLEX);
		
		TasteGroup parcelabledTG = new TasteGroup();
		parcelabledTG.createFromParcel(new Parcel(p.marshall()));
		
		// Check the taste group id
		Assert.assertEquals("taste group id", tgToParcel.getGroupId(), parcelabledTG.getGroupId());
		
		// Check the normal tastes
		Assert.assertEquals(tgToParcel.getNormalTastes().length, parcelabledTG.getNormalTastes().length);
		for(int i = 0; i < tgToParcel.getNormalTastes().length; i++){
			Assert.assertEquals("normal tastes to taste group", tgToParcel.getNormalTastes()[i], parcelabledTG.getNormalTastes()[i]);
		}
		
		// Check the temporary taste
		Assert.assertEquals("preference to tmp taste", tgToParcel.getTmpTaste().getPreference(), parcelabledTG.getTmpTaste().getPreference());
		Assert.assertEquals("price to tmp taste", tgToParcel.getTmpTaste().getPrice(), parcelabledTG.getTmpTaste().getPrice());
		Assert.assertEquals("alias id to tmp taste", tgToParcel.getTmpTaste().getAliasId(), parcelabledTG.getTmpTaste().getAliasId());
	}
	
	@Test 
	public void testComplexOrderFoodParcel4Query(){
		OrderFood orderFoodToParcel = new OrderFood();
		
		orderFoodToParcel.setTemp(false);
		orderFoodToParcel.setAliasId(100);
		orderFoodToParcel.setHot(true);
		orderFoodToParcel.setWeigh(true);
		orderFoodToParcel.hangStatus = OrderFood.FOOD_HANG_UP;
		orderFoodToParcel.setOrderDate(new Date().getTime());
		orderFoodToParcel.setWaiter("张宁远");
		
		orderFoodToParcel.getTasteGroup().setGroupId(100);
		
		orderFoodToParcel.getTasteGroup().addTaste(new Taste(0, 100, 0));
		orderFoodToParcel.getTasteGroup().addTaste(new Taste(0, 101, 0));
		orderFoodToParcel.getTasteGroup().addTaste(new Taste(0, 102, 0));
		orderFoodToParcel.getTasteGroup().addTaste(new Taste(0, 103, 0));
		orderFoodToParcel.getTasteGroup().addTaste(new Taste(0, 104, 0));
		
		Taste tmpTaste = new Taste();
		tmpTaste.setAliasId(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		orderFoodToParcel.getTasteGroup().setTmpTaste(tmpTaste);
		
		
		Parcel p = new Parcel();
		orderFoodToParcel.writeToParcel(p, OrderFood.OF_PARCELABLE_4_QUERY);
		
		OrderFood orderFoodAfterParcelled = new OrderFood();
		orderFoodAfterParcelled.createFromParcel(new Parcel(p.marshall()));
		
		compareOrderFood4Query(orderFoodToParcel, orderFoodAfterParcelled);
	}
	
	private void compareOrderFood4Query(OrderFood of1, OrderFood of2){
		// Check the temporary flag
		Assert.assertEquals("temporary flag to order fodd", of1.isTemp(), of2.isTemp());
		
		// Check the alias id
		Assert.assertEquals("alias id to order food", of1.getAliasId(), of2.getAliasId());
		
		// Check the status
		Assert.assertEquals("status to order food", of1.getStatus(), of2.getStatus());
		
		// Check the hang status
		Assert.assertEquals("hang status to order food", of1.hangStatus, of2.hangStatus);
		
		// Check the order date
		Assert.assertEquals("date to order food", of1.getOrderDate(), of2.getOrderDate());
		
		// Check the waiter
		Assert.assertEquals("waiter to order food", of1.getWaiter(), of2.getWaiter());
		
		// Check the taste group id
		Assert.assertEquals("taste group id", of1.getTasteGroup().getGroupId(), of2.getTasteGroup().getGroupId());
		
		// Check the normal tastes
		Assert.assertEquals(of1.hasNormalTaste(), of2.hasNormalTaste());
		if(of1.hasNormalTaste() && of2.hasNormalTaste()){
			Assert.assertEquals(of1.getTasteGroup().getNormalTastes().length, of2.getTasteGroup().getNormalTastes().length);
			for(int i = 0; i < of1.getTasteGroup().getNormalTastes().length; i++){
				Assert.assertEquals("normal tastes to taste group", of1.getTasteGroup().getNormalTastes()[i], of2.getTasteGroup().getNormalTastes()[i]);
			}
		}		
		// Check the temporary taste
		Assert.assertEquals(of1.hasTmpTaste(), of2.hasTmpTaste());
		if(of1.hasTmpTaste() && of2.hasTmpTaste()){
			Assert.assertEquals("preference to tmp taste", of1.getTasteGroup().getTmpTaste().getPreference(), of2.getTasteGroup().getTmpTaste().getPreference());
			Assert.assertEquals("price to tmp taste", of1.getTasteGroup().getTmpTaste().getPrice(), of2.getTasteGroup().getTmpTaste().getPrice());
			Assert.assertEquals("alias id to tmp taste", of1.getTasteGroup().getTmpTaste().getAliasId(), of2.getTasteGroup().getTmpTaste().getAliasId());
		}
	}
	
	@Test
	public void testOrderParcel4Query(){
		Order orderToParcel = new Order();
		
		orderToParcel.setId(191237);
		orderToParcel.getDestTbl().setAliasId(100);
		orderToParcel.getSrcTbl().setAliasId(101);
		orderToParcel.setBirthDate(new Date().getTime());
		orderToParcel.setOrderDate(new Date().getTime());
		orderToParcel.setCategory(Order.CATE_MERGER_CHILD);
		orderToParcel.setCustomNum(4);
		
		OrderFood[] foods = new OrderFood[]{
			new OrderFood(),
			new OrderFood()
		};
		
		orderToParcel.setOrderFoods(foods);
		
		//1st order food
		foods[0].setTemp(false);
		foods[0].setAliasId(100);
		foods[0].setHot(true);
		foods[0].setWeigh(true);
		foods[0].hangStatus = OrderFood.FOOD_HANG_UP;
		foods[0].setOrderDate(new Date().getTime());
		foods[0].setWaiter("张宁远");
		
		foods[0].getTasteGroup().setGroupId(100);
		
		foods[0].getTasteGroup().addTaste(new Taste(0, 100, 0));
		foods[0].getTasteGroup().addTaste(new Taste(0, 101, 0));
		foods[0].getTasteGroup().addTaste(new Taste(0, 102, 0));
		foods[0].getTasteGroup().addTaste(new Taste(0, 103, 0));
		foods[0].getTasteGroup().addTaste(new Taste(0, 104, 0));
		
		
		Taste tmpTaste = new Taste();
		tmpTaste.setAliasId(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		foods[0].getTasteGroup().setTmpTaste(tmpTaste);
		
		//2nd order food
		foods[1].setTemp(false);
		foods[1].setAliasId(101);
		foods[1].setWeigh(true);
		foods[1].hangStatus = OrderFood.FOOD_HANG_UP;
		foods[1].setOrderDate(new Date().getTime());
		foods[1].setWaiter("张宁远");
		
		Parcel p = new Parcel();
		orderToParcel.writeToParcel(p, Order.ORDER_PARCELABLE_4_QUERY);
		
		Order orderAfterParcelled = new Order();
		orderAfterParcelled.createFromParcel(new Parcel(p.marshall()));
		
		// Check the order id
		Assert.assertEquals("order id", orderToParcel.getId(), orderAfterParcelled.getId());
		
		// Check the destination table
		Assert.assertEquals("dest table to order", orderToParcel.getDestTbl().getAliasId(), orderAfterParcelled.getDestTbl().getAliasId());
		
		// Check the source table
		Assert.assertEquals("source table to order", orderToParcel.getSrcTbl().getAliasId(), orderAfterParcelled.getSrcTbl().getAliasId());
		
		// Check the birth date
		Assert.assertEquals("birth date to order", orderToParcel.getBirthDate(), orderAfterParcelled.getBirthDate());
		
		// Check the order date
		Assert.assertEquals("order date ", orderToParcel.getOrderDate(), orderAfterParcelled.getOrderDate());
		
		// Check the category
		Assert.assertEquals("category to order", orderToParcel.getCategory(), orderAfterParcelled.getCategory());
		
		// Check the custom number
		Assert.assertEquals("custom number to order", orderToParcel.getCustomNum(), orderAfterParcelled.getCustomNum());
		
		// Check the order foods
		Assert.assertEquals(orderToParcel.hasOrderFood(), orderAfterParcelled.hasOrderFood());
		Assert.assertEquals(orderToParcel.getOrderFoods().length, orderAfterParcelled.getOrderFoods().length);
		for(int i = 0; i < orderToParcel.getOrderFoods().length; i++){
			compareOrderFood4Query(orderToParcel.getOrderFoods()[i], orderAfterParcelled.getOrderFoods()[i]);
		}
	}
}
