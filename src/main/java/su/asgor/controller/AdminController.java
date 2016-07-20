package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.asgor.Application;
import su.asgor.config.db.util.DataSourceProvider;
import su.asgor.config.db.util.RoutingDataSource;
import su.asgor.dao.*;
import su.asgor.model.*;
import su.asgor.service.CategoryService;
import su.asgor.service.LoaderService;
import su.asgor.service.MailService;
import su.asgor.service.PropertyService;

import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private PatternRepository patternRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DownloadRepository downloadRepository;
    @Autowired
    private FTPArchiveRepository archiveRepository;
    @Autowired
    private XMLFileRepository xmlFileRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private LoaderService loaderService;
    @Autowired
    private MailService mailService;
    @Autowired
    private RoutingDataSource routingDataSource;
    @Autowired
    private DataSourceProvider dataSourceProvider;

    @RequestMapping(value = "/pattern/all",method = RequestMethod.GET )
    @ResponseBody
    public List<Pattern> getPatterns(){
        return patternRepository.findAll();
    }

    @RequestMapping(value = "/pattern/{id}/add-category/{cid}",method = RequestMethod.GET )
    @ResponseBody
    public Pattern addCategory(@PathVariable String id, @PathVariable long cid){
        Category category = categoryRepository.findOne(cid);
        category.getPatterns().add(patternRepository.findOne(id));
        categoryRepository.save(category);
        return patternRepository.findOne(id);
    }

    @RequestMapping(value = "/pattern/{id}/remove-category/{cid}",method = RequestMethod.GET )
    @ResponseBody
    public Pattern removeCategory(@PathVariable String id, @PathVariable long cid){
        Category category = categoryRepository.findOne(cid);
        category.getPatterns().remove(patternRepository.findOne(id));
        categoryRepository.save(category);
        return patternRepository.findOne(id);
    }

    @RequestMapping(value = "/pattern/{name}",method = RequestMethod.POST )
    @ResponseBody
    public ResponseEntity<?> addPattern(@PathVariable String name, @RequestBody long... categories){
        if(patternRepository.findByPattern(name.replace("&#46;",".")) == null){
            Pattern pattern = new Pattern(name.replace("&#46;","."));
            for(long cat : categories){
                Category category =  categoryRepository.findOne(cat);
                category.addPattern(pattern);
                categoryRepository.save(category);
            }
            return new ResponseEntity<>(pattern,HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/pattern/{id}",method = RequestMethod.DELETE )
    @ResponseBody
    public ResponseEntity<?> deletePattern(@PathVariable String id){
        List<Category> categories = patternRepository.findOne(id).getCategories();
        for (Category category:categories){
            category.getPatterns().remove(patternRepository.findOne(id));
        }
        categoryRepository.save(categories);
        patternRepository.delete(id);
        if(patternRepository.findOne(id) == null){
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/update",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> updatePurchasesCategory(){
        categoryService.updatePurchasesCategory();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "pattern/okpd/{okpd}",method = RequestMethod.GET )
    @ResponseBody
    public List<Pattern> getByPurchase(@PathVariable String okpd) {
        List<Pattern> patterns = patternRepository.findAll();
        List<Pattern> result = new ArrayList<>();
        for (Pattern pattern:patterns){
            if (okpd.replace("&#46;",".").equals(pattern.getPattern())||(pattern.getPattern().contains("x")&&
                    okpd.replace("&#46;",".").startsWith(pattern.getPattern().substring(0, pattern.getPattern().lastIndexOf("x")))))
                result.add(pattern);
        }
        return result;
    }
    
    @RequestMapping(value = "/download",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> triggerDownload(){
        loaderService.run();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/download/date/{date}",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> triggerDownloadSinceDate(@PathVariable Long date){
        Date startDate = new Date(date);
        loaderService.run(startDate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/downloads",method = RequestMethod.GET )
    @ResponseBody
    public List<Download> getDownloads(){
        List<Download> downloads = downloadRepository.findAll();
        for(Download download: downloads){
            for (FTPArchive archive: download.getFtpArchives()){
                if(!archive.getStatus())
                    download.setFailedArchive(download.getFailedArchive()+1);

                for(XMLFile xmlFile: archive.getXmlFiles()){
                    if(xmlFile.getStatus())
                        download.setSucceeded(download.getSucceeded()+1);
                    else
                        download.setFailed(download.getFailed()+1);
                }
            }
        }
        return downloads;
    }

    @RequestMapping(value = "/download/{id}/archives",method = RequestMethod.GET )
    @ResponseBody
    public List<FTPArchive> getArchivesInDownload(@PathVariable long id){
        Download download = downloadRepository.findOne(id);
        List<FTPArchive> archives = download.getFtpArchives();
        for(FTPArchive archive:archives){
            List<XMLFile> xmlFiles = archive.getXmlFiles();
            for(XMLFile xmlFile:xmlFiles){
               if(xmlFile.getStatus())
                   archive.setSucceeded(archive.getSucceeded()+1);
               else
                   archive.setFailed(archive.getFailed()+1);
            }
        }
        return archives;
    }

    @RequestMapping(value = "/archive/{id}/xml",method = RequestMethod.GET )
    @ResponseBody
    public List<XMLFile> getXmlInArchive(@PathVariable long id){
        FTPArchive archive = archiveRepository.findOne(id);
        List<XMLFile> xmlFiles = archive.getXmlFiles();
        for(XMLFile xmlFile:xmlFiles)
            xmlFile.setup();
        return xmlFiles;
    }

    @RequestMapping(value = "/archive/{id}/reload",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> reloadArchive(@PathVariable long id){
        FTPArchive archive = archiveRepository.findOne(id);
        if (archive == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        archive = loaderService.reloadArchive(archive);
        List<XMLFile> xmlFiles = archive.getXmlFiles();
        for(XMLFile xmlFile:xmlFiles){
            if(xmlFile.getStatus())
                archive.setSucceeded(archive.getSucceeded()+1);
            else
                archive.setFailed(archive.getFailed()+1);
        }
        return new ResponseEntity<>(archive,HttpStatus.OK);
    }

    @RequestMapping(value = "/xml/{id}/reload",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> reloadXml(@PathVariable long id){
        XMLFile xmlFile = xmlFileRepository.findOne(id);
        if (xmlFile == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        loaderService.reloadFile(xmlFile);
        return new ResponseEntity<>(xmlFile,HttpStatus.OK);
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST )
    @ResponseBody
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file){
        try{
            loaderService.runForUploaded(file);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/notify",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> triggerNotification(){
        mailService.notifyUsers();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/properties",method = RequestMethod.GET )
    @ResponseBody
    public Map<String, String> getProperties(){
        return propertyService.getAll();
    }

    @RequestMapping(value = "/properties",method = RequestMethod.POST )
    @ResponseBody
    public Map<String, String> setProperties(@RequestBody Map<String, String> map){
        propertyService.set(map);
        return map;
    }

    @RequestMapping(value = "/first-and-last-purchases-dates",method = RequestMethod.GET )
    @ResponseBody
    public Map<String, String> getFirstAndLastDatePurchase(){
        Purchase first = purchaseRepository.findTopByOrderByPublicationDate();
        Purchase last = purchaseRepository.findTopByOrderByPublicationDateDesc();
        Map<String, String> map = new TreeMap<>();
        if(first != null)
            map.put("first",first.getPublicationDate().toString());
        if(last != null)
            map.put("last",last.getPublicationDate().toString());
        return map;
    }

    @RequestMapping(value = "/change-db",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> changeDb(@RequestParam String ip, @RequestParam String port,
                                    @RequestParam String dbname, @RequestParam String username,
                                    @RequestParam String password ){
        DataSource dataSource = dataSourceProvider.dataSource(ip, port, dbname, username, password);
        try{
            JdbcTemplate jdbcTemplate =
                    new JdbcTemplate(dataSource);
            jdbcTemplate.execute("SELECT 1");
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }finally {
            dataSourceProvider.close(dataSource);
        }
        routingDataSource.setDataSource(ip,port,dbname,username,password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/update-db",method = RequestMethod.GET )
    @ResponseBody
    public ResponseEntity<?> updateDb(@RequestParam String ip, @RequestParam String port,
                                      @RequestParam String dbname, @RequestParam String username,
                                      @RequestParam String password){
        try {
            routingDataSource.updateSchema(ip,port,dbname,username,password);
            categoryService.initialize();
            propertyService.initialize();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/db-props",method = RequestMethod.GET )
    @ResponseBody
    public Map<String, String> getDbProperties(){
        Map<String,String> map = new HashMap<>();
        map.put("ip",dataSourceProvider.getIp());
        map.put("port",dataSourceProvider.getPort());
        map.put("dbname",dataSourceProvider.getDbname());
        map.put("username",dataSourceProvider.getUsername());
        map.put("password",dataSourceProvider.getPassword());

        try{
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceProvider.getDataSource());
            jdbcTemplate.execute("SELECT 1");
            map.put("up","true");
        }catch (Exception e){
            map.put("up","false");
        }

        return  map;
    }

    @RequestMapping(value = "/test-db",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> testDb(@RequestParam String ip, @RequestParam String port,
                                      @RequestParam String dbname, @RequestParam String username,
                                      @RequestParam String password ){
        DataSource dataSource = dataSourceProvider.dataSource(ip, port, dbname, username, password);
        try{
            JdbcTemplate jdbcTemplate =
                    new JdbcTemplate(dataSource);
            jdbcTemplate.execute("SELECT 1");
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }finally {
            dataSourceProvider.close(dataSource);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/save-db-props",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> saveDbProperties(@RequestParam String ip, @RequestParam String port,
                                    @RequestParam String dbname, @RequestParam String username,
                                    @RequestParam String password ){
        Properties properties = new Properties();
        FileOutputStream fileOut = null;
        try{
            fileOut = new FileOutputStream(Application.contextName+".properties");
            properties.setProperty("ip",ip);
            properties.setProperty("port",port);
            properties.setProperty("dbname",dbname);
            properties.setProperty("username",username);
            properties.setProperty("password",password);
            properties.store(fileOut,"Database connection properties");
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }finally {
            try {
                if(fileOut != null)
                    fileOut.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
