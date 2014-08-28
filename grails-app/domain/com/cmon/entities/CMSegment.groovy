package com.cmon.entities

import java.util.Date;

class CMSegment {
	
	double segmentMemTotal;
	double segmentMemUsed;
	double segmentMemFree;
	
	double segmentCpuSys;
	double segmentCpuUser;
	double segmentCpuCombined;
	
	double segmentDiskR;
	double segmentDiskW;
	
	Date dateCreated;
	Date lastUpdated;
	
    static constraints = {
    }
}
