package com.wireless.protocol.parcel;

import junit.framework.Assert;

import org.junit.Test;

import com.wireless.protocol.CancelReason;
import com.wireless.protocol.Department;
import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodStatistics;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Taste;

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
	
}
