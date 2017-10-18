package com.cas.das.core.service.ext;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import net.jlrnt.common.Const;

import com.cas.platform.service.ServiceHandle;

/**
 * 目录监视器
 * 
 * @author NiuLei
 */
public class DirectoryMonitor {

	/**
	 * 监视线程
	 */
	private MonitorThread monitorThread;

	/**
	 * 构造
	 * 
	 * @param path
	 * @param handle
	 * @throws IOException
	 */
	public DirectoryMonitor(Path path, ServiceHandle handle) throws IOException {
		if (null == path) {
			throw new IOException("Error: can not instance DirectoryMonitor, path is null.");
		}

		if (null == handle) {
			throw new IOException("Error: can not instance DirectoryMonitor, AccessServiceHandle is null.");
		}

		monitorThread = new MonitorThread(path, handle);
	}

	public void startup() {
		monitorThread.start();
	}

	public void shutdown() {
		try {
			monitorThread.watchService.close();
			monitorThread.interrupt();
			monitorThread.handle = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/**
 * 监视线程
 * 
 * @author NiuLei
 */
class MonitorThread extends Thread {

	/**
	 * 监视路径
	 */
	private Path path;

	/**
	 * 服务操作类
	 */
	ServiceHandle handle;

	/**
	 * WatchService
	 */
	WatchService watchService;

	/**
	 * 构造
	 * 
	 * @param path
	 * @param handle
	 * @throws IOException
	 */
	MonitorThread(Path path, ServiceHandle handle) throws IOException {
		this.path = path;
		this.handle = handle;
		watchService = FileSystems.getDefault().newWatchService();
	}

	@Override
	public void run() {
		try {
			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			System.err.println("Error: can not register WatchService on the path.");
			return;
		}

		while (true) {
			WatchKey key = null;
			try {
				key = watchService.take();

				for (WatchEvent<?> event : key.pollEvents()) {

					@SuppressWarnings("rawtypes")
					WatchEvent.Kind kind = event.kind();

					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}

					@SuppressWarnings("unchecked")
					WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;

					DirectoryMonitorEvent monitorEvent = new DirectoryMonitorEvent();
					monitorEvent.setEventName(kind.name());
					monitorEvent.setFilePath(path.toFile().getAbsolutePath() + Const.STRING_FILE_SEPARATOR + watchEvent.context().getFileName());

					handle.getServiceProcessor().process(handle, monitorEvent);
				}
			} catch (ClosedWatchServiceException e) {
				key = null;
				return;
			} catch (Exception e) {
				continue;
			} finally {
				if (null == key) {
					break;
				}
				if (!key.reset()) {
					break;
				}
			}
		}
	}

}
