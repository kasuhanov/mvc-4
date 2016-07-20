package su.asgor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.asgor.service.LoaderService;

import java.util.TimerTask;

public class LoadTask extends TimerTask {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    private LoaderService loaderService;

    public LoadTask(LoaderService loaderService){
        this.loaderService = loaderService;
    }

    @Override
    public void run() {
        log.info("load task triggered");
        loaderService.run();
    }
}
