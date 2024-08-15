package com.example.service;

import com.example.repository.RecordRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EventFolderService {
    private static final Logger logger = LoggerFactory.getLogger(EventFolderService.class);

    /*
    Folder list
     */
    @Value("${event.folder.incoming}")
    private String directoryPathIncoming;
    @Value("${event.folder.error}")
    private String directoryPathError;
    @Value("${event.folder.completed}")
    private String directoryPathCompleted;

    private WatchService watchService;
    private Thread watchThread;
    private AtomicBoolean keepWatching = new AtomicBoolean(true);

    @PostConstruct
    public void startMonitoring() {
        logger.info("monitoring folder");
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(directoryPathIncoming);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            watchThread = new Thread(() -> {
                while (keepWatching.get()) {
                    WatchKey key;
                    try {
                        key = watchService.take();  // Wait for a key to be available
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Directory monitoring thread interrupted", e);
                        break;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        // Retrieve the file name associated with the event
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        logger.info("Event kind: {}. File affected: {}.", kind.name(), fileName);

                        // Handle the event (e.g., process the file)
                        handleEvent(kind, fileName);
                    }

                    // Reset the key -- this step is critical if you want to receive further watch events.
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            });

            watchThread.start();
        } catch (IOException e) {
            logger.error("Error setting up directory watch service", e);
        }
    }

    @PreDestroy
    public void stopMonitoring() {
        keepWatching.set(false);
        try {
            watchService.close();
            watchThread.interrupt();
        } catch (IOException e) {
            logger.error("Error closing watch service", e);
        }
    }

    private static DocumentBuilderFactory factory;
    static {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }
    @Autowired
    private RecordService service;

    private void handleEvent(WatchEvent.Kind<?> kind, Path fileName) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            logger.info("Detected new event file: {}", fileName);
            Path srcPath = Paths.get(directoryPathIncoming + "/" + fileName.getFileName());
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(srcPath.toFile().getAbsolutePath());

                service.processDoc(doc);

                // add the time to the targetPath to avoid file name collision
                Path targetPath = Paths.get(directoryPathCompleted + "/" + fileName.getFileName() + "." + System.currentTimeMillis());
                Files.move(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                logger.error("there was an error", e);

                // add the time to the targetPath to avoid file name collision
                Path targetPath = Paths.get(directoryPathError + "/" + fileName.getFileName() + "." + System.currentTimeMillis());
                try {
                    Files.move(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    logger.error("not able to move to error folder", e);
                }
            }
        }
    }
}
