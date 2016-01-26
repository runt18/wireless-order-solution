package com.wireless.pojo.restaurantMgr;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.token.RSACoder;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;


public class Restaurant implements Parcelable, Jsonable{
	
	public static class InsertBuilder{
		private final String account;
		private final String restaurantName;
		private final long expireDate;
		private final String pwd;
		private String restaurantInfo;
		private RecordAlive recordAlive = RecordAlive.HALF_A_YEAR;
		private String tele1;
		private String tele2;
		private String address;
		private int dianpingId;
		private final List<Module> modules = new ArrayList<Module>();
		private final RSACoder coder;
		
		public InsertBuilder(String account, String restaurantName, long expireDate, String pwd) throws NoSuchAlgorithmException{
			this.account = account;
			this.restaurantName = restaurantName;
			this.expireDate = expireDate;
			this.pwd = pwd;
			modules.add(new Module(Module.Code.BASIC));
			coder = new RSACoder();
		}
		
		public InsertBuilder setRestaurantInfo(String info){
			this.restaurantInfo = info;
			return this;
		}
		
		public InsertBuilder setRecordAlive(RecordAlive recordAlive){
			this.recordAlive = recordAlive;
			return this;
		}
		
		public InsertBuilder setTele1(String tele1){
			this.tele1 = tele1;
			return this;
		}
		
		public InsertBuilder setTele2(String tele2){
			this.tele2 = tele2;
			return this;
		}
		
		public InsertBuilder setAddress(String address){
			this.address = address;
			return this;
		}
		
		public String getPwd(){
			return this.pwd;
		}
		
		public InsertBuilder setDianpingId(int dianpingId){
			this.dianpingId = dianpingId;
			return this;
		}
		
		public InsertBuilder addModule(Module.Code code){
			Module module = new Module(code);
			if(!modules.contains(module)){
				modules.add(module);
			}
			return this;
		}
		
		public Restaurant build(){
			return new Restaurant(this);
		}
	}
	
	//The helper class to update a restaurant
	public static class UpdateBuilder{
		private final int id;
		private String account;
		private String restaurantName;
		private long expireDate;
		private String pwd;
		private String restaurantInfo;
		private RecordAlive recordAlive;
		private String tele1;
		private String tele2;
		private String address;
		private int dianpingId;
		private final List<Module> modules = SortedList.newInstance(); 
		private RSACoder coder;
		private String beeCloudAppId;
		private String beeCloudAppSecret;
		private List<Restaurant> branches;
		
		public UpdateBuilder(int id){
			this.id = id;
		}

		public boolean isBranchChanged(){
			return branches != null;
		}
		
		public UpdateBuilder clearBranch(){
			if(branches == null){
				branches = new ArrayList<Restaurant>();
			}
			return this;
		}
		
		public UpdateBuilder addBranch(Restaurant restaurant){
			if(branches == null){
				branches = new ArrayList<Restaurant>();
			}
			branches.add(restaurant);
			return this;
		}
		
		public UpdateBuilder addBranch(int restaurantId){
			return addBranch(new Restaurant(restaurantId));
		}
		
		public UpdateBuilder setAccount(String account){
			this.account = account;
			return this;
		}
		
		public boolean isAccountChanged(){
			return this.account != null;
		}
		
		public UpdateBuilder setRestaurantName(String name){
			this.restaurantName = name;
			return this;
		}
		
		public boolean isRestaurantNameChanged(){
			return this.restaurantName != null;
		}
		
		public UpdateBuilder setExpireDate(long expiredate){
			this.expireDate = expiredate;
			return this;
		}
		
		public boolean isExpireDateChanged(){
			return this.expireDate != 0;
		}
		
		public UpdateBuilder setDianpingId(int dianpingId){
			this.dianpingId = dianpingId;
			return this;
		}
		
		public boolean isDianpingIdChanged(){
			return this.dianpingId != -1;
		}
		
		public UpdateBuilder setPwd(String pwd){
			this.pwd = pwd;
			return this;
		}
		
		public boolean isPwdChanged(){
			return this.pwd != null;
		}
		
		public UpdateBuilder setRestaurantInfo(String info){
			this.restaurantInfo = info;
			return this;
		}

		public boolean isRestaurantInfoChanged(){
			return this.restaurantInfo != null;
		}
		
		public UpdateBuilder setRecordAlive(RecordAlive recordAlive){
			this.recordAlive = recordAlive;
			return this;
		}
		
