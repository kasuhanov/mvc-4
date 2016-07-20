package su.asgor.service;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.asgor.dao.PropertyRepository;
import su.asgor.task.LoadTask;
import su.asgor.task.NotifyTask;

import java.util.Timer;

@Service
public class TaskService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private LoaderService loaderService;
    @Autowired
    private MailService mailService;

    private Timer notifyTimer;
    private Timer loadTimer;

    public void addTasks(){
        cancelTasks();
        if(Boolean.valueOf(propertyRepository.findByName("app.scheduling.enable").getValue()))
        {
            String notifyProperty = propertyRepository.findByName("app.scheduling.notify").getValue();
            String loadProperty = propertyRepository.findByName("app.scheduling.load").getValue();
            notifyTimer = new Timer();
            loadTimer = new Timer();
            NotifyTask notifyTask = new NotifyTask(mailService);
            LoadTask loadTask = new LoadTask(loaderService);

            log.info("tasks set");

            try {
                if((notifyProperty.indexOf('-')==-1)||(loadProperty.indexOf('-')==-1))
                    throw new Exception("invalid date format");

                DateTime dt = new DateTime()
                        .withHourOfDay(Integer.valueOf(notifyProperty.substring(0,notifyProperty.indexOf('-'))))
                        .withMinuteOfHour(Integer.valueOf(notifyProperty.substring(notifyProperty.indexOf('-')+1)))
                        .withSecondOfMinute(0);
                if(!dt.isAfterNow()){
                    dt = dt.plusDays(1);
                }
                notifyTimer.schedule(notifyTask,dt.toDate(),24*60*60*1000);//once a day

                dt = new DateTime()
                        .withHourOfDay(Integer.valueOf(loadProperty.substring(0,loadProperty.indexOf('-'))))
                        .withMinuteOfHour(Integer.valueOf(loadProperty.substring(loadProperty.indexOf('-')+1)))
                        .withSecondOfMinute(0);
                if(!dt.isAfterNow()){
                    dt = dt.plusDays(1);
                }
                loadTimer.schedule(loadTask, dt.toDate(),24*60*60*1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelTasks(){
        if(loadTimer!=null){
            loadTimer.cancel();
            loadTimer.purge();
        }
        if(notifyTimer!=null){
            notifyTimer.cancel();
            notifyTimer.purge();
        }
    }

}
