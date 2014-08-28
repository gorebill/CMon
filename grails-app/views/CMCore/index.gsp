<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="main"/>
<title>CMon Platform</title>

<script type="text/javascript" src="${resource(dir: 'js', file: 'highcharts.js')}"></script>

</head>
<body>
<div class="body" style="font-family:Microsoft Yahei;">


<div style="font-size:12px;">
<table style="min-width:600px;text-align:left;">
<tr><th>当前状态</th><th></th><th>CPU核数</th><th>总内存</th><th>磁盘</th></tr>
<tr>
<td><span id="isRunning">${jobRunning?'Running':'Stopped'}</span></td>
<td><button onclick="disableMonitor()">打开/关闭监控</button></td>
<td>${cpuNumber}</td>
<td><g:formatNumber number="${memTotal/1024f/1024f/1024f}" /> GB</td>
<td>${diskInfo}</td>
</tr>
</table>
</div>

<div id="charts">
	<div id=chartCpu style="height:240px;">This is just a replacement in case Javascript is not available or used for SEO purposes</div>
	<div id=chartMem style="height:240px;">This is just a replacement in case Javascript is not available or used for SEO purposes</div>
	<div id=chartVol style="height:240px;">This is just a replacement in case Javascript is not available or used for SEO purposes</div>
</div>



</div>

<script type="text/javascript">
var chartCpu;
var chartMem;
var chartVol;
$(function(){
	Highcharts.setOptions({
		global: {
			useUTC: false
		}
	});
	
	$.getJSON('${createLink(action:'dataListSegments.json')}', function(data) {
		// cpu chart
		chartCpu = new Highcharts.Chart({
            chart: {
                renderTo: 'chartCpu',
                type: 'spline'
            },
            credits: {
                enabled: false
            },
            
            plotOptions: {
            	spline: {
                    marker: {
                        enabled: false
                    }
                }
            },
            
            title: {
                text: 'CPU总用量图'
            },

            subtitle: {
                text: '最近100次采样'
            },
            
            xAxis: {
                type: 'datetime',
                //tickInterval: 20*1000,//7 * 24 * 3600 * 1000, // one week
                tickWidth: 0,
                gridLineWidth: 1,
                labels: {
                    align: 'left',
                    x: 3,
                    y: 13
                }
            },

            yAxis: [{ // left y axis
                title: {
                    text: null
                },
                labels: {
                    align: 'left',
                    x: 3,
                    y: 16,
                    formatter: function(){
                        return parseFloat(this.value*100).toFixed(2) + "%";
                    }
                },
                showFirstLabel: false
            }],

            tooltip: {
                shared: true,
                crosshairs: true,
                formatter: function () {
                    if(this.points[0] && this.points[1] && this.points[2]) {
    					var str1=Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x);
    					var str2='总用量:<b>'+parseFloat(this.points[0].y*100).toFixed(2)+'%</b>';
    					var str3='系统:<b>'+parseFloat(this.points[1].y*100).toFixed(2)+'%</b>';
    					var str4='用户:<b>'+parseFloat(this.points[2].y*100).toFixed(2)+'%</b>';
                        return [str1,str2,str3,str4].join('<br>');
                    }
                }
            },

            series: [
            	{id: 'cpuUsedPercent', name:'CPU总用量', data: data.cpuUsedPercent},
            	{id: 'cpuUsedSysPercent', name:'系统', data: data.cpuUsedSysPercent},
            	{id: 'cpuUsedUserPercent', name:'用户', data: data.cpuUsedUserPercent}
            ]
        });

		// memory chart
		chartMem = new Highcharts.Chart({
            chart: {
                renderTo: 'chartMem',
                type: 'spline'
            },
            credits: {
                enabled: false
            },
            
            plotOptions: {
            	spline: {
                    marker: {
                        enabled: false
                    }
                }
            },
            
            title: {
                text: '内存用量图'
            },

            subtitle: {
                text: '最近100次采样'
            },
            
            xAxis: {
                type: 'datetime',
                //tickInterval: 20*1000,//7 * 24 * 3600 * 1000, // one week
                tickWidth: 0,
                gridLineWidth: 1,
                labels: {
                    align: 'left',
                    x: 3,
                    y: 13
                }
            },

            yAxis: [{ // left y axis
                title: {
                    text: null
                },
                labels: {
                    align: 'left',
                    x: 3,
                    y: 16,
                    formatter: function(){
                        return parseFloat(this.value*100).toFixed(2) + "%";
                    }
                },
                showFirstLabel: false
            }],

            tooltip: {
                shared: true,
                crosshairs: true,
                formatter: function () {
                    if(this.points[0] && this.points[1]) {
    					var str1=Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x);
    					var str2='占用:<b>'+parseFloat(this.points[0].y*100).toFixed(2)+'%</b>';
    					var str3='空闲:<b>'+parseFloat(this.points[1].y*100).toFixed(2)+'%</b>';
                        return [str1,str2,str3].join('<br>');
                    }
                }
            },

            series: [
                {id: 'memUsedPercent', name:'占用', data: data.memUsedPercent},
            	{id: 'memFreePercent', name:'空闲', data: data.memFreePercent}
            ]
        });

		// volume chart
		chartVol = new Highcharts.Chart({
            chart: {
                renderTo: 'chartVol',
                type: 'spline',
                events: {
                    load: requestData
                }
            },
            credits: {
                enabled: false
            },
            
            plotOptions: {
            	spline: {
                    marker: {
                        enabled: false
                    }
                }
            },
            
            title: {
                text: '磁盘IO'
            },

            subtitle: {
                text: '最近100次采样'
            },
            
            xAxis: {
                type: 'datetime',
                //tickInterval: 20*1000,//7 * 24 * 3600 * 1000, // one week
                tickWidth: 0,
                gridLineWidth: 1,
                labels: {
                    align: 'left',
                    x: 3,
                    y: 13
                }
            },

            yAxis: [{ // left y axis
                title: {
                    text: null
                },
                labels: {
                    align: 'left',
                    x: 3,
                    y: 16,
                    formatter: function(){
                        return this.value + "次";
                    }
                },
                showFirstLabel: false
            }],

            tooltip: {
                shared: true,
                crosshairs: true,
                formatter: function () {
                    if(this.points[0] && this.points[1]) {
    					var str1=Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x);
    					var str2='读:<b>'+this.points[0].y+' 次</b>';
    					var str3='写:<b>'+this.points[1].y+' 次</b>';
                        return [str1,str2,str3].join('<br>');
                    }
                }
            },

            series: [
                {id: 'diskR', name:'读', data: data.diskR},
            	{id: 'diskW', name:'写', data: data.diskW}
            ]
        });
    });
    
});

