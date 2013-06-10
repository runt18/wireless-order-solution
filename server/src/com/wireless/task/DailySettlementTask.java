package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.BitSet;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.foodAssociation.CalcFoodAssociationDao;
import com.wireless.db.foodStatistics.CalcFoodStatisticsDao;
import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.server.PrinterLosses;

/**
 * 
 * @author Ying.Zhang
 *
 */
public class DailySettlementTask extends SchedulerTask{
	
	@Override
	public void run() {
		final String sep = System.getProperty("line.separator");
		final StringBuffer taskInfo = new StringBuffer(); 
		taskInfo.append("Daily settlement task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date())).append(sep);
		
		try {   
			
			//Clean up the unprinted records
			PrinterLosses.instance().clear();
			
			//Perform daily settlement.
			DailySettleDao.Result result = DailySettleDao.exec();		
					
			taskInfo.append("info : " + result.getTotalOrder() + " record(s) are moved from \"order\" to \"order_history\"").append(sep);
			taskInfo.append("info : " + result.getTotalOrderDetail() + " record(s) are moved from \"order_food\" to \"order_food_history\"").append(sep);
			taskInfo.append("info : " + result.getTotalShift() + " record(s) are moved from \"shift\" to \"shift_history\"").append(sep);
			taskInfo.append("info : " + 
							"maxium order id : " + result.getMaxOrderId() + ", " +
							"maxium order food id : " + result.getMaxOrderFoodId() + ", " +
							"maxium shift id : " + result.getMaxShiftId()).append(sep);
			
			//Perform to smart taste calculation.
//			long beginTime = System.currentTimeMillis();
//			TasteRefDao.exec();
//			long elapsedTime = System.currentTimeMillis() - beginTime;
//			
//			taskInfo += "info : The calculation to smart taste reference takes " + elapsedTime / 1000 + " sec." + sep;			
			TaskParallelExecutor.Task tasteRefTask = new TaskParallelExecutor.Task() {				
				
				@Override
				public void run() throws SQLException, BusinessException {
					TasteRefDao.exec();					
				}
				
				@Override
				public String getTag() {
					return " the calculation to smart taste reference ";
				}
				
				@Override
				public int getId() {
					return 0;
				}
			};
			
			TaskParallelExecutor.Task foodAssociationTask = new TaskParallelExecutor.Task(){
				  
				@Override 
				public void run() throws SQLException{
					CalcFoodAssociationDao.exec();
				}

				@Override
				public int getId() {
					return 0;
				}

				@Override
				public String getTag() {
					return " food association ";
				}
				
			};

			//Perform to calculate the order count to each food from bill history
//			beginTime = System.currentTimeMillis();
//			int nRows = CalcFoodStatisticsDao.exec();
//			elapsedTime = System.currentTimeMillis() - beginTime;
			
			//taskInfo += "info : The statistics to " + nRows + " foods is calculated and takes " + elapsedTime / 1000 + " sec." + sep;
			
			TaskParallelExecutor.Task foodStatisticsTask = new TaskParallelExecutor.Task(){

				private int nRowsAffacted;
				
				@Override
				public void run() throws SQLException, BusinessException {
					nRowsAffacted = CalcFoodStatisticsDao.exec();
				}

				@Override
				public int getId() {
					return 0;
				}

				@Override
				public String getTag() {
					return " food statistics to " + nRowsAffacted + " foods ";
				}			
			};			

			new TaskParallelExecutor(new TaskParallelExecutor.Task[]{ tasteRefTask, foodStatisticsTask, foodAssociationTask }){
				protected void onPostExecute(TaskProxy[] tasks){
					for(TaskProxy taskProxy : tasks){
						taskInfo.append("info :" + taskProxy.getDesc()).append(sep);
					}
				}
			}.run();

				
		}catch(SQLException e){
			taskInfo.append("error : " + e.getMessage()).append(sep);
			e.printStackTrace();
			
		}catch(BusinessException e){
			taskInfo.append("error : " + e.getMessage()).append(sep);
			e.printStackTrace();
			
		}finally{
			
			//append to the log file
			taskInfo.append("***************************************************************").append(sep);
			try{
				File parent = new File("log/");
				if(!parent.exists()){
					parent.mkdir();
				}
				File logFile = new File("log/daily_settlement.log");
				if(!logFile.exists()){
					logFile.createNewFile();
				}
				FileWriter logWriter = new FileWriter(logFile, true);
				logWriter.write(taskInfo.toString());
				logWriter.close();
			}catch(IOException e){}
			
		}
	}
}

class TaskParallelExecutor{