		public boolean isRecordAliveChanged(){
			return this.recordAlive != null;
		}
		
		public UpdateBuilder setTele1(String tele1){
			this.tele1 = tele1;
			return this;
		}
		
		public boolean isTele1Changed(){
			return this.tele1 != null;
		}
		
		public UpdateBuilder setTele2(String tele2){
			this.tele2 = tele2;
			return this;
		}
		
		public boolean isTele2Changed(){
			return this.tele2 != null;
		}
		
		public UpdateBuilder setAddress(String address){
			this.address = address;
			return this;
		}
		
		public boolean isAddressChanged(){
			return this.address != null;
		}
		
		public String getPwd(){
			return this.pwd;
		}
		
		public int getId(){
			return id;
		}
		
		public UpdateBuilder setBeeCloud(String appId, String appSecret){
			this.beeCloudAppId = appId;
			this.beeCloudAppSecret = appSecret;
			return this;
		}
		
		public boolean isBeeCloudChanged(){
			return this.beeCloudAppId != null && this.beeCloudAppSecret != null;
		}
		
		public UpdateBuilder addModule(Module.Code code){
			Module module = new Module(code);
			if(!this.modules.contains(module)){
				modules.add(module);
			}
			return this;
		}
		
		public boolean isModuleChanged(){
			return !modules.isEmpty();
		}
		
		public UpdateBuilder resetRSA() throws NoSuchAlgorithmException{
			this.coder = new RSACoder();
			return this;
		}
		
		public boolean isRSAChanged(){
			return this.coder != null;
		}
		
		public Restaurant build(){
			return new Restaurant(this);
		}
	}
	
	public final static byte RESTAURANT_PARCELABLE_COMPLEX = 0;
	public final static byte RESTAURANT_PARCELABLE_SIMPLE = 1;
	
	//The reserved restaurant id
	public static final int ADMIN = 1;
	public static final int IDLE = 2;
	public static final int DISCARD = 3;
	public static final int RESERVED_1 = 4;
	public static final int RESERVED_2 = 5;
	public static final int RESERVED_3 = 6;
	public static final int RESERVED_4 = 7;
	public static final int RESERVED_5 = 8;
	public static final int RESERVED_6 = 9;
	public static final int RESERVED_7 = 10;
	
	public static enum Type{
		RESTAURANT(1, "餐厅"),
		GROUP(2, "集团"),
		BRANCE(3, "门店");
		
