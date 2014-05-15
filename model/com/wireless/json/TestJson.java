package com.wireless.json;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Region;

public class TestJson {

	@Test
	public void testRegion(){
		List<Region> regions = new ArrayList<Region>();
		regions.add(new Region((short)0, "测试区域1", 37));
		regions.add(new Region((short)1, "测试区域2", 37));
		//System.out.println(new JsonPackage(regions, Region.REGION_JSONABLE_LEAF, Jsonable.Type.PAIR).toString());
		//System.out.println(new JsonPackage(regions, 0, Jsonable.Type.LIST).toString());
		//System.out.println(new JsonPackage(regions.get(0), 0, Jsonable.Type.PAIR).toString());
	}
	
	@Test 
	public void testJson4Order(){
		String jsonText = "[{\"id\":12301,\"alias\":101,\"name\":\"珍藏日本吉品鲍18头\",\"amount\":0,\"unitPrice\":980,\"commission\":0,\"restaurantId\":40,\"pinyin\":\"zzrbjpb18t\",\"desc\":\"\",\"status\":8,\"stockStatusValue\":2,\"stockStatusText\":\"商品出库\",\"tasteRefType\":1,\"kitchen\":{\"id\":1576,\"alias\":2,\"rid\":40,\"name\":\"鲍鱼\",\"isAllowTmp\":false,\"typeValue\":0,\"dept\":{\"rid\":40,\"id\":4,\"name\":\"厨房\",\"typeValue\":0}},\"count\":1,\"isHangup\":false,\"tasteGroup\":{\"tastePref\":\"加快,免做,中牌\",\"price\":490,\"normalTasteContent\":[{\"id\":1169,\"alias\":1169,\"rid\":40,\"name\":\"加快\",\"price\":0,\"rate\":0,\"rank\":2,\"cateValue\":156,\"cateText\":\"口味\",\"cateStatusValue\":2,\"cateStatusText\":\"口味\",\"calcValue\":0,\"calcText\":\"按价格\",\"typeValue\":0,\"typeText\":\"一般\"},{\"id\":1168,\"alias\":1168,\"rid\":40,\"name\":\"免做\",\"price\":0,\"rate\":0,\"rank\":1,\"cateValue\":156,\"cateText\":\"口味\",\"cateStatusValue\":2,\"cateStatusText\":\"口味\",\"calcValue\":0,\"calcText\":\"按价格\",\"typeValue\":0,\"typeText\":\"一般\"},{\"id\":1134,\"alias\":1134,\"rid\":40,\"name\":\"中牌\",\"price\":0,\"rate\":0.5,\"rank\":0,\"cateValue\":29,\"cateText\":\"规格\",\"cateStatusValue\":1,\"cateStatusText\":\"规格\",\"calcValue\":1,\"calcText\":\"按比例\",\"typeValue\":0,\"typeText\":\"一般\"}],\"normalTaste\":{\"name\":\"加快,免做,中牌\",\"price\":490}}},{\"isTemporary\":true,\"id\":\"353\",\"alias\":\"353\",\"name\":\"linshicai\",\"count\":1,\"unitPrice\":12,\"isHangup\":false,\"kitchen\":{\"id\":\"1578\"},\"tasteGroup\":{\"tastePref\":\"无口味\",\"price\":0,\"normalTasteContent\":[]}}]";
		System.out.println(JObject.parseList(OrderFood.JSON_CREATOR, OrderFood.OF_JSONABLE_4_COMMIT, jsonText));
	}
}
