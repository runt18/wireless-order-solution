package com.wireless.json;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
	
}
