package su.asgor;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import su.asgor.config.RootConfig;
import su.asgor.config.WebConfig;
import su.asgor.config.db.util.DataSourceProvider;
import su.asgor.service.CategoryService;
import su.asgor.service.PropertyService;


@Component
public class Application extends
        AbstractAnnotationConfigDispatcherServletInitializer {
	@Autowired
    private CategoryService categoryService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private DataSourceProvider dataSourceProvider;
    public static String contextName;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @PostConstruct
    public void init() throws Exception {
        try{
            if(servletContext.getContextPath().equals("")){
                contextName = "ROOT";
            }else{
                contextName = servletContext.getContextPath().substring(1);
            }
            try{
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceProvider.getDataSource());
                jdbcTemplate.execute("SELECT 1");
                categoryService.initialize();
                propertyService.initialize();
            }catch (Exception e){
                log.warn("ds connection not active, init postponed");
            }
        }catch (Exception e){
            log.error("",e);
        }
	}

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}