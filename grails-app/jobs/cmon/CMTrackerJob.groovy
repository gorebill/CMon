package cmon

import org.hyperic.sigar.Sigar;

import com.cmon.entities.CMSegment;



class CMTrackerJob {
	
	def grailsApplication;
	
	def static segmentCache=[];
			
	def static lastDiskR=0;
	def static lastDiskW=0;
	
    static triggers = {
      simple name: 'trackerTrigger', startDelay:5000l, repeatInterval: 1000l // execute job once in 5 seconds
    }

    def execute() {
        // execute job
		
		if(grailsApplication.config.cmjob.isRunning) {
			
			def sigar=new Sigar();
			
			def mem=sigar.getMem();
			def memRam=mem.getRam();
			def memUsed=mem.getUsed();
			def memActualUsed=mem.getActualUsed();
			def memTotal=mem.getTotal();
			def memFree=mem.getFree();
			def memActualFree=mem.getActualFree();
			def memUsedPrecent=mem.getUsedPercent();
			
			def cpu=sigar.getCpuPerc();
			def cpuIdle=cpu.getIdle();
			def cpuUser=cpu.getUser();
			def cpuSys=cpu.getSys();
			def cpuWait=cpu.getWait();
			def cpuCombined=cpu.getCombined();
			
			def diskR=0;
			def diskW=0;
			sigar.getFileSystemList()?.each {
				// fs usage
				def fs=sigar.getFileSystemUsage(it.dirName);
				def fsTotal=fs.getTotal();
				def fsFree=fs.getFree();
				def fsUsed=fs.getUsed();
				
				diskR+=fs.getDiskReads();
				diskW+=fs.getDiskWrites();
			}
			
			def segment=new CMSegment(
				segmentMemTotal: memTotal,
				segmentMemUsed: memActualUsed,
				segmentMemFree: memActualFree,
				
				segmentCpuCombined: cpuCombined,
				segmentCpuUser:cpuUser,
				segmentCpuSys: cpuSys,
				
				segmentDiskR: (lastDiskR!=0 && diskR>lastDiskR?diskR-lastDiskR:0),
				segmentDiskW: (lastDiskW!=0 && diskW>lastDiskW?diskW-lastDiskW:0)
			);
		
			segmentCache.add(segment);
			
			lastDiskR=diskR;
			lastDiskW=diskW;
			
			if(segmentCache.size() >= 60) {
				
				def totalCount=segmentCache.size();
				
				def seg=new CMSegment(segmentMemTotal: memTotal);
				for(int i=0; i<totalCount; i++) {
					seg.segmentMemUsed += (segmentCache[i].segmentMemUsed/totalCount);
					seg.segmentMemFree += (segmentCache[i].segmentMemFree/totalCount);
					
					seg.segmentCpuUser += (segmentCache[i].segmentCpuUser/totalCount);
					seg.segmentCpuSys += (segmentCache[i].segmentCpuSys/totalCount);
					seg.segmentCpuCombined += (segmentCache[i].segmentCpuCombined/totalCount);
					
					seg.segmentDiskR += (segmentCache[i].segmentDiskR);
					seg.segmentDiskW += (segmentCache[i].segmentDiskW);
				}
				
				seg?.save();
				
				segmentCache.clear();
			}
		}else{
			segmentCache.clear();
		}
    }
	
	
	
	
}