	static interface Task{
		public void run() throws SQLException, BusinessException;
		public int getId();
		public String getTag();
	}
	
	private enum Status{
		READY,
		IN_PROGRESS,
		FAILED,
		FINISHED
	};
	
	class TaskProxy{		
		
		long mElapsedTime;
		
		Task mTaskImpl;
		
		Status mStatus = Status.READY;
		
		private int mBitIndex;
		
		private String mErrMsg;
		
		TaskProxy(Task taskImpl, int bitIndex){
			this.mTaskImpl = taskImpl;
			this.mBitIndex = bitIndex;
		}	

		/**
		 * Perform the run the task in a new thread.
		 * And notify the task executor till it finished.
		 */
		public void run() {
			new Thread(){
				@Override
				public void run(){
					mStatus = Status.IN_PROGRESS;
					
					long beginTime = System.currentTimeMillis();
					try{
						mTaskImpl.run();
						mStatus = Status.FINISHED;
						
					}catch(SQLException e){
						mStatus = Status.FAILED;
						mErrMsg = e.getMessage();
						
					}catch(BusinessException e){
						mStatus = Status.FAILED;
						mErrMsg = e.getMessage();
						
					}finally{
						mElapsedTime = System.currentTimeMillis() - beginTime;						
						
						mTaskBitSet.clear(mBitIndex);
						synchronized(mTaskBitSet){
							mTaskBitSet.notifyAll();
						}
						
					}
				}
			}.start();
		}	
		
		/**
		 * Get the description according the status of the task.
		 * @return the description of the task.
		 */
		String getDesc(){
			if(mStatus == Status.FAILED){
				return mTaskImpl.getTag() + "failed because " + mErrMsg + ".";
				
			}else if(mStatus == Status.FINISHED){				
				return mTaskImpl.getTag() + "takes " + mElapsedTime / 1000 + " secs.";
				
			}else if(mStatus == Status.READY){
				return mTaskImpl.getTag() + "is ready to run.";
				
			}else if(mStatus == Status.IN_PROGRESS){
				return mTaskImpl.getTag() + "is in progress...";
				
			}else{
				return "";
			}
		}
	};
	
	/**
	 * The bit set indicates whether each task is finished or NOT.
	 * true means READY, false means FINISHED.
	 */
	private BitSet mTaskBitSet = new BitSet();
	private TaskProxy[] mTasks;
	
	/**
	 * Initialize task vector and bit set of task executor.
	 * @param tasks
	 */
	public TaskParallelExecutor(Task[] tasks){
		mTasks = new TaskProxy[tasks.length];
		mTaskBitSet = new BitSet(tasks.length);
		for(int i = 0; i < tasks.length; i++){
			mTaskBitSet.set(i);
			mTasks[i] = new TaskProxy(tasks[i], i);
		}
	}
			
	public void run(){
		
		//Starts to run each task.
		for(TaskProxy taskProxy : mTasks){
			taskProxy.run();
		}

		//Wait until all the task has finished.
		while(mTaskBitSet.cardinality() != 0){
			synchronized(mTaskBitSet){
				try{
					mTaskBitSet.wait();
				}catch(InterruptedException e){
					
				}
			}
		}
		
		onPostExecute(mTasks);
	}
	
	protected void onPostExecute(TaskProxy[] tasks){
		for(TaskProxy taskProxy : mTasks){
			System.out.println(taskProxy.getDesc());
		}
	}

};
