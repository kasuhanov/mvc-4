package su.asgor.config.db.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Component
@Scope(value = "singleton")
public class DataSourceProvider{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private String ip="localhost";
    private String port="5432";
    private String dbname="database_name";
    private String username="postgres";
    private String password="password";
    @Autowired
    private ServletContext servletContext;
    private DataSource dataSource;

    @PostConstruct
    public void postConstruct(){
        InputStream is =  null;
        try {
            String contextName;
            if(servletContext.getContextPath().equals("")){
                contextName = "ROOT";
            }else{
                contextName = servletContext.getContextPath().substring(1);
            }
            Properties properties = new Properties();
            is = new FileInputStream(contextName+".properties");
            properties.load(is);
            ip=properties.getProperty("ip");
            port=properties.getProperty("port");
            dbname=properties.getProperty("dbname");
            username=properties.getProperty("username");
            password=properties.getProperty("password");
            dataSource = defaultDataSource();
        }catch (Exception e){
            log.warn("properties file not found");
        }finally {
            try {
                if(is != null)
                    is.close();
            }catch (Exception e1){
                log.info("",e1);
            }
        }
    }

    public DataSource getDataSource(){
        return dataSource;
    }

    public DataSource dataSource(String ip, String port, String dbname, String username, String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl("jdbc:postgresql://"+ip+":"+port+"/"+dbname);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setIdleTimeout(45000);
        ds.setConnectionTestQuery("select version();");
        return ds;
    }

    public DataSource defaultDataSource() {
        return dataSource(ip, port, dbname, username, password);
    }

    public void setDataSource( String ip, String port, String dbname, String username, String password){
        dataSource = dataSource(ip, port, dbname, username, password);
    }

    public void close(DataSource dataSource) {
        try {
            if (dataSource!=null)
                ((HikariDataSource)dataSource).close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    public String getDbname() {
        return dbname;
    }

    public String getIp() {
        return ip;
    }

    public String getPassword() {
        return password;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}