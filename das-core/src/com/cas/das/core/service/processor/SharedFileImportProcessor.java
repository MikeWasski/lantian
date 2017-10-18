package com.cas.das.core.service.processor;

import java.io.File;

import com.cas.das.core.util.RecordFileUtils;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceProcessor;
import com.cas.platform.service.def.param.TextParam;

/**
 * 从共享目录复制文件
 * 
 * @author yushun_gong
 */
public class SharedFileImportProcessor extends ServiceProcessor {

	/**
	 * 数据记录位置前缀
	 */
	private static final String FILE_PREFIX = "SharedFile";

	@Override
	public void process(ServiceHandle handle, Object argument) {
		int num = 0;//记录导入文件个数
		File directory = (File) handle.getAttribute("directory");
		String startTime = ((TextParam) handle.getProcessorOuterParam("startTime")).getValue();
		if(null==startTime||"".equals(startTime)){
			startTime = "19700101";
		}
		String siteRecordCode = ((TextParam) handle.getProcessorOuterParam("siteRecordCode")).getValue();
		String importPath = ((TextParam) handle.getProcessorOuterParam("importPath")).getValue();
		File[] files = directory.listFiles();
		for (int index = 0; index < files.length; index++) {
			File file = files[index];
			if (file.exists() && file.isFile()) {
				// 确定读取数据日期
				// 判断记录文件是否存在，存在则在记录文件的位置开始读取数据，不存在则读取所有数据
				File recordFile = RecordFileUtils.createRecordFile(FILE_PREFIX, siteRecordCode);
				// 判断记录时间是否存在
				String recordDate = RecordFileUtils.getRecordDate(recordFile);
				//判断记录文件时间是否大于指定的扫描起始时间
				if(recordDate.compareTo(startTime)<0){
					RecordFileUtils.resetRecordDate(recordFile,startTime);
					recordDate = startTime;
				}
				//按不同的文件名提取数据文件具体时间（文件名1：NJ2015090913.2616，文件名2：NJ20161203124231.HPL）
				String fileName = file.getName();
				String fileTime = "";
				fileTime = getTime(fileName);
				//获取数据文件年月日信息
				String fileDate = fileTime.substring(0, 8);
				//判断数据文件时间是否大于记录文件时间
				if(recordDate.compareTo(fileDate)<0){
					// 判断当天的文件是否接入
					if(!RecordFileUtils.isContentExists(recordFile, fileTime)){
						// 文件未接入时，接入并记录
						RecordFileUtils.copyTo(file.getPath(),importPath+"/"+FILE_PREFIX + "_" + siteRecordCode+"/"+fileName);
						RecordFileUtils.writeToFile(recordFile,fileTime);
						System.out.println(fileName+"(File import)");
						num++;
					}
				}
			}
		}
		System.out.println("Data import complete ! Import "+num+" data.");
	}
	
	private String getTime(String str){
		if(str.startsWith("复件")){
			return "19700101000000";
		}
		if(str.indexOf("-")!=-1){
			str = str.substring(str.indexOf("-"), str.length());
		}
		if(str.startsWith("RM") && str.length() == 13){
			str = "20"+str;
			int year = Integer.parseInt(str.substring(6, 8));
			if(12 < year){
				str = new StringBuffer(str).insert(6, '0').toString();
			}
		}
		String s = "\\d";
		String time = "";
		for(int i=0;i<str.length();i++){
			Character c = str.charAt(i);
			if(c.toString().matches(s)){
				time += str.charAt(i);
			}
		}
		if(null==time||9>time.length()){
			return "19700101000000";
		}	
		
		return time;
	}

	
}