function requestData() {
    $.ajax({
        url: '${createLink(action:'dataGetLastSegment.json')}',
        success: function(segments) {
            var series_1 = chartCpu.get('cpuUsedPercent'),
                shift = series_1.data.length > 100; // shift if the series is 
                                                 // longer than 20

            var series_2 = chartCpu.get('cpuUsedSysPercent'),
                shift = series_2.data.length > 100;
                                                 
            var series_3 = chartCpu.get('cpuUsedUserPercent'),
                shift = series_3.data.length > 100;
                
            var series_4 = chartMem.get('memUsedPercent'),
                shift = series_4.data.length > 100;
                
            var series_5 = chartMem.get('memFreePercent'),
                shift = series_5.data.length > 100;
                
            var series_6 = chartVol.get('diskR'),
                shift = series_6.data.length > 100;
                
            var series_7 = chartVol.get('diskW'),
                shift = series_7.data.length > 100;
                
            // add the point
            //console.log('A: '+chart.get('cpuUsedPercent').data.reverse()[0].x+' B:'+segments.cpuUsedPercent[0]);
            if(series_1.data[series_1.data.length-1].x != segments.cpuUsedPercent[0]) {
            	series_1.addPoint(segments.cpuUsedPercent, true, shift);
			}
            if(series_2.data[series_2.data.length-1].x != segments.cpuUsedSysPercent[0]) {
            	series_2.addPoint(segments.cpuUsedSysPercent, true, shift);
			}
            if(series_3.data[series_3.data.length-1].x != segments.cpuUsedUserPercent[0]) {
            	series_3.addPoint(segments.cpuUsedUserPercent, true, shift);
			}
            if(series_4.data[series_4.data.length-1].x != segments.memUsedPercent[0]) {
            	series_4.addPoint(segments.memUsedPercent, true, shift);
			}
            if(series_5.data[series_5.data.length-1].x != segments.memFreePercent[0]) {
            	series_5.addPoint(segments.memFreePercent, true, shift);
			}
            if(series_6.data[series_6.data.length-1].x != segments.diskR[0]) {
            	series_6.addPoint(segments.diskR, true, shift);
			}
            if(series_7.data[series_7.data.length-1].x != segments.diskW[0]) {
            	series_7.addPoint(segments.diskW, true, shift);
			}
            
            // call it again after one second
            setTimeout(requestData, 1000);    
        },
        cache: false
    });
}

function disableMonitor() {
	$.ajax({
		url: "${createLink(action:'dataStopMonitor.json')}",
		type: "post",
		data: {}
	}).done(function(data){
		$("#isRunning").html(data.data?"Running":"Stopped");
	}).fail(function(){
	});
}
</script>

</body>
</html>