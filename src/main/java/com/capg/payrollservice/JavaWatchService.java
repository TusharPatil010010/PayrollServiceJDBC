package com.capg.payrollservice;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.*;

public class JavaWatchService {
	private final WatchService watcher;
	private final Map<WatchKey, Path> dirWatcher;
	public Path dir;

	public JavaWatchService(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.dirWatcher = new HashMap<WatchKey, Path>();
		this.dir = dir;
		scanAndRegisterDirectories(dir);
	}

	private void registerDirWatchers(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		dirWatcher.put(key, dir);
	}

	private void scanAndRegisterDirectories(final Path start) throws IOException {
		// TODO Auto-generated method stub

		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDirWatchers(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void processEvents() throws IOException {
		while (true) {
			WatchKey key = null;
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Path dir = dirWatcher.get(key);
			if (dir == null)
				continue;
			for (WatchEvent<?> event : key.pollEvents()) {

				WatchEvent.Kind<?> kind = event.kind();
				Path name = ((WatchEvent<Path>) event).context();
				Path child = dir.resolve(name);
				System.out.format("%s: %s\n", event.kind().name(), child);
				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					if (Files.isDirectory(child))
						scanAndRegisterDirectories(child);
					else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
						if (Files.isDirectory(child))
							dirWatcher.remove(key);
					}
				}
			}
			boolean valid = key.reset();
			if (!valid) {
				dirWatcher.remove(key);
				if (dirWatcher.isEmpty())
					break;
			}
		}
	}
}