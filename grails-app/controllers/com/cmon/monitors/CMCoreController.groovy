package com.cmon.monitors

import java.text.SimpleDateFormat;

import grails.converters.JSON;

import org.hyperic.sigar.Sigar;

import com.cmon.entities.CMSegment;

class CMCoreController {

    def index() {
		
		def sigar=new Sigar();
		
		def mem=sigar.getMem();
		def memRam=mem.getRam();
		def memTotal=mem.getTotal();
		def cpuNumber=sigar.getCpuInfoList().size();
		
		def diskInfo="";
		sigar.getFileSystemList()?.each {
			// fs usage
			def fs=sigar.getFileSystemUsage(it.dirName);
			def fsTotal=fs.getTotal();
			def fsFree=fs.getFree();
			def fsUsed=fs.getUsed();
			
			def diskR=fs.getDiskReads();
			def diskW=fs.getDiskWrites();
			
			def partionCount=0;
			while(fsTotal>=1024) {
				fsTotal/=1024f;
				partionCount++;
			}
			
			fsTotal=g.formatNumber(number:fsTotal, maxFractionDigits:2);
			
			def tmpStr="${it.devName}: ${fsTotal} ${['KB','MB','GB','TB'][partionCount] ?: 'Unknown unit'}";
			diskInfo="${diskInfo}<br>${tmpStr}"
		}
		
		[
			'jobRunning':grailsApplication.config.cmjob.isRunning,
			'memTotal': memTotal,
			'memRam': memRam,
			'diskInfo': diskInfo,
			'cpuNumber': cpuNumber
		]
	}
	
	def dataGetLastSegment() {
		def criteria = CMSegment.createCriteria();
		def result = criteria.list(max: 1) {//(max: max, offset: offset)
			order('dateCreated','desc')
		};
	
		def data=[];
		if(result.size()>0) {
			data=[
				'cpuUsedPercent': [result[0].dateCreated.time, result[0].segmentCpuCombined],
				'cpuUsedSysPercent': [result[0].dateCreated.time, result[0].segmentCpuSys],
				'cpuUsedUserPercent': [result[0].dateCreated.time, result[0].segmentCpuUser],
			
				'memFreePercent': [result[0].dateCreated.time, result[0].segmentMemFree/result[0].segmentMemTotal],
				'memUsedPercent': [result[0].dateCreated.time, result[0].segmentMemUsed/result[0].segmentMemTotal],
			
				'diskR': [result[0].dateCreated.time, result[0].segmentDiskR],
				'diskW': [result[0].dateCreated.time, result[0].segmentDiskW]
			];
		}
	
		render (contentType: "application/json", text: data as JSON)
	}
	
	def dataListSegments() {
		def firstDay=new Date().minus(30);
		
		def criteria = CMSegment.createCriteria();
		def result = criteria.list(max: 100) {//(max: max, offset: offset)
			and {
				ge('dateCreated', firstDay)
				
				//'in'("dateCreated",[18..65])
			}
			
			order('dateCreated','desc')
		};
	
		def dataset1=[];
		def dataset2=[];
		def dataset3=[];
		def dataset4=[];
		def dataset5=[];
		def dataset6=[];
		def dataset7=[];
		
		result?.each {
			dataset1.add([it.dateCreated.time, it.segmentCpuCombined]);
			dataset2.add([it.dateCreated.time, it.segmentCpuSys]);
			dataset3.add([it.dateCreated.time, it.segmentCpuUser]);
			
			dataset4.add([it.dateCreated.time, it.segmentMemFree/it.segmentMemTotal]);
			dataset5.add([it.dateCreated.time, it.segmentMemUsed/it.segmentMemTotal]);
			
			dataset6.add([it.dateCreated.time, it.segmentDiskR]);
			dataset7.add([it.dateCreated.time, it.segmentDiskW]);
		}
		
		def data=[
			'cpuUsedPercent': dataset1.reverse(),
			'cpuUsedSysPercent': dataset2.reverse(),
			'cpuUsedUserPercent': dataset3.reverse(),
			
			'memFreePercent': dataset4.reverse(),
			'memUsedPercent': dataset5.reverse(),
			
			'diskR': dataset6.reverse(),
			'diskW': dataset7.reverse()
		];
		
		render (contentType: "application/json",text: data as JSON)
	
	}
	
	def dataStartMonitor() {
		
		//http://www.hyperic.com/support/docs/sigar/
		
		def sigar=new Sigar();
		
		def mem=sigar.getMem();
		def memRam=mem.getRam();
		def memUsed=mem.getUsed();
		def memActualUsed=mem.getActualUsed();
		def memTotal=mem.getTotal();
		def memFree=mem.getFree();
		def memActualFree=mem.getActualFree();
		def memUsedPrecent=mem.getUsedPercent();
		
		println "ram: ${memRam} MB";
		println "mem: ${memActualUsed}/${memTotal}"
		
		
		// cpu total
		def cpu=sigar.getCpu();
		def cpuIdle=cpu.getIdle();
		def cpuUser=cpu.getUser();
		def cpuSys=cpu.getSys();
		def cpuWait=cpu.getWait();
		def cpuTotal=cpu.getTotal();
		
		println "${cpuUser}"
		println "${cpuSys}"
		println "${cpuWait}"
		println "${cpuIdle}"
		println "${cpuTotal}"
		
		// each cpu
		/*
		sigar.getCpuList()?.eachWithIndex { element, index ->
			
			println "----cpu ${index}----"
			
			cpuIdle=element.getIdle();
			cpuUser=element.getUser();
			cpuSys=element.getSys();
			cpuWait=element.getWait();
			cpuTotal=element.getTotal();
			
			
			println "${cpuUser}"
			println "${cpuSys}"
			println "${cpuWait}"
			println "${cpuIdle}"
			println "${cpuTotal}"
			
			println "--------"
		}
		*/
		
		
		sigar.getFileSystemList()?.each {
			
			println "----${it.devName}----"
			
			// fs usage
			def fs=sigar.getFileSystemUsage(it.dirName);
			def fsTotal=fs.getTotal();
			def fsFree=fs.getFree();
			def fsUsed=fs.getUsed();
			
			def diskR=fs.getDiskReads();
			def diskW=fs.getDiskWrites();
			
			println "disk io: ${diskR}/${diskW}";
			println "fs used/total: ${fsUsed}/${fsTotal}";
			
			
			println "--------"
		}
		
		
		render (["state":"success"]) as JSON
		
	}
	
	def dataStopMonitor() {
		def isRunning=!grailsApplication.config.cmjob.isRunning;
		grailsApplication.config.cmjob.isRunning=isRunning;
		
		render (contentType: "application/json", text: (["state": "success", "data": isRunning]) as JSON)
	
	
	}
	
}
