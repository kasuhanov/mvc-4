package su.asgor;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import su.asgor.config.RootConfig;
import su.asgor.config.WebConfig;

public class Application extends
        AbstractAnnotationConfigDispatcherServletInitializer {

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