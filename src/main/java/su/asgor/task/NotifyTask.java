package su.asgor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.asgor.service.MailService;

import java.util.TimerTask;

public class NotifyTask extends TimerTask{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MailService mailService;

    public NotifyTask(MailService mailService){
        this.mailService = mailService;
    }

    @Override
    public void run() {
        log.info("notify task triggered");
        mailService.notifyUsers();
    }
}
