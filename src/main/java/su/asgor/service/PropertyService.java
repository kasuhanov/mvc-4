package su.asgor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.asgor.dao.PropertyRepository;
import su.asgor.model.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PropertyService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private TaskService taskService;

    public String get(String name){
        return propertyRepository.findByName(name).getValue();
    }

    public Map<String,String> getAll(){
        List<Property> properties = propertyRepository.findAll();
        Map<String, String> map = new TreeMap<>();
        for (Property property:properties){
            map.put(property.getName(),property.getValue());
        }
        return  map;
    }

    public void set(String name,String value){
        Property property = propertyRepository.findByName(name);
        property.setValue(value);
        propertyRepository.save(property);
    }

    public void set(Map<String, String> map){
        boolean taskChanged =  false;
        for (Map.Entry<String,String> entry:map.entrySet()){
            Property property =  new Property();
            property.setName(entry.getKey());
            property.setValue(entry.getValue());
            if(entry.getKey().equals("app.scheduling.enable")
                    ||entry.getKey().equals("app.scheduling.notify")
                    ||entry.getKey().equals("app.scheduling.load")){
                taskChanged = true;
            }
            if(propertyRepository.findByName(entry.getKey())==null){
                propertyRepository.save(property);
            }else{
                property = propertyRepository.findByName(entry.getKey());
                property.setValue(entry.getValue());
                propertyRepository.save(property);
            }
        }
        if (taskChanged){
            taskService.addTasks();
        }
    }

    public void initialize(){
        if(propertyRepository.count() == 0){
            log.info("initializing properties table");
            Map<String, String> map = new HashMap<>();

            map.put("app.admin.password","admin");

            map.put("app.mail.host","smtp.mail.ru");
            map.put("app.mail.password","F[LSJf38dfcb");
            map.put("app.mail.port","465");
            map.put("app.mail.username","test-zakupki@mail.ru");

            map.put("app.ftp.fz223.server-address","ftp.zakupki.gov.ru");
            map.put("app.ftp.fz223.user","fz223free");
            map.put("app.ftp.fz223.password","fz223free");
            map.put("app.ftp.fz223.directory","out/published/Cheliabinskaya_obl/purchaseNotice/daily");

            map.put("app.ftp.fz44.server-address","ftp.zakupki.gov.ru");
            map.put("app.ftp.fz44.user","free");
            map.put("app.ftp.fz44.password","free");
            map.put("app.ftp.fz44.directory","fcs_regions/Cheljabinskaja_obl/notifications/currMonth/");

            map.put("app.scheduling.enable","false");
            map.put("app.scheduling.notify","20-00");
            map.put("app.scheduling.load","0-30");

            map.put("app.download.start-date","2016-01-01");

            set(map);
        }else {
            taskService.addTasks();
        }
    }
}
