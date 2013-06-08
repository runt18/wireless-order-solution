package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.req.ReqPing;
import com.wireless.pack.req.RequestPackage;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class PrinterConnections {

	private final static PrinterConnections ONLY_INSTANCE = new PrinterConnections();
	
	private final ConcurrentMap<Restaurant, List<Socket>> mConnections = new ConcurrentHashMap<Restaurant, List<Socket>>();
	
	private PrinterConnections(){
		
	}
	
	public static PrinterConnections instance(){
		return ONLY_INSTANCE;
	}
	
	/**
	 * Get the associated sockets according to specified restaurant id.
	 * @param restaurantId
	 * @return the associated sockets
	 */
	public List<Socket> get(int restaurantId){
		return get(new Restaurant(restaurantId));
	}
	
	/**
	 * Get the associated sockets according to specified restaurant.
	 * @param restaurant
	 * @return the associated sockets
	 */
	public List<Socket> get(Restaurant restaurant){
		List<Socket> result = mConnections.get(restaurant);
		if(result != null){
			return result;
		}else{
			return Collections.emptyList();
		}
	}
	
	/**
	 * Add the socket to restaurant
	 * @param restaurant the socket added to 
	 * @param sockToAdd the socket to add
	 */
	public void add(Restaurant restaurant, Socket sockToAdd){
		List<Socket> socks = mConnections.get(restaurant);
		if(socks != null){
			socks.add(sockToAdd);
		}else{
			socks = new CopyOnWriteArrayList<Socket>();
			socks.add(sockToAdd);
			mConnections.put(restaurant, socks);
		}
	}
	
	/**
	 * Remove the socket from specified restaurant.
	 * @param restaurantId the socket removed from
	 * @param sockToRemove the socket to remove
	 * @return true if the socket to remove is contained in restaurant before, otherwise false
	 */
	public boolean remove(int restaurantId, Socket sockToRemove){
		return remove(new Restaurant(restaurantId), sockToRemove);
	}
	
	/**
	 * Remove the socket from specified restaurant.
	 * @param restaurant the socket removed from
	 * @param sockToRemove the socket to remove
	 * @return true if the socket to remove is contained in restaurant before, otherwise false
	 */
	public boolean remove(Restaurant restaurant, Socket sockToRemove){
		List<Socket> socks = mConnections.get(restaurant);
		if(socks != null){
			return socks.remove(sockToRemove);
		}else{
			return false;
		}
	}
	
	public Collection<Entry<Restaurant, List<Socket>>> stat(){
		return Collections.unmodifiableCollection(mConnections.entrySet());
	}
	
	/**
	 * Scan the sockets to the specified restaurant and remove it in case of invalid.
	 * @param restaurant
	 */
	public void scan(Restaurant restaurant){
		List<Socket> socks = get(restaurant);
		for(Socket sock : socks){
			try{
				new ReqPing(RequestPackage.EMPTY_PIN).writeToStream(sock.getOutputStream());
				new ProtocolPackage().readFromStream(sock.getInputStream(), 3 * 1000);
				
			}catch(IOException e){
				try{
					sock.close();
				}catch(IOException ex){
					
				}finally{
					socks.remove(sock);
				}
			}
		}
	}
	
}