		private final int val;
		private final String desc;
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum RecordAlive{
		NEVER_EXPIRED(1, 0, "无限期"),
		THREE_MONTHS(2, 3600 * 24 * 90, "90天"),
		HALF_A_YEAR(3, 3600 * 24 * 180, "180天"),
		ONE_YEAR(4, 3600 * 24 * 360, "1年");
		
		private final int val;
		private final int aliveSeconds;
		private final String desc;
		
		RecordAlive(int val, int aliveSeconds, String desc){
			this.val = val;
			this.aliveSeconds = aliveSeconds;
			this.desc = desc;
		}
		
		public static RecordAlive valueOf(int val){
			for(RecordAlive recordAlive : values()){
				if(recordAlive.val == val){
					return recordAlive;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public static RecordAlive valueOfSeconds(int val){
			for(RecordAlive recordAlive : values()){
				if(recordAlive.aliveSeconds == val){
					return recordAlive;
				}
			}
			throw new IllegalArgumentException("The aliveSeconds(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public int getSeconds(){
			return aliveSeconds;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return "RecordAlive(val = " + val + ",desc = " + desc + ")";
		}
		
	}
	
	private int id;
	private String account;
	private String restaurantName;
	private String restaurantInfo;
	private int recordAlive;
	private String tele1;
	private String tele2;
	private String address;
	private float liveness;
	private long birthDate;
	private long expireDate;
	private int dianpingId;
	private Type type;
	private String publicKey;
	private String privateKey;
	private String beeCloudAppId;
	private String beeCloudAppSecret;
	private final List<Module> modules = new ArrayList<Module>();
	private final List<Restaurant> branches = new ArrayList<Restaurant>();
	
	public Restaurant(){
		
	}
	
	public Restaurant(int id){
		this.id = id;
	}
	
	private Restaurant(InsertBuilder builder){
		setAccount(builder.account);
		setName(builder.restaurantName);
		setInfo(builder.restaurantInfo);
		setRecordAlive(builder.recordAlive.getSeconds());
		setTele1(builder.tele1);
		setTele2(builder.tele2);
		setAddress(builder.address);
		setExpireDate(builder.expireDate);
		setDianpingId(builder.dianpingId);
		String now = new SimpleDateFormat(DateUtil.Pattern.DATE.getPattern(), Locale.getDefault()).format(new Date());
		try {
			setBirthDate(new SimpleDateFormat(DateUtil.Pattern.DATE.getPattern(), Locale.getDefault()).parse(now).getTime());
		} catch (ParseException ignored) {}
		setModule(builder.modules);
		setPublicKey(builder.coder.getPublicKey());
		setPrivateKey(builder.coder.getPrivateKey());
	}
	
	private Restaurant(UpdateBuilder builder){
		setId(builder.id);
		if(builder.isAccountChanged()){
			setAccount(builder.account);
		}
		if(builder.isDianpingIdChanged()){
			setDianpingId(builder.dianpingId);
		}
		if(builder.isRestaurantNameChanged()){
			setName(builder.restaurantName);
		}
		if(builder.isRestaurantInfoChanged()){
			setInfo(builder.restaurantInfo);
		}
		if(builder.isRecordAliveChanged()){
			setRecordAlive(builder.recordAlive.getSeconds());
		}
		if(builder.isTele1Changed()){
			setTele1(builder.tele1);
		}
		if(builder.isTele2Changed()){
			setTele2(builder.tele2);
		}
		if(builder.isAddressChanged()){
			setAddress(builder.address);
		}
		if(builder.isExpireDateChanged()){
			setExpireDate(builder.expireDate);
		}
		String now = new SimpleDateFormat(DateUtil.Pattern.DATE.getPattern(), Locale.getDefault()).format(new Date());
		try {
			setBirthDate(new SimpleDateFormat(DateUtil.Pattern.DATE.getPattern(), Locale.getDefault()).parse(now).getTime());
		} catch (ParseException ignored) {
			
		}
		if(builder.isModuleChanged()){
			setModule(builder.modules);
		}
		if(builder.isBranchChanged()){
			setBranches(builder.branches);
		}
		if(builder.isRSAChanged()){
			setPublicKey(builder.coder.getPublicKey());
			setPrivateKey(builder.coder.getPrivateKey());
		}
		if(builder.isBeeCloudChanged()){
			setBeeCloudAppId(builder.beeCloudAppId);
			setBeeCloudAppSecret(builder.beeCloudAppSecret);
		}
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		if(account != null){
			this.account = account;
		}
	}
	
	public String getName() {
		if(restaurantName == null){
			return "";
		}
		return restaurantName;
	}
	
	public void setName(String restaurantName) {
		if(restaurantName != null){
			this.restaurantName = restaurantName;
		}
	}
	
	public int getDianpingId(){
		return this.dianpingId;
	}
	
	public void setDianpingId(int dianpingId){
		this.dianpingId = dianpingId;
	}
	
	public String getInfo() {
		if(restaurantInfo == null){
			return "";
		}
		return restaurantInfo;
	}
	
	public void setInfo(String restaurantInfo) {
		if(restaurantInfo != null){
			this.restaurantInfo = restaurantInfo;
		}
	}
	
	public int getRecordAlive() {
		return recordAlive;
	}
	
	public void setRecordAlive(int recordAlive) {
		this.recordAlive = recordAlive;
	}
	
	public void setRecordAlive(RecordAlive alive){
		if(alive != null){
			this.recordAlive = alive.getSeconds();
		}
	}
	
	public String getTele1() {
		if(tele1 == null){
			return "";
		}
		return tele1;
	}
	
	public void setTele1(String tele1) {
		if(tele1 != null){
			this.tele1 = tele1;
		}
	}
	
	public String getTele2() {
		if(tele2 == null){
			return "";
		}
		return tele2;
	}
	
	public void setTele2(String tele2) {
		if(tele2 != null){
			this.tele2 = tele2;
		}
	}
	
	public String getAddress() {
		if(address == null){
			return "";
		}
		return address;
	}
	
	public void setAddress(String address) {
		if(address != null){
			this.address = address;
		}
	}
	
	public float getLiveness(){
		return this.liveness;
	}
	
	public void setBirthDate(long birthDate){
		this.birthDate = birthDate;
	}
	
	public long getBirthDate(){
		return this.birthDate;
	}
	
	public void setLiveness(float liveness){
		if(liveness < 0 || liveness > 1){
			throw new IllegalArgumentException("The liveness must be ranged from 0 to 1.");
		}
		this.liveness = liveness;
	}
	
	public long getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(long expireDate) {
		if(expireDate != 0){
			this.expireDate = expireDate;
		}
	}

	public Type getType(){
		return this.type;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public String getPublicKey(){
		return this.publicKey;
	}
	
	public void setPublicKey(String publicKey){
		this.publicKey = publicKey;
	}
	
	public boolean hasRSA(){
		return this.publicKey != null && this.privateKey != null;
	}
	
	public String getPrivateKey(){
		return this.privateKey;
	}
	
	public void setPrivateKey(String privateKey){
		this.privateKey = privateKey;
	}
	
	public String getBeeCloudAppId(){
		return this.beeCloudAppId;
	}
	
	public void setBeeCloudAppId(String appId){
		this.beeCloudAppId = appId;
	}
	
	public String getBeeCloudAppSecret(){
		return this.beeCloudAppSecret;
	}
	
	public void setBeeCloudAppSecret(String appSecret){
		this.beeCloudAppSecret = appSecret;
	}
	
	public boolean hasBeeCloud(){
		return this.beeCloudAppId != null;
	}
	
	public void addModule(Module module){
		if(!modules.contains(module)){
			modules.add(module);
		}
	}
	
	public void setModule(List<Module> modules){
		this.modules.clear();
		for(Module module : modules){
			addModule(module);
		}
	}
	
	public List<Module> getModules(){
		return Collections.unmodifiableList(modules);
	}
	
	public boolean hasModule(Module.Code code){
		return modules.contains(new Module(code));
	}
	
	public void setBranches(List<Restaurant> branches){
		this.branches.clear();
		for(Restaurant branch : branches){
			addBranch(branch);
		}
	}
	
	public void addBranch(Restaurant restaurant){
		this.branches.add(restaurant);
	}
	
	public List<Restaurant> getBranches(){
		return Collections.unmodifiableList(this.branches);
	}
	
	public boolean hasBranches(){
		return !this.branches.isEmpty();
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Restaurant)){
			return false;
		}else{
			return id == ((Restaurant)obj).id;
		}
	}
	
	@Override 
	public String toString(){
		return "restaurant(id = " + id + ", name = " + getName() + ")";
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == RESTAURANT_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == RESTAURANT_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeString(this.restaurantName);
			dest.writeString(this.restaurantInfo);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == RESTAURANT_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == RESTAURANT_PARCELABLE_COMPLEX){
			this.id = source.readInt();
			this.restaurantName = source.readString();
			this.restaurantInfo = source.readString();
		}
	}
	
	public final static Parcelable.Creator<Restaurant> CREATOR = new Parcelable.Creator<Restaurant>() {
		
		public Restaurant[] newInstance(int size) {
			return new Restaurant[size];
		}
		
		public Restaurant newInstance() {
			return new Restaurant();
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		String moduleDescs = "";
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("account", this.account);
		jm.putString("name", this.restaurantName);
		
		if(flag == RESTAURANT_PARCELABLE_COMPLEX){
			jm.putString("info", this.restaurantInfo);
			jm.putString("tele1", this.tele1);
			jm.putString("tele2", this.tele2);
			jm.putInt("dianping", this.dianpingId);
			jm.putString("address", this.address);
			jm.putFloat("liveness", this.liveness);
			jm.putJsonableList("modules", this.modules, 0);
			if(this.modules.size() > 0){
				moduleDescs = "";
				for (Module module : this.modules) {
					if(moduleDescs == ""){
						moduleDescs += module.getCode().getDesc();
					}else{
						moduleDescs += ("，" + module.getCode().getDesc());
					}
				}
			}
			jm.putString("moduleDescs", moduleDescs);
			jm.putInt("recordAliveValue", RecordAlive.valueOfSeconds(this.recordAlive).getVal());
			jm.putString("recordAliveText", RecordAlive.valueOfSeconds(this.recordAlive).getDesc());
			jm.putString("birthDate", DateUtil.formatToDate(this.birthDate));
			jm.putString("expireDate", DateUtil.formatToDate(this.expireDate));
			
			if(this.type == Restaurant.Type.GROUP){
				jm.putJsonableList("branches", this.getBranches(), flag);
			}
		}
		jm.putInt("typeVal", this.type.val);
		jm.putString("typeText", this.type.desc);

		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
