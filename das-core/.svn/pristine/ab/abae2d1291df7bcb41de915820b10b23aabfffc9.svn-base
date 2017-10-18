package com.cas.das.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import net.jlrnt.common.Const;

/**
 * 记录文件共通方法。
 * 
 * <p>
 * 目前可记录文件格式有：<br/>
 * 1.第一行为天时间，后续为具体内容
 * </p>
 * 
 * @author xiang_wang
 */
public class RecordFileUtils {

	private static final String RECORD_PREFIX = Const.USER_DIR_PATH + "/cas-pe-setting/das/record/";

	/**
	 * 创建记录文件。
	 * <p>
	 * 记录文件前缀为每个模块特定名称，后者为文件名。
	 * </p>
	 * 
	 * @param filePrefix
	 * @param fileName
	 * @return
	 */
	public static File createRecordFile(String filePrefix, String fileName) {
		File recordFile = new File(RECORD_PREFIX + filePrefix + "_" + fileName);

		// 创建父目录
		createParentDir(recordFile);

		// 文件不存在则创建
		if (!recordFile.exists()) {
			try {
				recordFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return recordFile;
	}

	/**
	 * 获取记录时间
	 * 
	 * @param file
	 * @return
	 */
	public static String getRecordDate(File file) {

		String recordDate = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);

			recordDate = br.readLine();
			br.close();
			isr.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (recordDate == null || "".equals(recordDate)) {
			recordDate = "20000101";
		}
		return recordDate;
	}

	/**
	 * 清空文件。
	 * 
	 * @param file
	 * @return
	 */
	public static void resetRecordDate(File file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, false);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 清空文件并更新记录时间。
	 * 
	 * @param file
	 * @param recordDate
	 * @return
	 */
	public static void resetRecordDate(File file, String recordDate) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, false);
			fw.write(recordDate + "\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 创建父目录
	public static void createParentDir(File file) {
		if (!file.getParentFile().exists()) {
			createParentDir(file.getParentFile());
			file.getParentFile().mkdir();
		}
	}

	/**
	 * 判断文件中是否有指定内容。
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean isContentExists(File file, String content) {
		boolean result = false;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains(content)) {
					result = true;
					break;
				}
			}
			br.close();
			isr.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 把内容写入文件结尾。
	 * 
	 * @param file
	 * @param content
	 */
	public static void writeToFile(File file, String content) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
			fw.write(content + "\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 复制文件到指定目录
	 * */
	public static void copyTo(String oldPath, String newPath) {
		File file = new File(newPath);
		createParentDir(file);
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				FileInputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}
}
