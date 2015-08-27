package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.stockMgr.StockTakeDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeptError;
import com.wireless.exception.MaterialError;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTake.InsertStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTake.Status;
import com.wireless.pojo.stockMgr.StockTake.UpdateStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.stockMgr.StockTakeDetail.InsertStockTakeDetail;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStockTake {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getByRestaurant(37).get(0);
			//mTerminal.restaurantID = 26;
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	//期望值与真实值的比较
	private void compareStockAction(StockAction expected, StockAction actual, boolean isIncludeDetail){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("deptIn", expected.getDeptIn().getId(), actual.getDeptIn().getId());
		Assert.assertEquals("deptInName", expected.getDeptIn().getName(), actual.getDeptIn().getName());
		Assert.assertEquals("operatorId", expected.getOperatorId(), actual.getOperatorId());
		Assert.assertEquals("operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals("approverId", expected.getApproverId(), actual.getApproverId());
		Assert.assertEquals("approver", expected.getApprover(), actual.getApprover());
		Assert.assertEquals("amount", Math.abs(expected.getTotalAmount()), actual.getTotalAmount(),0.0001F);
		Assert.assertEquals("price", Math.abs(expected.getTotalPrice()), actual.getTotalPrice(),0.0001F);
		Assert.assertEquals("status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("cateType", expected.getCateType(), actual.getCateType());
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("subType", expected.getSubType(), actual.getSubType());
		if(isIncludeDetail){
			for(StockActionDetail expectedDetail : expected.getStockDetails()){
				int index = actual.getStockDetails().indexOf(expectedDetail);
				if(index >= 0){
					Assert.assertEquals("associated material id to detail", expectedDetail.getMaterialId(), actual.getStockDetails().get(index).getMaterialId());
					Assert.assertEquals("associated material name to detail", expectedDetail.getName(), actual.getStockDetails().get(index).getName());
					Assert.assertEquals("price to detail", expectedDetail.getPrice(), actual.getStockDetails().get(index).getPrice(), 0.001);
					Assert.assertEquals("amount to detail", expectedDetail.getAmount(), actual.getStockDetails().get(index).getAmount(), 0.001);
				}else{
					Assert.assertTrue("stock action detail", false);
				}
			}
		}
		
	}
	
	//比较
	public void compare(StockTake expected, StockTake actual, boolean isIncludeStockTakeDetail){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("deptId", expected.getDept().getId(), actual.getDept().getId());
		Assert.assertEquals("deptName", expected.getDept().getName(), actual.getDept().getName());
		Assert.assertEquals("materialCateId",expected.getCateType(), actual.getCateType());
		Assert.assertEquals("status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("cateId", expected.getMaterialCate().getId(), actual.getMaterialCate().getId());
		Assert.assertEquals("operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals("operatorId", expected.getOperatorId(), actual.getOperatorId());
		Assert.assertEquals("comment", expected.getComment(), actual.getComment());
		
		if(isIncludeStockTakeDetail){
			
			for (StockTakeDetail stockTakeDetail : expected.getStockTakeDetails()) {
				int index = actual.getStockTakeDetails().indexOf(stockTakeDetail);
				if(index >= 0){
					Assert.assertEquals("id", stockTakeDetail.getId(), actual.getStockTakeDetails().get(index).getId());
					Assert.assertEquals("stockTakeId", stockTakeDetail.getStockTakeId(), actual.getStockTakeDetails().get(index).getStockTakeId());
					Assert.assertEquals("materialId", stockTakeDetail.getMaterial().getId(), actual.getStockTakeDetails().get(index).getMaterial().getId());
					Assert.assertEquals("materialName", stockTakeDetail.getMaterial().getName(), actual.getStockTakeDetails().get(index).getMaterial().getName());
					Assert.assertEquals("actualAmount", stockTakeDetail.getActualAmount(), actual.getStockTakeDetails().get(index).getActualAmount(), 0.0001f);
					Assert.assertEquals("expectAmount", stockTakeDetail.getExpectAmount(), actual.getStockTakeDetails().get(index).getExpectAmount(), 0.0001f);
					Assert.assertEquals("deltaAmount", stockTakeDetail.getDeltaAmount(), actual.getStockTakeDetails().get(index).getDeltaAmount(), 0.0001f);
				}else{
					Assert.assertTrue("stockTake in detail", false);
				}
			}
		}
		
		
	}
	@Test
	public void testBeforeAudit() throws SQLException, BusinessException{
		//先进行判断是否有遗漏
		StockTakeDao.beforeAudit(mStaff, 5);
		//System.out.println("num"+lost);
/*		int result = StockTakeDao.keepOrReset(mTerminal, 0, uBuilder);
		System.out.println("result"+result);*/
	}
	@Test
	public void testStockTake() throws SQLException, BusinessException{

		
		Department dept;
		List<Department> depts = DepartmentDao.getDepartments4Inventory(mStaff);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			dept = depts.get(3);
		}
		System.out.println(dept.getName());
//		Map<Object, Object> params = new HashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId());
		List<Material> materials = MaterialDao.getByCond(mStaff, null);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
		int cokeId = materials.get(0).getId();
		int spriteId = materials.get(2).getId();
		float cokeAmount = 0;
		float spriteAmount = 0;
		List<MaterialDept> materialDepts;
		//获取可乐的库存数量
		System.out.println("id"+cokeId+ "sid"+spriteId);
		materialDepts = MaterialDeptDao.getMaterialDepts(mStaff, " AND dept_id = " + dept.getId() + " AND material_id = " + cokeId, null);
		if(!materialDepts.isEmpty()){
			cokeAmount = materialDepts.get(0).getStock();
		}else{
			throw new BusinessException(StockError.MATERIAL_DEPT_ADD);
		}
		//获取雪碧的库存数量
		materialDepts = MaterialDeptDao.getMaterialDepts(mStaff, " AND dept_id = " + dept.getId() + " AND material_id = " + spriteId, null);
		if(!materialDepts.isEmpty()){
			spriteAmount = materialDepts.get(0).getStock();
		}else{
			throw new BusinessException(StockError.MATERIAL_DEPT_ADD);
		}
		//添加一张盘点单	
		InsertStockTakeBuilder builder = new InsertStockTakeBuilder(mStaff.getRestaurantId())
											.setCateType(MaterialCate.Type.GOOD)
											.setDept(dept)
											.setCateId(2)
											.setOperatorId((int) mStaff.getId()).setOperator(mStaff.getName())
											.setComment("盘点1月份的")
											.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(0)).setExpectAmount(cokeAmount).setActualAmount(10).build())
											.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(3)).setExpectAmount(spriteAmount).setActualAmount(20).build());
		final int id = StockTakeDao.insertStockTake(mStaff, builder);
		//System.out.println("id"+id);
		StockTake expected = builder.build();
		//获取真实值
		StockTake actual = StockTakeDao.getStockTakeAndDetailById(mStaff, id);
		expected.getStockTakeDetails().get(0).setId(actual.getStockTakeDetails().get(0).getId());
		expected.getStockTakeDetails().get(1).setId(actual.getStockTakeDetails().get(1).getId());
		expected.getStockTakeDetails().get(0).setDeltaAmount(actual.getStockTakeDetails().get(0).getDeltaAmount());
		expected.getStockTakeDetails().get(1).setDeltaAmount(actual.getStockTakeDetails().get(1).getDeltaAmount());
		expected.setId(id);
		compare(expected, actual, false);
		//修改是解注释
/*		InsertStockTakeBuilder updateBuilder = new InsertStockTakeBuilder(mTerminal.restaurantID)
		.setCateType(CateType.GOOD)
		.setDept(dept)
		.setCateId(2)
		.setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
		.setComment("盘点10月份的")
		.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(0)).setExpectAmount(cokeAmount).setActualAmount(5).build())
		.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(1)).setExpectAmount(spriteAmount).setActualAmount(6).build());
		
		StockTakeDao.updateStockTake(mTerminal, actual.getId(), updateBuilder);*/
		
		//审核盘点
		expected = actual;
		expected.setApprover(mStaff.getName());
		expected.setApproverId((int) mStaff.getId());
		expected.setStatus(Status.AUDIT);
		//expected.setComment("盘点10月份的");
			
		UpdateStockTakeBuilder uBuilder = StockTake.UpdateStockTakeBuilder.newAudit(id)
								.setApproverId((int) mStaff.getId()).setApprover(mStaff.getName());
		//FIXME 应该返回更有意义的值
		//获取库单id的集合

		
		//测试审核时解注释
		List<Integer> stockActionIds = StockTakeDao.auditStockTake(mStaff, uBuilder);
		
		actual = StockTakeDao.getStockTakeById(mStaff, id);

		compare(expected, actual, false);
		
		//是否有添加新的库单,不是则有盘盈或盘亏
		if(!stockActionIds.isEmpty()){
			//库存分布对比
			for (int stockActionId : stockActionIds) {
				//获得对应的库单
				StockAction stockAction = StockActionDao.getStockAndDetailById(mStaff, stockActionId);
				//对比盘点后的数据
				for (StockTakeDetail stockTakeDetail : builder.getStockTakeDetails()) {
					for (StockActionDetail stockActionDetail : stockAction.getStockDetails()) {
						//盘点明细单有可能是收支平衡的,所以得先判断库单中是否存在此明细单
						if(stockTakeDetail.getMaterial().getId() == stockActionDetail.getMaterialId()){
							//获取盘点审核后对应的部门_原料表信息
							MaterialDept afterMaterialDept = MaterialDeptDao.getMaterialDepts(mStaff, " AND material_id = " + stockActionDetail.getMaterialId() + " AND dept_id = " + stockAction.getDeptIn().getId(), null).get(0);
							
							Map<Object, Object> afterParam = new HashMap<Object, Object>();
							afterParam.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId() + " AND M.material_id = " + stockActionDetail.getMaterialId());
							Material afterMaterial = MaterialDao.getByCond(mStaff, new MaterialDao.ExtraCond().setId(stockActionDetail.getMaterialId())).get(0);
							
							int index = materials.indexOf(afterMaterial);
							if(index >= 0){
								float deltaMaterialStock = Math.abs(afterMaterial.getStock() - materials.get(index).getStock());
								//对比原料表的变化
								Assert.assertEquals("deltaMaterialStock", Math.abs(stockTakeDetail.getTotalDelta()), deltaMaterialStock, 0.001);
							}else{
								throw new BusinessException(MaterialError.MATERIAL_NOT_EXIST);
							}
							//盘点的实际数量与审核后部门_原料表的储存量对比
							Assert.assertEquals("deltaMaterialDeptStock", stockTakeDetail.getActualAmount(), afterMaterialDept.getStock(), 0.001);
						}
					}
				}
			}
			
			
			
			//库单的对比
			Map<InsertBuilder, InsertBuilder> insertBuilders = new HashMap<InsertBuilder, InsertBuilder>();										
			
			InsertBuilder stockActionInsertMore = null;
			InsertBuilder stockActionInsertLess = null;
			//获取库单的期望值
			for (StockTakeDetail stockTakeDetail : expected.getStockTakeDetails()) {
				//获取对应的material
//				Map<Object, Object> param = new HashMap<Object, Object>();
//				param.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId() + " AND M.material_id = " + stockTakeDetail.getMaterial().getId());
				Material material = MaterialDao.getByCond(mStaff, new MaterialDao.ExtraCond().setId(stockTakeDetail.getMaterial().getId())).get(0);
				//获取库存明细单
				StockActionDetail stockActionDetail = new StockActionDetail();
				stockActionDetail.setMaterialId(stockTakeDetail.getMaterial().getId());
				stockActionDetail.setName(stockTakeDetail.getMaterial().getName());
				stockActionDetail.setPrice(material.getPrice());
				stockActionDetail.setAmount(stockTakeDetail.getDeltaAmount());
				//通过差额对应生成入库单或者出库单
				if(stockTakeDetail.getDeltaAmount() > 0){
					//差额大于0,则是盘盈
					stockActionInsertMore =  StockAction.InsertBuilder.newMore(mStaff.getRestaurantId())
														.setOperatorId(expected.getApproverId())
														.setOperator(expected.getApprover())
														.setDeptIn(expected.getDept())
														.setCateType(expected.getCateType().getValue());
					if(insertBuilders.get(stockActionInsertMore) == null){
						stockActionInsertMore.addDetail(stockActionDetail);
						insertBuilders.put(stockActionInsertMore, stockActionInsertMore);
					}else{
						insertBuilders.get(stockActionInsertMore).addDetail(stockActionDetail);
					}
				
				}else if(stockTakeDetail.getDeltaAmount() < 0){
					//差额小于0,则是盘亏
					stockActionInsertLess =  StockAction.InsertBuilder.newLess(mStaff.getRestaurantId())
														.setOperatorId(expected.getApproverId())
														.setOperator(expected.getApprover())
														.setDeptIn(expected.getDept())
														.setCateType(expected.getCateType().getValue());
					if(insertBuilders.get(stockActionInsertLess) == null){
						stockActionInsertLess.addDetail(stockActionDetail);
						insertBuilders.put(stockActionInsertLess, stockActionInsertLess);
					}else{
						insertBuilders.get(stockActionInsertLess).addDetail(stockActionDetail);
					}
					
				}
			}
			
			List<StockAction> lists = new ArrayList<StockAction>();
			//把入库或出库单集成
			if(insertBuilders.get(stockActionInsertMore) != null){
				lists.add(insertBuilders.get(stockActionInsertMore).build());
			}
			if(insertBuilders.get(stockActionInsertLess) != null){
				lists.add(insertBuilders.get(stockActionInsertLess).build());
			}
		
			for (int stockActionId : stockActionIds) {	
				//通过返回的id获取库单
				StockAction actualStockAction = StockActionDao.getStockAndDetailById(mStaff, stockActionId);
				for (StockAction expectedStockAction : lists) {
					if(expectedStockAction.getSubType() == actualStockAction.getSubType()){
						expectedStockAction.setId(stockActionId);
						expectedStockAction.setApproverId((int) mStaff.getId());
						expectedStockAction.setApprover(mStaff.getName());
						expectedStockAction.setStatus(com.wireless.pojo.stockMgr.StockAction.Status.AUDIT);
					
						//期望值与真实值比较
						compareStockAction(expectedStockAction, actualStockAction, false);
					}
				}
			}
	
		}else{
			throw new BusinessException(StockError.STOCKTAKE_BALANCE);
		}

		
		
		//删除
/*		StockTakeDao.deleteStockTakeById(mTerminal, id);
		
		try{
			StockTakeDao.getStockTakeById(mTerminal, id);
			Assert.assertTrue("delete stock in record(id = " + id + ") failed", false);
		}catch(Exception e){}*/
		
											
	}
	
	
	
	
	
	
}
