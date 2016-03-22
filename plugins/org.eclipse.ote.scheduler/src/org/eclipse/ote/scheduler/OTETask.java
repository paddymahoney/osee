package org.eclipse.ote.scheduler;

import java.util.concurrent.Callable;

public class OTETask implements Callable<OTETaskResult>, Comparable<OTETask>{

   private long time = 0;
   private Runnable r;
   private OTETaskResult result;
   private int period;
   private final boolean isScheduled;
   private volatile boolean complete = false;
   private boolean canceled;
   private boolean isMainThread = false;
   private Stopwatch stopwatch;
   
   public OTETask(Runnable runnable, int period){
      isScheduled = true;
      this.r = runnable;
      this.period = period;
      result = new OTETaskResult();
      stopwatch = new Stopwatch(runnable.toString());
   }
   
   public OTETask(Runnable runnable, long time) {
      isScheduled = false;
      this.time = time;
      this.r = runnable;
      this.period = 0;
      result = new OTETaskResult();
      stopwatch = new Stopwatch(runnable.toString());
   }

   public long getTime() {
      return time;
   }

   @Override
   public OTETaskResult call() throws Exception {
      stopwatch.start();
      result.th = null;
      try{
         if(!canceled){
            r.run();
         }
      } catch (Throwable th){
         result.th = th;
      } 
      complete  = true;
      stopwatch.stop();
      result.elapsedTime = stopwatch.getLastElapsedTime();
      return result;
   }
   
   public boolean isComplete(){
      return complete;
   }
   
   public boolean isScheduled() {
      return isScheduled;
   }

   public long period() {
      return period;
   }
   
   public void cancel(){
      canceled = true;
   }

   public void setNextTime(long l) {
      time = l;      
   }

   @Override
   public int compareTo(OTETask o) {
      if(equals(o)){
         return 0;
      }
      long delta = getTime() - o.getTime();
      if(delta > 0){
         return 1;
      } else if (delta < 0){
         return -1;
      } else {
         long periodDelta = period() - o.period();
         if(periodDelta > 0){
            return 1;
         } else if (periodDelta < 0){
            return -1;
         } else {
            return hashCode() - o.hashCode();
         }
      }
   }

   void setCanceled() {
      canceled = true;
   }
   
   public String toString(){
      return String.format("%d %d %s %s", this.time, period, r.toString(), stopwatch.toString());
   }

   public void setMain(boolean isMainThread) {
      this.isMainThread = isMainThread;
   }
   
   public boolean isMainThread(){
      return isMainThread;
   }
}
