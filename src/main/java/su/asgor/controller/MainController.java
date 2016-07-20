package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import su.asgor.config.db.util.RoutingDataSource;

@Controller
public class MainController {

    @Autowired
    private RoutingDataSource routingDataSource;
    private static boolean dataSourceOk = false;

    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String index(){
        if (!dataSourceOk){
            try{
                JdbcTemplate jdbcTemplate = new JdbcTemplate(routingDataSource);
                jdbcTemplate.execute("SELECT 1");
                dataSourceOk=true;
                return "/index.html";
            }catch (Exception e){
                return "redirect:db.html";
            }
        }
        else {
            return "/index.html";
        }
    }

    @RequestMapping(value = "/admin",method = RequestMethod.GET)
    public String admin(){
        if (!dataSourceOk){
            try{
                JdbcTemplate jdbcTemplate = new JdbcTemplate(routingDataSource);
                jdbcTemplate.execute("SELECT 1");
                dataSourceOk=true;
                return "//admin-login.html";
            }catch (Exception e){
                return "redirect:db.html";
            }
        }
        else {
            return "/admin-login.html";
        }
    }
}
