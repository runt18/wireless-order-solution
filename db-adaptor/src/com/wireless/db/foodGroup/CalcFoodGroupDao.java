package com.wireless.db.foodGroup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodStatistics;
import com.wireless.protocol.Terminal;

public class CalcFoodGroupDao {

	private final static int AVERAGE_AMOUNT_PER_PAGE = 3;
	
	private final static int MAX_LARGE_AMOUNT_PER_PAGE = 2;
	
	/**
	 * 
	 * @param term
	 * @return
	 * @throws SQLException
	 */
	public static List<Pager> calc(Terminal term) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calc(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @return
	 * @throws SQLException
	 */
	public static List<Pager> calc(DBCon dbCon, Terminal term) throws SQLException{
		
		List<Department> depts = DepartmentDao.getDepartments(dbCon, term, null, null);
		
		List<Pager> pagers = new ArrayList<Pager>();
		for(Department dept : depts){
			//Get the foods to this department.
			List<Food> foods = FoodDao.getPureFoods(dbCon, " AND DEPT.restaurant_id = " + dept.getRestaurantId() + 
														   " AND DEPT.dept_id = " + dept.getId() +
														   " AND FOOD.img IS NOT NULL " + 
														   " AND FOOD.status & " + Food.SELL_OUT + " = 0 ", 
													null);
			pagers.addAll(divideFoodsInfoPager(foods));
		}
		
		return pagers;
	}
	
	/**
	 * Divide the foods into pagers
	 * @param foodsToDivied the foods to be divided
	 * @return a list holding the pagers.
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	private static List<Pager> divideFoodsInfoPager(List<Food> foodsToDivied) throws SQLException{
		
		if(!foodsToDivied.isEmpty()){
			
			int pageAmount = foodsToDivied.size() / AVERAGE_AMOUNT_PER_PAGE + (foodsToDivied.size() % AVERAGE_AMOUNT_PER_PAGE == 0 ? 0 : 1);
			
			List<Food> largeFoods = new ArrayList<Food>();
			List<Food> smallFoods = new ArrayList<Food>(foodsToDivied);
			
			/*
			 * 按照以下条件把菜品进行归类。
			 * 1、推荐，热销，特价的菜品划分到大分类
			 * 2、其余的菜品划分到小分类
			 */
			Iterator<Food> iter = smallFoods.iterator();
			while(iter.hasNext()){
				Food f = iter.next();
				if(f.isSpecial() || f.isHot() || f.isRecommend()){
					largeFoods.add(f);
					iter.remove();
				}
			}

			if(largeFoods.size() < pageAmount){
				
				/*
				 * 如果大分类菜品的数量小于平均分页数，
				 * 则将小分类中点菜数量最多的菜品补充到大分类，直到大分类的菜品数量等于平均分页数。
				 * 此步骤的用意是使得每个Pager都至少有一个大分类的菜品。
				 */
				Collections.sort(smallFoods, new Comparator<Food>(){
					@Override
					public int compare(Food f1, Food f2) {
						if(f1.statistics.getOrderCnt() < f2.statistics.getOrderCnt()){
							return 1;
						}else if(f1.statistics.getOrderCnt() > f2.statistics.getOrderCnt()){
							return -1;
						}else{
							return 0;
						}
					}
				});
				
				iter = smallFoods.iterator();
				while(iter.hasNext()){
					largeFoods.add(iter.next());
					iter.remove();
					if(largeFoods.size() == pageAmount){
						break;
					}
				}
				
			}else{
				/*
				 * 如果大分类菜品的数量多于平均分页数，则将大分类数量  / MAX_LARGE_AMOUNT_PER_PAGE作为Pager数。
				 * 此步骤的用意是使得大分类的菜品始终会以大图展示
				 */
				pageAmount = largeFoods.size() / MAX_LARGE_AMOUNT_PER_PAGE;
			}
			
			List<Pager> pagers = new ArrayList<Pager>();
			
			/*
			 * 按厨房和编号进行排序，同一个厨房下"热"、"特"、"荐"的菜品优先显示。
			 * 此步骤的用意是使得Pager中尽可能显示同一个厨房的菜品
			 */
			Comparator<Food> foodComp = new Comparator<Food>(){

				@Override
				public int compare(Food f1, Food f2) {
					if(f1.getKitchen().getAliasId() > f2.getKitchen().getAliasId()){
						return 1;
					}else if(f1.getKitchen().getAliasId() < f2.getKitchen().getAliasId()){
						return -1;
					}else{
						if(f1.isHot()){
							return -1;
						}else if(f2.isHot()){
							return 1;
						}else if(f1.isSpecial() || f1.isRecommend()){
							return -1;
						}else if(f2.isSpecial() || f2.isRecommend()){
							return 1;
						}else if(f1.getAliasId() > f2.getAliasId()){
							return 1;
						}else if(f1.getAliasId() < f2.getAliasId()){
							return -1;
						}else{
							return 0;
						}
					}
				};
			};
			
			Collections.sort(largeFoods, foodComp);
			Collections.sort(smallFoods, foodComp);
			
			int fromLargeIndex = 0;
			int fromSmallIndex = 0;
			for(int pageNo = 0; pageNo < pageAmount; pageNo++){
				
				List<Food> large = new ArrayList<Food>();
				List<Food> small = new ArrayList<Food>();
				
				if(fromSmallIndex <= smallFoods.size() && smallFoods.size() > 0){
					/*
					 * 如果小分类还有剩余，
					 * 则连同大分类按照"1 + X"的方式进行搭配
					 */
					int largeAmount = 1;
					large.addAll(largeFoods.subList(fromLargeIndex, fromLargeIndex + largeAmount));
					fromLargeIndex += largeAmount;
					
					int smallAmount = AVERAGE_AMOUNT_PER_PAGE - 1 + adjust(pageNo);
					int toSmallIndex = fromSmallIndex + smallAmount;
					small.addAll(smallFoods.subList(fromSmallIndex, toSmallIndex <= smallFoods.size() ? toSmallIndex : smallFoods.size()));
					fromSmallIndex += smallAmount;
					
				}else{
					
					/*
					 * 如果小分类已经用完，
					 * 则将大分类的图片分配到Pager中，
					 * 分配的原则是
					 */
					int largeAmount = MAX_LARGE_AMOUNT_PER_PAGE;
					int toLargeIndex = fromLargeIndex + largeAmount;
					large.addAll(largeFoods.subList(fromLargeIndex, toLargeIndex <= largeFoods.size() ? toLargeIndex : largeFoods.size()));
					fromLargeIndex += largeAmount;
					
				}

				pagers.add(new Pager(large.toArray(new Food[large.size()]),
									 null,
									 small.toArray(new Food[small.size()]),
									 null,
									 large.get(0)));
			}
			
			return pagers;
			
		}else{
			return new ArrayList<Pager>();
		}
	}
	
	
	private static int adjust(int pageNo){
		int remainder = pageNo % 3;
		if(remainder == 0){
			return 0;
		}else if(remainder == 1){
			return 1;
		}else{
			return -1;
		}
	}
	
	@Test
	public void testCalcFoodGroup() throws SQLException, BusinessException{
		Food[] foods = new Food[10];
		for(int i = 0; i < foods.length; i++){
			FoodStatistics statistics = new FoodStatistics(foods.length - i);
			foods[i] = new Food(0, i, 0);
			foods[i].setStatistics(statistics);
		}
		
		foods[8].setHot(true);
		foods[7].setRecommend(true);
		
		List<Food> largeFoods = new ArrayList<Food>();
		List<Food> smallFoods = new ArrayList<Food>();
		List<Pager> expectedPagers = new ArrayList<Pager>();

		largeFoods.clear();
		smallFoods.clear();

		Pager p1 = new Pager();
		largeFoods.add(foods[8]);
		smallFoods.add(foods[2]);
		smallFoods.add(foods[3]);
		p1.setCaptainFood(largeFoods.get(0));
		p1.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));
		p1.setSmallFoods(smallFoods.toArray(new Food[smallFoods.size()]));
		
		largeFoods.clear();
		smallFoods.clear();
		Pager p2 = new Pager();
		largeFoods.add(foods[7]);
		smallFoods.add(foods[4]);
		smallFoods.add(foods[5]);
		smallFoods.add(foods[6]);
		p2.setCaptainFood(largeFoods.get(0));
		p2.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));
		p2.setSmallFoods(smallFoods.toArray(new Food[smallFoods.size()]));

		largeFoods.clear();
		smallFoods.clear();
		Pager p3 = new Pager();
		largeFoods.add(foods[0]);
		smallFoods.add(foods[9]);
		p3.setCaptainFood(largeFoods.get(0));
		p3.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));
		p3.setSmallFoods(smallFoods.toArray(new Food[smallFoods.size()]));
		
		largeFoods.clear();
		smallFoods.clear();
		Pager p4 = new Pager();
		largeFoods.add(foods[1]);
		p4.setCaptainFood(largeFoods.get(0));
		p4.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));
		
		expectedPagers.add(p1);
		expectedPagers.add(p2);
		expectedPagers.add(p3);
		expectedPagers.add(p4);
		
		List<Pager> resultPagers = divideFoodsInfoPager(Arrays.asList(foods));

		Assert.assertArrayEquals(expectedPagers.toArray(), resultPagers.toArray());
	}
	
	@Test
	public void testCalcFoodGroup2() throws SQLException, BusinessException{
		Food[] foods = new Food[8];
		for(int i = 0; i < foods.length; i++){
			FoodStatistics statistics = new FoodStatistics(foods.length - i);
			foods[i] = new Food(0, i, 0);
			foods[i].setHot(true);
			foods[i].setStatistics(statistics);
		}
		
		List<Food> largeFoods = new ArrayList<Food>();
		List<Pager> expectedPagers = new ArrayList<Pager>();

		largeFoods.clear();

		Pager p1 = new Pager();
		largeFoods.add(foods[0]);
		largeFoods.add(foods[1]);
		p1.setCaptainFood(largeFoods.get(0));
		p1.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));
		
		largeFoods.clear();
		Pager p2 = new Pager();
		largeFoods.add(foods[2]);
		largeFoods.add(foods[3]);
		p2.setCaptainFood(largeFoods.get(0));
		p2.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));
		
		largeFoods.clear();
		Pager p3 = new Pager();
		largeFoods.add(foods[4]);
		largeFoods.add(foods[5]);
		p3.setCaptainFood(largeFoods.get(0));
		p3.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));

		largeFoods.clear();
		Pager p4 = new Pager();
		largeFoods.add(foods[6]);
		largeFoods.add(foods[7]);
		p4.setCaptainFood(largeFoods.get(0));
		p4.setLargeFoods(largeFoods.toArray(new Food[largeFoods.size()]));

		expectedPagers.add(p1);
		expectedPagers.add(p2);
		expectedPagers.add(p3);
		expectedPagers.add(p4);
		
		List<Pager> resultPagers = divideFoodsInfoPager(Arrays.asList(foods));

		Assert.assertArrayEquals(expectedPagers.toArray(), resultPagers.toArray());
	}
	
}
