package com.wireless.parcel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodStatistics;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;

public class TestParcel {

	private static Staff mStaff;
	
	@BeforeClass
	public static void init(){
		mStaff = new Staff();
		Role role = new Role(0);
		role.addPrivilege(new Privilege(Privilege.Code.ADD_FOOD));
		role.addPrivilege(new Privilege(Privilege.Code.CANCEL_FOOD));
		mStaff.setRole(role);
	}
	
	@Test
	public void testComplexFoodParcel(){
		Food foodToParcel = new Food(0);
		
		foodToParcel.setAliasId(1);
		foodToParcel.setPrice(2.45f);
		foodToParcel.getKitchen().setDisplayId(1);
		foodToParcel.setHot(true);
		foodToParcel.setSpecial(true);
		foodToParcel.setName("测试菜品");
		foodToParcel.setImage("238f91a1.jpg");
		foodToParcel.setStatistics(new FoodStatistics(15367));
		
		List<Taste> popTastes = new ArrayList<Taste>();
		popTastes.add(new Taste(1));
		popTastes.add(new Taste(2));
		popTastes.add(new Taste(3));
		popTastes.add(new Taste(4));
		popTastes.add(new Taste(5));
		popTastes.add(new Taste(6));
		popTastes.add(new Taste(7));
		popTastes.add(new Taste(8));
		popTastes.add(new Taste(9));
		popTastes.add(new Taste(10));
		foodToParcel.setPopTastes(popTastes);
		
		List<Food> childFoods = new ArrayList<Food>();
		childFoods.add(new Food(0, 1, 0));
		childFoods.add(new Food(0, 2, 0));
		childFoods.add(new Food(0, 3, 0));
		childFoods.add(new Food(0, 4, 0));
		childFoods.add(new Food(0, 5, 0));
		childFoods.add(new Food(0, 6, 0));
		childFoods.add(new Food(0, 7, 0));
		childFoods.add(new Food(0, 8, 0));
		childFoods.add(new Food(0, 9, 0));
		childFoods.add(new Food(0, 10, 0));
		foodToParcel.setChildFoods(childFoods);
		
		Parcel p = new Parcel();
		foodToParcel.writeToParcel(p, Food.FOOD_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		Food parcelableFood = new Food(0);
		parcelableFood.createFromParcel(p2);
		
		// Check the food alias id 
		assertEquals("food alias id", foodToParcel.getAliasId(), parcelableFood.getAliasId());
		
		// Check the food unit price
		assertEquals("food unit price", foodToParcel.getPrice(), parcelableFood.getPrice(), 0.01);
		
		// Check the kitchen to food
		assertEquals("kitchen to food", foodToParcel.getKitchen(), parcelableFood.getKitchen());
		
		// Check the status to food
		assertEquals("hot flag to food", foodToParcel.isHot(), parcelableFood.isHot());
		assertEquals("special flag to food", foodToParcel.isSpecial(), parcelableFood.isSpecial());
		assertEquals("combo flag to food", foodToParcel.isCombo(), parcelableFood.isCombo());
		assertEquals("current price flag to food", foodToParcel.isCurPrice(), parcelableFood.isCurPrice());
		assertEquals("gift flag to food", foodToParcel.isGift(), parcelableFood.isGift());
		assertEquals("recommend flag to food", foodToParcel.isRecommend(), parcelableFood.isRecommend());
		assertEquals("sell out flag to food", foodToParcel.isSellOut(), parcelableFood.isSellOut());
		assertEquals("weight flag to food", foodToParcel.isWeigh(), parcelableFood.isWeigh());
		
		// Check the name to food
		assertEquals("food name", foodToParcel.getName(), parcelableFood.getName());
		
		// Check the image to food
		assertEquals("food image", foodToParcel.getImage(), parcelableFood.getImage());

		// Check the the order count
		assertEquals("order count to food", foodToParcel.statistics.getOrderCnt(), parcelableFood.statistics.getOrderCnt());
		
		// Check the pop tastes
		assertEquals("pop tastes to food", foodToParcel.getPopTastes(), parcelableFood.getPopTastes());
		
		// Check the child foods
		assertEquals("children to food", foodToParcel.getChildFoods(), parcelableFood.getChildFoods());
	}
	
	@Test
	public void testComplexTasteParcel(){
		Taste tasteToParcel = new Taste(1);
		
		tasteToParcel.setCategory(new TasteCategory(1));
		tasteToParcel.setCalc(Taste.Calc.BY_RATE);
		tasteToParcel.setType(Taste.Type.RESERVED);
		tasteToParcel.setPrice(2.3f);
		tasteToParcel.setRate(0.2f);
		tasteToParcel.setPreference("测试口味");
		
		Parcel p = new Parcel();
		tasteToParcel.writeToParcel(p, Taste.TASTE_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		
		Taste parcelableTaste = new Taste(0);
		parcelableTaste.createFromParcel(p2);
		
		//Check the taste alias id
		assertEquals("taste alias id", tasteToParcel.getTasteId(), parcelableTaste.getTasteId());
		
		//Check the taste category
		assertEquals("taste category", tasteToParcel.getCategory().getId(), parcelableTaste.getCategory().getId());
		
		//Check the taste calculate type
		assertEquals("taste calculate type", tasteToParcel.getCalc().getVal(), parcelableTaste.getCalc().getVal());

		//Check the taste type
		assertEquals("taste type", tasteToParcel.getType().getVal(), parcelableTaste.getType().getVal());

		//Check the taste price
		assertEquals("taste price", tasteToParcel.getPrice(), parcelableTaste.getPrice(), 0.01);
		
		//Check the taste rate
		assertEquals("taste rate", tasteToParcel.getRate(), parcelableTaste.getRate(), 0.01);

		//Check the taste preference
		assertEquals("taste preference", tasteToParcel.getPreference(), parcelableTaste.getPreference());

	}
	
	@Test
	public void testComplexKitchenParcel(){
		Kitchen kitchenToParcel = new Kitchen(0);
		
		kitchenToParcel.setDisplayId(1);
		kitchenToParcel.getDept().setId(Department.DeptId.DEPT_1.getVal());
		kitchenToParcel.setAllowTemp(true);
		kitchenToParcel.setType(Kitchen.Type.NORMAL);
		kitchenToParcel.setName("测试厨房");
		
		Parcel p = new Parcel();
		kitchenToParcel.writeToParcel(p, Kitchen.KITCHEN_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());

		Kitchen parcelableKitchen = new Kitchen(0);
		parcelableKitchen.createFromParcel(p2);
		
		// Check the kitchen alias id
		assertEquals("kitchen display id", kitchenToParcel.getDisplayId(), parcelableKitchen.getDisplayId());
		
		// Check the associated department
		assertEquals("associated department", kitchenToParcel.getDept(), parcelableKitchen.getDept());
		
		// Check the flag to allow temporary
		assertEquals("flag to allow temporary", kitchenToParcel.isAllowTemp(), parcelableKitchen.isAllowTemp());
		
		// Check the kitchen type
		assertEquals("kitchen type", kitchenToParcel.getType(), parcelableKitchen.getType());
		
		// Check the kitchen name
		assertEquals("kitchen name", kitchenToParcel.getName(), parcelableKitchen.getName());
	}
	
	@Test
	public void testComplexDeptParcel(){
		Department deptToParcel = new Department(Department.DeptId.DEPT_1.getVal());
		
		deptToParcel.setType(Department.DeptId.DEPT_1.getType());
		deptToParcel.setName("测试部门");
		
		Parcel p = new Parcel();
		deptToParcel.writeToParcel(p, Department.DEPT_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		Department parcelableDept = new Department(0);
		parcelableDept.createFromParcel(p2);
		
		// Check the department id
		assertEquals("dept id", deptToParcel.getId(), parcelableDept.getId());
		
		// Check the department type
		assertEquals("dept type", deptToParcel.getType(), parcelableDept.getType());
		
		// Check the department name
		assertEquals("dept name", deptToParcel.getName(), parcelableDept.getName());
		
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
		assertEquals("cancel reason id", crToParcel.getId(), parcelableCR.getId());
		
		// Check the cancel reason description
		assertEquals("cancel reason", crToParcel.getReason(), parcelableCR.getReason());
	}
	
	@Test
	public void testComplexDiscountParcel(){
		Discount discountToParcel = new Discount();
		
		discountToParcel.setId(Math.round((float)Math.random()));
		discountToParcel.setType(Discount.Type.NORMAL);
		discountToParcel.setStatus(Discount.Status.DEFAULT);
		discountToParcel.setName("测试折扣方案");
		List<DiscountPlan> plans = new ArrayList<DiscountPlan>();
		plans.add(new DiscountPlan(new Kitchen(0), 0.1f));
		plans.add(new DiscountPlan(new Kitchen(0), 0.2f));
		plans.add(new DiscountPlan(new Kitchen(0), 0.2f));
		plans.add(new DiscountPlan(new Kitchen(0), 0.2f));
		plans.add(new DiscountPlan(new Kitchen(0), 0.2f));
		discountToParcel.addPlans(plans);
		
		Parcel p = new Parcel();
		discountToParcel.writeToParcel(p, Discount.DISCOUNT_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		
		Discount parcelableDiscount = new Discount();
		parcelableDiscount.createFromParcel(p2);
		
		// Check the discount id
		assertEquals("discount id", discountToParcel.getId(), parcelableDiscount.getId());
		
		// Check the discount level
		assertEquals("discount level", discountToParcel.getType(), parcelableDiscount.getType());
		
		// Check the discount status
		assertEquals("discount status", discountToParcel.getStatus(), parcelableDiscount.getStatus());
		
		// Check the discount name
		assertEquals("discount name", discountToParcel.getName(), parcelableDiscount.getName());
		
		// Check the associated discount plans
		assertEquals(discountToParcel.getPlans().size(), parcelableDiscount.getPlans().size());
		for(int i = 0; i < discountToParcel.getPlans().size(); i++){
			assertEquals("the kitchen to plans[" + i + "]", discountToParcel.getPlans().get(i).getKitchen(), parcelableDiscount.getPlans().get(i).getKitchen());
			assertEquals("the rate to plans[" + i + "]", discountToParcel.getPlans().get(i).getRate(), parcelableDiscount.getPlans().get(i).getRate(), 0.01);
		}
	}
	
	@Test
	public void testComplexRestaurantParcel(){
		Restaurant restToParcel = new Restaurant();
		
		restToParcel.setId(1);
		restToParcel.setName("测试餐厅");
		restToParcel.setInfo("adfasdftesfsd");
		
		Parcel p = new Parcel();
		restToParcel.writeToParcel(p, Restaurant.RESTAURANT_PARCELABLE_COMPLEX);
		
		Restaurant parceableRest = new Restaurant();
		parceableRest.createFromParcel(new Parcel(p.marshall()));
		
		// Check the restaurant id
		assertEquals("restaurant id", restToParcel.getId(), parceableRest.getId());
		// Check the restaurant name
		assertEquals("restaurant name", restToParcel.getName(), parceableRest.getName());
		// Check the restaurant info
		assertEquals("restaurant info", restToParcel.getInfo(), parceableRest.getInfo());
		
	}
	
	@Test
	public void testComplexRegionParcel(){
		Region expected = new Region();
		expected.setRegionId(Region.RegionId.REGION_10.getId());
		expected.setName("测试区域");
		
		Parcel p = new Parcel();
		expected.writeToParcel(p, Region.REGION_PARCELABLE_COMPLEX);
		
		Region actual = new Region();
		actual.createFromParcel(new Parcel(p.marshall()));
		
		// Check the region id
		assertEquals("region id", expected.getRegionId(), actual.getRegionId());
		
		// Check the region name
		assertEquals("region name", expected.getName(), actual.getName());
	}

	@Test
	public void testComplexTableParcel(){
		Table expected = new Table();
		
		expected.setTableAlias(100);
		expected.setTableName("测试餐台");
		expected.setRegion(new Region(Region.RegionId.REGION_10.getId()));
		expected.setServiceRate(0.2f);
		expected.setMinimumCost(23.4f);
		expected.setStatus(Table.Status.IDLE);
		expected.setCategory(Order.Category.NORMAL);
		expected.setCustomNum(13);
		
		Parcel p = new Parcel();
		expected.writeToParcel(p, Table.TABLE_PARCELABLE_COMPLEX);
		
		Table actual = new Table();
		actual.createFromParcel(new Parcel(p.marshall()));
		
		// Check the table alias id
		assertEquals("table alias id", expected.getAliasId(), actual.getAliasId());
		
		// Check the table name
		assertEquals("table name", expected.getName(), actual.getName());
		
		// Check the associated region id
		assertEquals("table region id", expected.getRegion().getRegionId(), actual.getRegion().getRegionId());
		
		// Check the service rate
		assertEquals("table service rate", expected.getServiceRate(), actual.getServiceRate(), 0.01);
		
		// Check the minimum cost
		assertEquals("table minimum cost", expected.getMinimumCost(), actual.getMinimumCost(), 0.01);
		
		// Check the table status
		assertEquals("table status", expected.getStatus().getVal(), actual.getStatus().getVal());
		
		// Check the table category
		assertEquals("table category", expected.getCategory(), actual.getCategory());
		
		// Check the table custom number
		assertEquals("table custom number", expected.getCustomNum(), actual.getCustomNum());
	}

	@Test
	public void testComplexTasteGroupParcel(){
		//TasteGroup tgToParcel = new TasteGroup();
		
		//tgToParcel.setGroupId(100);
		
//		tgToParcel.addTaste(new Taste(100, TasteCategory.Status.TASTE));
//		tgToParcel.addTaste(new Taste(101, TasteCategory.Status.TASTE));
//		tgToParcel.addTaste(new Taste(102, TasteCategory.Status.TASTE));
//		tgToParcel.addTaste(new Taste(103, TasteCategory.Status.TASTE));
//		tgToParcel.addTaste(new Taste(104, TasteCategory.Status.TASTE));
		
		List<Taste> tastes = Arrays.asList(new Taste[]{
				new Taste(100, TasteCategory.Status.TASTE),
				new Taste(101, TasteCategory.Status.TASTE),
				new Taste(102, TasteCategory.Status.TASTE),
				new Taste(103, TasteCategory.Status.TASTE),
				new Taste(104, TasteCategory.Status.TASTE)
			  });
		
		Taste tmpTaste = new Taste(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		//tgToParcel.setTmpTaste(tmpTaste);
		
		TasteGroup tgToParcel = new TasteGroup(100, null, tastes, tmpTaste);
		
		Parcel p = new Parcel();
		tgToParcel.writeToParcel(p, TasteGroup.TG_PARCELABLE_COMPLEX);
		
		TasteGroup parcelabledTG = new TasteGroup();
		parcelabledTG.createFromParcel(new Parcel(p.marshall()));
		
		// Check the taste group id
		assertEquals("taste group id", tgToParcel.getGroupId(), parcelabledTG.getGroupId());
		
		// Check the normal tastes
		assertEquals("normal tastes to taste group", tgToParcel.getNormalTastes(), parcelabledTG.getNormalTastes());
		
		// Check the temporary taste
		assertEquals("preference to tmp taste", tgToParcel.getTmpTaste().getPreference(), parcelabledTG.getTmpTaste().getPreference());
		assertEquals("price to tmp taste", tgToParcel.getTmpTaste().getPrice(), parcelabledTG.getTmpTaste().getPrice(), 0.01);
		assertEquals("alias id to tmp taste", tgToParcel.getTmpTaste().getTasteId(), parcelabledTG.getTmpTaste().getTasteId());
	}
	
	@Test 
	public void testComplexOrderFoodParcel4Query(){
		OrderFood orderFoodToParcel = new OrderFood();
		
		orderFoodToParcel.setTemp(false);
		orderFoodToParcel.setCount(2.34f);
		orderFoodToParcel.asFood().setAliasId(100);
		orderFoodToParcel.asFood().setHot(true);
		orderFoodToParcel.asFood().setWeigh(true);
		orderFoodToParcel.setHangup(true);
		orderFoodToParcel.setOrderDate(new Date().getTime());
		orderFoodToParcel.setWaiter("张宁远");
		
		orderFoodToParcel.makeTasteGroup().setGroupId(100);
		
		orderFoodToParcel.addTaste(new Taste(100, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(101, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(102, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(103, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(104, TasteCategory.Status.TASTE));
		
		Taste tmpTaste = new Taste(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		orderFoodToParcel.setTmpTaste(tmpTaste);
		
		
		Parcel p = new Parcel();
		orderFoodToParcel.writeToParcel(p, OrderFood.OF_PARCELABLE_4_QUERY);
		
		OrderFood orderFoodAfterParcelled = new OrderFood();
		orderFoodAfterParcelled.createFromParcel(new Parcel(p.marshall()));
		
		compareOrderFood4Query(orderFoodToParcel, orderFoodAfterParcelled);
	}
	
	private void compareOrderFood4Query(OrderFood of1, OrderFood of2){
		// Check the temporary flag
		assertEquals("temporary flag to order fodd", of1.isTemp(), of2.isTemp());
		
		if(of1.isTemp()){
			// Check the temporary food name
			assertEquals("name to temporary food", of1.getName(), of2.getName());
			
			// Check the unit price to temporary food
			assertEquals("price to temporary food", of1.asFood().getPrice(), of2.asFood().getPrice(), 0.01);
			
			// Check the kitchen alias to temporary food
			assertEquals("kitchen display id to temporary food", of1.getKitchen().getDisplayId(), of2.getKitchen().getDisplayId());
			
		}else{

			// Check the status
			assertEquals("status to order food", of1.asFood().getStatus(), of2.asFood().getStatus());
			
			if(of1.hasTasteGroup() && of2.hasTasteGroup()){
				// Check the taste group id
				assertEquals("taste group id", of1.getTasteGroup().getGroupId(), of2.getTasteGroup().getGroupId());
				// Check the normal tastes
				assertEquals("normal tastes to taste group", of1.getTasteGroup().getNormalTastes(), of2.getTasteGroup().getNormalTastes());
			}
			
			// Check the temporary taste
			assertEquals(of1.hasTmpTaste(), of2.hasTmpTaste());
			if(of1.hasTmpTaste() && of2.hasTmpTaste()){
				assertEquals("preference to tmp taste", of1.getTasteGroup().getTmpTaste().getPreference(), of2.getTasteGroup().getTmpTaste().getPreference());
				assertEquals("price to tmp taste", of1.getTasteGroup().getTmpTaste().getPrice(), of2.getTasteGroup().getTmpTaste().getPrice(), 0.01);
				assertEquals("alias id to tmp taste", of1.getTasteGroup().getTmpTaste().getTasteId(), of2.getTasteGroup().getTmpTaste().getTasteId());
			}
		}

		// Check the alias id
		assertEquals("alias id to order food", of1.getAliasId(), of2.getAliasId());

		// Check the order count
		assertEquals("count to order food", of1.getCount(), of2.getCount(), 0.01);
		
		// Check the hang status
		assertEquals("hang status to order food", of1.isHangup(), of2.isHangup());
		
		// Check the order count
		assertEquals("count to order food", of1.getCount(), of2.getCount(), 0.01);
		
		// Check the order date
		assertEquals("date to order food", of1.getOrderDate(), of2.getOrderDate());
		
		// Check the waiter
		assertEquals("waiter to order food", of1.getWaiter(), of2.getWaiter());
	}
	
	@Test
	public void testOrderParcel4Query() throws BusinessException{
		Order orderToParcel = new Order();
		
		orderToParcel.setId(191237);
		orderToParcel.getDestTbl().setTableAlias(100);
		orderToParcel.setBirthDate(new Date().getTime());
		orderToParcel.setOrderDate(new Date().getTime());
		orderToParcel.setCategory(Order.Category.NORMAL);
		orderToParcel.setCustomNum(4);
		
		OrderFood[] foods = new OrderFood[]{
			new OrderFood(),
			new OrderFood()
		};
		
		//1st order food
		foods[0].setTemp(false);
		foods[0].asFood().setAliasId(100);
		foods[0].asFood().setHot(true);
		foods[0].asFood().setWeigh(true);
		foods[0].setHangup(true);
		foods[0].setOrderDate(System.currentTimeMillis());
		foods[0].setWaiter("张宁远");
		
		List<Taste> tastes = Arrays.asList(new Taste[]{
				new Taste(100, TasteCategory.Status.TASTE),
				new Taste(101, TasteCategory.Status.TASTE),
				new Taste(102, TasteCategory.Status.TASTE),
				new Taste(103, TasteCategory.Status.TASTE),
				new Taste(104, TasteCategory.Status.TASTE)
			  });
		
		Taste tmpTaste = new Taste(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		//tgToParcel.setTmpTaste(tmpTaste);
		
		foods[0].setTasteGroup(new TasteGroup(100, null, tastes, tmpTaste));
		
		//foods[0].makeTasteGroup().setGroupId(100);
		
//		foods[0].getTasteGroup().addTaste(new Taste(100, TasteCategory.Status.TASTE));
//		foods[0].getTasteGroup().addTaste(new Taste(101, TasteCategory.Status.TASTE));
//		foods[0].getTasteGroup().addTaste(new Taste(102, TasteCategory.Status.TASTE));
//		foods[0].getTasteGroup().addTaste(new Taste(103, TasteCategory.Status.TASTE));
//		foods[0].getTasteGroup().addTaste(new Taste(104, TasteCategory.Status.TASTE));
//		
//		Taste tmpTaste = new Taste(302);
//		tmpTaste.setPreference("临时口味");
//		tmpTaste.setPrice(2.3f);
//		foods[0].getTasteGroup().setTmpTaste(tmpTaste);
		
		orderToParcel.addFood(foods[0], mStaff);
		
		//2nd order food
		foods[1].setTemp(false);
		foods[1].asFood().setAliasId(101);
		foods[1].asFood().setWeigh(true);
		foods[1].setHangup(true);
		foods[1].setOrderDate(new Date().getTime());
		foods[1].setWaiter("张宁远");
		
		orderToParcel.addFood(foods[1], mStaff);
		
		Parcel p = new Parcel();
		orderToParcel.writeToParcel(p, Order.ORDER_PARCELABLE_4_QUERY);
		
		Order orderAfterParcelled = new Order();
		orderAfterParcelled.createFromParcel(new Parcel(p.marshall()));
		
		// Check the order id
		assertEquals("order id", orderToParcel.getId(), orderAfterParcelled.getId());
		
		// Check the destination table
		assertEquals("dest table to order", orderToParcel.getDestTbl().getAliasId(), orderAfterParcelled.getDestTbl().getAliasId());
		
		// Check the birth date
		assertEquals("birth date to order", orderToParcel.getBirthDate(), orderAfterParcelled.getBirthDate());
		
		// Check the order date
		assertEquals("order date ", orderToParcel.getOrderDate(), orderAfterParcelled.getOrderDate());
		
		// Check the category
		assertEquals("category to order", orderToParcel.getCategory(), orderAfterParcelled.getCategory());
		
		// Check the custom number
		assertEquals("custom number to order", orderToParcel.getCustomNum(), orderAfterParcelled.getCustomNum());
		
		// Check the order foods
		assertEquals(orderToParcel.hasOrderFood(), orderAfterParcelled.hasOrderFood());
		assertEquals(orderToParcel.getOrderFoods().size(), orderAfterParcelled.getOrderFoods().size());
		for(int i = 0; i < orderToParcel.getOrderFoods().size(); i++){
			compareOrderFood4Query(orderToParcel.getOrderFoods().get(i), orderAfterParcelled.getOrderFoods().get(i));
		}
	}
	
	@Test
	public void testComplexOrderFood4Commit(){
		OrderFood orderFoodToParcel = new OrderFood();
		
		orderFoodToParcel.setTemp(false);
		orderFoodToParcel.setCount(2.34f);
		orderFoodToParcel.asFood().setAliasId(100);
		orderFoodToParcel.asFood().setHot(true);
		orderFoodToParcel.asFood().setWeigh(true);
		orderFoodToParcel.setHangup(true);
		orderFoodToParcel.setOrderDate(new Date().getTime());
		orderFoodToParcel.setWaiter("张宁远");
		orderFoodToParcel.setHurried(true);
		orderFoodToParcel.setCancelReason(new CancelReason(120));
		
		orderFoodToParcel.makeTasteGroup().setGroupId(100);
		
		orderFoodToParcel.addTaste(new Taste(100, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(101, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(102, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(103, TasteCategory.Status.TASTE));
		orderFoodToParcel.addTaste(new Taste(104, TasteCategory.Status.TASTE));
		
		Taste tmpTaste = new Taste(302);
		tmpTaste.setPreference("临时口味");
		tmpTaste.setPrice(2.3f);
		orderFoodToParcel.setTmpTaste(tmpTaste);
		
		
		Parcel p = new Parcel();
		orderFoodToParcel.writeToParcel(p, OrderFood.OF_PARCELABLE_4_COMMIT);
		
		OrderFood orderFoodAfterParcelled = new OrderFood();
		orderFoodAfterParcelled.createFromParcel(new Parcel(p.marshall()));
		
		compareOrderFood4Commit(orderFoodToParcel, orderFoodAfterParcelled);
	}
	
	private void compareOrderFood4Commit(OrderFood of1, OrderFood of2){
		// Check the temporary flag
		assertEquals("temporary flag to order fodd", of1.isTemp(), of2.isTemp());
		
		if(of1.isTemp()){
			// Check the temporary food name
			assertEquals("name to temporary food", of1.getName(), of2.getName());
			
			// Check the unit price to temporary food
			assertEquals("price to temporary food", of1.asFood().getPrice(), of2.asFood().getPrice(), 0.01);
			
			// Check the kitchen alias to temporary food
			assertEquals("kitchen display id to temporary food", of1.getKitchen().getDisplayId(), of2.getKitchen().getDisplayId());
			
		}else{

			// Check the status
			assertEquals("status to order food", of1.asFood().getStatus(), of2.asFood().getStatus());
			
			// Check the taste group id
			assertEquals("taste group id", of1.getTasteGroup().getGroupId(), of2.getTasteGroup().getGroupId());
			
			// Check the normal tastes
			assertEquals("normal tastes to taste group", of1.getTasteGroup().getNormalTastes(), of2.getTasteGroup().getNormalTastes());
			
			// Check the temporary taste
			assertEquals(of1.hasTmpTaste(), of2.hasTmpTaste());
			if(of1.hasTmpTaste() && of2.hasTmpTaste()){
				assertEquals("preference to tmp taste", of1.getTasteGroup().getTmpTaste().getPreference(), of2.getTasteGroup().getTmpTaste().getPreference());
				assertEquals("price to tmp taste", of1.getTasteGroup().getTmpTaste().getPrice(), of2.getTasteGroup().getTmpTaste().getPrice(), 0.01);
				assertEquals("alias id to tmp taste", of1.getTasteGroup().getTmpTaste().getTasteId(), of2.getTasteGroup().getTmpTaste().getTasteId());
			}
		}

		// Check the alias id
		assertEquals("alias id to order food", of1.getAliasId(), of2.getAliasId());

		// Check the order count
		assertEquals("count to order food", of1.getCount(), of2.getCount(), 0.01);
		
		// Check the hang status
		assertEquals("hang status to order food", of1.isHangup(), of2.isHangup());
		
		// Check the order count
		assertEquals("count to order food", of1.getCount(), of2.getCount(), 0.01);
		
		// Check the order date
		assertEquals("date to order food", of1.getOrderDate(), of2.getOrderDate());
		
		// Check the waiter
		assertEquals("waiter to order food", of1.getWaiter(), of2.getWaiter());
		
		// Check the hurried flag
		assertEquals("hurried flag to order food", of1.isHurried(), of2.isHurried());
		
		// Check the cancel reason id
		assertEquals("cancel reason id to order food", of1.getCancelReason().getId(), of2.getCancelReason().getId());
	}
	
}
