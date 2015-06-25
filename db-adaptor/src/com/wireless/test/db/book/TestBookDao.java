package com.wireless.test.db.book;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.book.BookDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BookError;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestBookDao {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(40);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBook4Manual() throws SQLException, BusinessException{
		int bookId = 0;
		final List<Table> tables = TableDao.getByCond(mStaff, null, null);
		final List<Food> foods = FoodDao.getByCond(mStaff, null, null);
		final List<Taste> tastes = TasteDao.getByCond(mStaff, null, null);
		try{
			Table bookTbl1 = tables.get(0);
			Table bookTbl2 = tables.get(1);

			OrderFood of1 = new OrderFood();
			of1.asFood().copyFrom(foods.get(0));
			of1.addTaste(tastes.get(0));
			of1.setTmpTaste(Taste.newTmpTaste("测试口味1", 10));

			OrderFood of2 = new OrderFood();
			of2.asFood().copyFrom(foods.get(1));
			of2.addTaste(tastes.get(1));
			of2.setTmpTaste(Taste.newTmpTaste("测试口味2", 10));
			
			Book.InsertBuilder4Manual insertBuilder = new Book.InsertBuilder4Manual().setBookDate("2015-10-2 15:00:00")
																					 .setMember("张生")
																					 .setTele("18520590932")
																					 .setAmount(4)
																					 .setCategory(Book.Category.SINGLE.toString())
																					 .setReserved(60 * 30)
																					 .setComment("测试备注")
																					 .setStaff(mStaff)
																					 .setMoney(1000)
																					 .addTable(bookTbl1).addTable(bookTbl2)
																					 .addOrderFood(of1, mStaff)
																					 .addOrderFood(of2, mStaff);
			bookId = BookDao.insert(mStaff, insertBuilder);
			
			Book expected = insertBuilder.build();
			expected.setId(bookId);
			Book actual = BookDao.getById(mStaff, bookId);
			
			compare(expected, actual);
			
		}finally{
			if(bookId != 0){
				BookDao.deleteById(mStaff, bookId);
				try{
					BookDao.getById(mStaff, bookId);
					Assert.assertTrue("failed to delete the book record", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the book record", BookError.BOOK_RECORD_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	@Test
	public void testBook4Weixin() throws SQLException, BusinessException{
		int bookId = 0;
		final List<Table> tables = TableDao.getByCond(mStaff, null, null);
		final List<Food> foods = FoodDao.getByCond(mStaff, null, null);
		final List<Taste> tastes = TasteDao.getByCond(mStaff, null, null);
		try{
			OrderFood of1 = new OrderFood();
			of1.asFood().copyFrom(foods.get(0));
			of1.addTaste(tastes.get(0));
			of1.setTmpTaste(Taste.newTmpTaste("测试口味1", 10));

			OrderFood of2 = new OrderFood();
			of2.asFood().copyFrom(foods.get(1));
			of2.addTaste(tastes.get(1));
			of2.setTmpTaste(Taste.newTmpTaste("测试口味2", 10));
			
			Book.InsertBuilder4Weixin insertBuilder = new Book.InsertBuilder4Weixin().setBookDate("2015-10-2 15:00:00")
																					 .setMember("张生")
																					 .setTele("18520590932")
																					 .setAmount(4)
																					 .setRegion("宏图")
																					 .addOrderFood(of1, mStaff)
																					 .addOrderFood(of2, mStaff);
			bookId = BookDao.insert(mStaff, insertBuilder);
			
			Book expected = insertBuilder.build();
			expected.setId(bookId);
			Book actual = BookDao.getById(mStaff, bookId);
			
			compare(expected, actual);
			
			Table bookTbl1 = tables.get(0);
			Table bookTbl2 = tables.get(1);
			Book.ConfirmBuilder confirmBuilder = new Book.ConfirmBuilder(bookId).setCategory(Book.Category.SINGLE.toString())
																				.setReserved(60 * 30)
																				.setComment("测试备注")
																				.setStaff(mStaff)
																				.setMoney(1000)
																				.addTable(bookTbl1).addTable(bookTbl2);
			BookDao.confirm(mStaff, confirmBuilder);
			actual = BookDao.getById(mStaff, bookId);
			
			expected.setCategory(confirmBuilder.build().getCategory());
			expected.setReserved(confirmBuilder.build().getReserved());
			expected.setComment(confirmBuilder.build().getComment());
			expected.setStaff(confirmBuilder.build().getStaff());
			expected.setTables(confirmBuilder.build().getTables());
			expected.setMoney(confirmBuilder.build().getMoney());
			
			compare(expected, actual);
			
		}finally{
			if(bookId != 0){
				BookDao.deleteById(mStaff, bookId);
				try{
					BookDao.getById(mStaff, bookId);
					Assert.assertTrue("failed to delete the book record", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the book record", BookError.BOOK_RECORD_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(Book expected, Book actual){
		Assert.assertEquals("book id", expected.getId(), actual.getId());
		Assert.assertEquals("book date", expected.getBookDate(), actual.getBookDate());
		Assert.assertEquals("book reserved", expected.getReserved(), actual.getReserved());
		Assert.assertEquals("book member", expected.getMember(), actual.getMember());
		Assert.assertEquals("book member id", expected.getMemberId(), actual.getMemberId());
		Assert.assertEquals("book telephone", expected.getTele(), actual.getTele());
		Assert.assertEquals("book amount", expected.getAmount(), actual.getAmount());
		Assert.assertEquals("book region", expected.getRegion(), actual.getRegion());
		Assert.assertEquals("book source", expected.getSource(), actual.getSource());
		Assert.assertEquals("book category", expected.getCategory(), actual.getCategory());
		Assert.assertEquals("book money", expected.getMoney(), actual.getMoney(), 0.01);
		Assert.assertEquals("book comment", expected.getComment(), actual.getComment());
		if(expected.getStatus() != Book.Status.CREATED){
			Assert.assertEquals("book staff name", expected.getStaff().getName(), actual.getStaff().getName());
			Assert.assertEquals("book staff id", expected.getStaff().getId(), actual.getStaff().getId());
		}
		//Compare the booked tables.
		Comparator<Table> comp4Tbl = new Comparator<Table>(){
			@Override
			public int compare(Table o1, Table o2) {
				if(o1.getId() > o2.getId()){
					return 1;
				}else if(o1.getId() < o2.getId()){
					return -1;
				}else{
					return 0;
				}
			}
		};
		final List<Table> expectedTbls = SortedList.newInstance(expected.getTables(), comp4Tbl);
		final List<Table> actualTbls = SortedList.newInstance(actual.getTables(), comp4Tbl);
		Assert.assertEquals("book tables", expectedTbls, actualTbls);
		
		//Compare the booked order foods.
		Assert.assertEquals("book order", expected.hasOrder(), actual.hasOrder());
		if(expected.hasOrder() && actual.hasOrder()){
			Comparator<OrderFood> comp = new Comparator<OrderFood>(){
				@Override
				public int compare(OrderFood arg0, OrderFood arg1) {
					return arg0.asFood().compareTo(arg1.asFood());
				}
			};
			List<OrderFood> expectedFoods = SortedList.newInstance(expected.getOrder().getOrderFoods(), comp);
			List<OrderFood> actualFoods = SortedList.newInstance(actual.getOrder().getOrderFoods(), comp);
			
			Assert.assertEquals("size to book order food", expectedFoods.size(), actualFoods.size());
			for(int i = 0; i < expectedFoods.size(); i++){
				Assert.assertEquals("basic info to book order food[" + i + "]", expectedFoods.get(i), actualFoods.get(i));
				Assert.assertEquals("order count to book order food[" + i + "]", expectedFoods.get(i).getCount(), actualFoods.get(i).getCount(), 0.01);
				Assert.assertEquals("food unit to book order food[" + i + "]", expectedFoods.get(i).getFoodUnit(), actualFoods.get(i).getFoodUnit());
				Assert.assertEquals("food taste group to book order food[" + i + "]", expectedFoods.get(i).getTasteGroup(), actualFoods.get(i).getTasteGroup());
			}
		}
	}
}
