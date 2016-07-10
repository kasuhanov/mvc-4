package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

    private static boolean dataSourceOk = false;

    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String index(){
        return "/index.html";
    }
}
