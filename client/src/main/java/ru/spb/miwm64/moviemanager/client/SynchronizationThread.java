package ru.spb.miwm64.moviemanager.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class SynchronizationThread extends Thread{
    private static final Logger LOG = LoggerFactory.getLogger(SynchronizationThread.class);
    private boolean isRunning = true;
    public void run() {
        while (isRunning) {
            try {
                sync();
                Thread.sleep(5000);
            }
            catch (InterruptedException e){
                LOG.info("sync thread interrupted");
            }
            catch (Exception e) {
                LOG.error(e.getMessage(), e.getStackTrace());
            }
        }
    }

    private void sync(){
        System.out.println("["+LocalDateTime.now()+"] Synchronization successful");
    }

    public void gracefulShutdown(){
        sync();
        close();
    }

    public void close(){
        isRunning = false;
        this.interrupt();
    }
}
