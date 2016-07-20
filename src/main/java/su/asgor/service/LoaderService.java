package su.asgor.service;

import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import su.asgor.dao.DownloadRepository;
import su.asgor.dao.FTPArchiveRepository;
import su.asgor.dao.XMLFileRepository;
import su.asgor.model.*;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipError;

@Service
public class LoaderService {
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private DownloadRepository downloadRepository;
    @Autowired
    private FTPArchiveRepository ftpArchiveRepository;
    @Autowired
    private XMLFileRepository xmlFileRepository;
	@Autowired
	private ParserFz233Service fz223Parser;
    @Autowired
    private ParserFz44Service fz44Parser;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MailService mailService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void run(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = df.parse(propertyService.get("app.download.start-date"));
            run(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void runForUploaded(MultipartFile file) throws IOException{
        log.info("running loader for uploaded file");
        File workingDir = new File("downloaded/");
        workingDir.mkdirs();
        BufferedOutputStream stream = new BufferedOutputStream(
                new FileOutputStream(new File("downloaded/" + file.getOriginalFilename())));
        FileCopyUtils.copy(file.getInputStream(), stream);
        stream.close();
        Download download = new Download();
        download.setDate(new Date());
        downloadRepository.save(download);
        FTPArchive archive = new FTPArchive();
        archive.setName("archive");
        archive.setDate(getDate(file.getOriginalFilename()));
        archive.setStatus(true);
        archive.setDownload(download);
        XMLFile xmlFile = new XMLFile();
        xmlFile.setName(file.getOriginalFilename());
        xmlFile.setPath("downloaded/" + file.getOriginalFilename());
        xmlFile.setFtpArchive(archive);
        archive.setXmlFiles(Collections.singletonList(xmlFile));
        if (file.getName().startsWith("fcs"))
            parseFz44(xmlFile);
        else
            parse(xmlFile);
        ftpArchiveRepository.save(archive);
        download.setFtpArchives(Collections.singletonList(archive));
        sendEmails(download);
    }

    public void run(Date startDate){
        log.info("running loader");
        log.info("retrieving purchases published after "+startDate);
        Download download = new Download();
        download.setDate(new Date());
        download.setMessage("uncompleted");
        downloadRepository.save(download);
        long id = download.getId();

        try{
            load(id, startDate);
            loadFz44(id, startDate);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            Download d = downloadRepository.findOne(id);
            d.setMessage(e.getClass()+" : "+e.getMessage());
            downloadRepository.save(d);
        }
        categoryService.setupCount();
        Download d = downloadRepository.findOne(id);
        if(d.getMessage().equals("uncompleted")){
            d.setMessage("");
            downloadRepository.save(d);
        }
        sendEmails(downloadRepository.findOne(id));
    }

    private void load(long downloadId, Date startDate) throws Exception{
        List<PurchaseType> ptfz223 = new ArrayList<>();
        ptfz223.add(PurchaseType.AE);
        ptfz223.add(PurchaseType.OA);
        ptfz223.add(PurchaseType.OK);
        ptfz223.add(PurchaseType.ZK);
        ptfz223.add(PurchaseType.EP);
        String serverAddress = propertyService.get("app.ftp.fz223.server-address");
        String userId = propertyService.get("app.ftp.fz223.user");
        String password = propertyService.get("app.ftp.fz223.password");
        String remoteDirectory = propertyService.get("app.ftp.fz223.directory");
        String localDirectory;
        for(PurchaseType pt : ptfz223){
            File workingDir = new File("downloaded/purchaseNotice"+pt);
            workingDir.mkdirs();
        }

        FTPClient ftp = connectToFTP(serverAddress,userId,password,remoteDirectory);

        for(PurchaseType pt : ptfz223){
            ftp.changeWorkingDirectory("../../purchaseNotice"+pt+"/daily");
            localDirectory = "downloaded/purchaseNotice"+pt;
            FTPFile[] ftpFiles = ftp.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile file : ftpFiles) {
                    if (!file.isFile()) {
                        continue;
                    }
                    Date date = getDate(file.getName());
                    if(startDate != null){
                        if (!date.after(startDate)){
                            continue;
                        }
                    }
                    File newFile = new File(localDirectory+"/"+file.getName());
                    FTPArchive archive = new FTPArchive();
                    archive.setDate(date);
                    archive.setName(file.getName());
                    archive.setPath(newFile.getPath());

                    if (archiveInDb(file.getName())){
                        log.info("file already in db - "+file.getName());
                        continue;
                    }
                    if(newFile.exists()){
                        if(file.getSize()==newFile.length()){
                            log.info("file already exists - "+file.getName());
                            archive.setStatus(true);
                            archive.setDownload(downloadRepository.findOne(downloadId));
                            ftpArchiveRepository.save(archive);
                            unzip(archive,"fz223");
                            continue;
                        }else {
                            log.info("file already exists but file sizes not equals - "+file.getName());
                            newFile.delete();
                        }
                    }
                    newFile.createNewFile();
                    OutputStream output = new FileOutputStream(newFile,false);
                    boolean downloaded = false;
                    do {
                        try {
                            log.info("Downloading file - " + file.getName());
                            ftp.retrieveFile(file.getName(), output);
                            downloaded = true;
                            archive.setStatus(true);
                            if (file.getSize()!=newFile.length()){
                                log.error("files size not equal");
                                log.error("remote file - "+file.getSize());
                                log.error("local file - "+newFile.length());
                                archive.setStatus(false);
                                archive.setMessage("downloaded file size not equal original fise size");
                            }
                        } catch (IOException e) {
                            downloaded = false;
                            log.warn("Error downloading file - " + file.getName());
                            log.warn(e.getMessage());
                            if (e instanceof FTPConnectionClosedException || e instanceof ConnectException){
                                ftp = connectToFTP(serverAddress,userId,password,remoteDirectory);
                            }else if(!(e instanceof SocketTimeoutException)) {
                                throw e;
                            }
                        }
                    }while(!downloaded);
                    output.close();
                    archive.setDownload(downloadRepository.findOne(downloadId));
                    ftpArchiveRepository.save(archive);
                    unzip(archive,"fz223");
                }
            }
            log.info("Done loading files in "+pt);
        }
        log.info("Files in fz223 are loaded. Disconnecting.");
        try {
            ftp.logout();
            ftp.disconnect();
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    private void loadFz44(long downloadId, Date startDate)  throws Exception{
        String serverAddress = propertyService.get("app.ftp.fz44.server-address");
        String userId = propertyService.get("app.ftp.fz44.user");
        String password = propertyService.get("app.ftp.fz44.password");
        String remoteDirectory = propertyService.get("app.ftp.fz44.directory");
        String localDirectory = "downloaded/fz44";
        File workingDir = new File(localDirectory);
        workingDir.mkdirs();

        FTPClient ftp = connectToFTP(serverAddress,userId,password,remoteDirectory);

        FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles != null && ftpFiles.length > 0) {
            for (FTPFile file : ftpFiles) {
                if (!file.isFile()||!file.getName().contains("001.xml.zip")) {
                    continue;
                }
                Date date = getDateFz44(file.getName());
                if(startDate !=null){
                    if (!date.after(startDate)){
                        continue;
                    }
                }
                File newFile = new File(localDirectory+"/"+file.getName());
                FTPArchive archive = new FTPArchive();
                archive.setDate(date);
                archive.setName(file.getName());
                archive.setPath(newFile.getPath());

                if (archiveInDb(file.getName())){
                    log.info("file already in db - "+file.getName());
                    continue;
                }
                if(newFile.exists()){
                    if(file.getSize()==newFile.length()){
                        log.info("file already exists - "+file.getName());
                        archive.setStatus(true);
                        archive.setDownload(downloadRepository.findOne(downloadId));
                        ftpArchiveRepository.save(archive);
                        unzip(archive,"fz44");
                        continue;
                    }else {
                        log.info("file already exists but file sizes not equals - "+file.getName());
                        newFile.delete();
                    }
                }
                newFile.createNewFile();
                OutputStream output = new FileOutputStream(newFile,false);
                boolean downloaded = false;
                do {
                    try {
                        log.info("Downloading file - " + file.getName());
                        ftp.retrieveFile(file.getName(), output);
                        downloaded = true;
                        archive.setStatus(true);
                        if (file.getSize()!=newFile.length()){
                            log.error("files size not equal");
                            log.error("remote file - "+file.getSize());
                            log.error("local file - "+newFile.length());
                            archive.setStatus(false);
                            archive.setMessage("downloaded file size not equal original fise size");
                        }
                    } catch (IOException e) {
                        downloaded = false;
                        log.warn("Error downloading file - " + file.getName());
                        log.warn(e.getMessage());
                        if (e instanceof FTPConnectionClosedException || e instanceof ConnectException){
                            ftp = connectToFTP(serverAddress,userId,password,remoteDirectory);
                        }else if(!(e instanceof SocketTimeoutException)) {
                            throw e;
                        }
                    }
                }while(!downloaded);
                output.close();

                archive.setDownload(downloadRepository.findOne(downloadId));
                ftpArchiveRepository.save(archive);
                unzip(archive,"fz44");
            }
        }
        log.info("Done loading files");
        log.info("Files in fz44 are loaded. Disconnecting.");
        try {
            ftp.logout();
            ftp.disconnect();
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public FTPArchive reloadArchive(FTPArchive archive){
        String serverAddress;
        String userId;
        String password;
        String remoteDirectory;
        String localDirectory;
        String type;
        ftpArchiveRepository.delete(archive);
        if (archive.getName().startsWith("notification")){
            type="fz44";
            serverAddress = propertyService.get("app.ftp.fz44.server-address");
            userId = propertyService.get("app.ftp.fz44.user");
            password = propertyService.get("app.ftp.fz44.password");
            remoteDirectory = propertyService.get("app.ftp.fz44.directory");
            localDirectory = "downloaded/fz44";
        }else {
            type=archive.getName().substring(14,16);
            serverAddress = propertyService.get("app.ftp.fz223.server-address");
            userId = propertyService.get("app.ftp.fz223.user");
            password = propertyService.get("app.ftp.fz223.password");
            remoteDirectory = "out/published/Cheliabinskaya_obl/purchaseNotice"+type+"/daily";
            localDirectory = "downloaded/purchaseNotice"+type;
        }
        try {
            File workingDir = new File(localDirectory);
            workingDir.mkdirs();

            FTPClient ftp = connectToFTP(serverAddress,userId,password,remoteDirectory);

            FTPFile[] ftpFiles = ftp.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile file : ftpFiles) {
                    if (file.getName().equals(archive.getName())){
                        log.info("archive found");
                        Date date;
                        if(type.equals("fz44"))
                            date = getDateFz44(file.getName());
                        else
                            date = getDate(file.getName());
                        File newFile = new File(localDirectory+"/"+file.getName());
                        archive.setDate(date);
                        archive.setName(file.getName());
                        archive.setPath(newFile.getPath());
                        if(newFile.exists()){
                            newFile.delete();
                        }
                        newFile.createNewFile();
                        OutputStream output = new FileOutputStream(newFile,false);
                        try {
                            log.info("Downloading file - " + file.getName());
                            ftp.retrieveFile(file.getName(), output);
                            archive.setStatus(true);
                        } catch (IOException e) {
                            log.error("Error downloading file - " + file.getName());
                            log.error(e.getMessage());
                            archive.setStatus(false);
                            if (e instanceof FTPConnectionClosedException){
                                archive.setMessage("Archive wasn't loaded cause connection was closed");
                            } else {
                                archive.setMessage(e.getMessage());
                            }
                            ftpArchiveRepository.save(archive);
                            output.close();
                            continue;
                        }
                        unzip(archive,type);
                        if(archive.getStatus()){
                            for(XMLFile xmlFile:archive.getXmlFiles()){
                                xmlFile.setFtpArchive(archive);
                                if(xmlFile.getStatus()){
                                    if(type.equals("fz44"))
                                        parseFz44(xmlFile);
                                    else
                                        parse(xmlFile);
                                }
                            }
                        }
                        archive = ftpArchiveRepository.save(archive);
                        output.close();
                    }
                }
            }
            ftp.logout();
            ftp.disconnect();
        }catch (Exception e){
            if (e instanceof FTPConnectionClosedException){
                log.info(e.getMessage());
            }else {
                log.error(e.getMessage(),e);
            }
        }
        return archive;
    }

    public void reloadFile(XMLFile xmlFile){
        String serverAddress;
        String userId;
        String password;
        String remoteDirectory;
        String localDirectory;
        String type;
        if (xmlFile.getFtpArchive().getName().startsWith("notification")){
            type="fz44";
            serverAddress = propertyService.get("app.ftp.fz44.server-address");
            userId = propertyService.get("app.ftp.fz44.user");
            password = propertyService.get("app.ftp.fz44.password");
            remoteDirectory = propertyService.get("app.ftp.fz44.directory");
            localDirectory = "downloaded/fz44";
        }else {
            type=xmlFile.getFtpArchive().getName().substring(14,16);
            serverAddress = propertyService.get("app.ftp.fz223.server-address");
            userId = propertyService.get("app.ftp.fz223.user");
            password = propertyService.get("app.ftp.fz223.password");
            remoteDirectory = "out/published/Cheliabinskaya_obl/purchaseNotice"+type+"/daily";
            localDirectory = "downloaded/purchaseNotice"+type;
        }
        try {
            File workingDir = new File(localDirectory);
            workingDir.mkdirs();

            FTPClient ftp = connectToFTP(serverAddress,userId,password,remoteDirectory);

            FTPFile[] ftpFiles = ftp.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile file : ftpFiles) {
                    if (file.getName().equals(xmlFile.getFtpArchive().getName())){
                        log.info("archive found");
                        Date date;
                        if(type.equals("fz44"))
                            date = getDateFz44(file.getName());
                        else
                            date = getDate(file.getName());
                        File newFile = new File(localDirectory+"/"+file.getName());
                        xmlFile.getFtpArchive().setDate(date);
                        xmlFile.getFtpArchive().setName(file.getName());
                        xmlFile.getFtpArchive().setPath(newFile.getPath());
                        if(newFile.exists()){
                            newFile.delete();
                        }
                        newFile.createNewFile();
                        OutputStream output = new FileOutputStream(newFile,false);
                        try {
                            log.info("Downloading file - " + file.getName());
                            ftp.retrieveFile(file.getName(), output);
                            xmlFile.getFtpArchive().setStatus(true);
                        } catch (IOException e) {
                            log.error("Error downloading file - " + file.getName());
                            log.error(e.getMessage());
                            xmlFile.setStatus(false);
                            xmlFile.setMessage(e.getMessage());
                            output.close();
                            continue;
                        }
                        unzipWithName(xmlFile.getFtpArchive(),xmlFile.getName());
                        if(xmlFile.getFtpArchive().getStatus()){
                            for(XMLFile xml:xmlFile.getFtpArchive().getXmlFiles()){
                                xml.setFtpArchive(xmlFile.getFtpArchive());
                                if(xml.getStatus()&&xml.getName().equals(xmlFile.getName())){
                                    if(type.equals("fz44"))
                                        parseFz44(xml);
                                    else
                                        parse(xml);
                                    xmlFileRepository.delete(xmlFile);
                                    xmlFile=xml;
                                }
                            }
                        }
                        output.close();
                    }
                }
            }
            ftp.logout();
            ftp.disconnect();
        }catch (Exception e){
            if (e instanceof FTPConnectionClosedException){
                log.info(e.getMessage());
            }else {
                log.error(e.getMessage(),e);
            }
        }
        xmlFileRepository.save(xmlFile);
    }

    private void unzip(FTPArchive archive, String fz){
    	log.info("Unzipping " + archive.getName());
        File workingDir = new File("downloaded/unzipped");
        workingDir.mkdirs();
	    try {
			//archive.setXmlFiles(unzipFile(Paths.get(archive.getPath()), workingDir.toPath(),archive));
            if(fz.equals("fz44")){
                unzipFile(Paths.get(archive.getPath()), workingDir.toPath(),archive,fz);
            }else {
                unzipFile(Paths.get(archive.getPath()), workingDir.toPath(),archive,fz);
            }
		} catch (ZipError e) {
			archive.setStatus(false);
			archive.setMessage(e.getMessage());
        	log.error("ZipError unzipping "+archive.getName()+" "+e.getMessage(),e);
		} catch (Exception e) {
			archive.setStatus(false);
			archive.setMessage(e.getMessage());
			log.error("error unzipping "+archive.getName()+" "+e.getMessage(),e);
		}
    }
    	
    private List<XMLFile> unzipFile(Path filePath, final Path destination, final FTPArchive archive, final String fz) throws IOException {
    	Map<String, String> zipProperties = new HashMap<>();
    	zipProperties.put("create", "false");
    	zipProperties.put("encoding", "UTF-8");
    	URI zipFile = URI.create("jar:file:" + filePath.toUri().getPath().replace(" ", "%20"));
        final List<XMLFile> result = new ArrayList<>();
    	log.info("unzipping file - "+filePath.getFileName());
        
    	try (FileSystem zipfs = FileSystems.newFileSystem(zipFile, zipProperties)) {
            final Path rootPath = zipfs.getPath("/");
    	    Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = destination.resolve(rootPath.relativize(dir).toString());
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                	XMLFile xmlFile = new XMLFile();
                	xmlFile.setName(file.getFileName().toString());
                    if (Files.size(file)==0){
                        log.info("skipping file with size = 0 bytes "+file.getFileName());
                        return FileVisitResult.CONTINUE;
                    }else{
                        if(Files.exists(destination.resolve(rootPath.relativize(file).toString()))){
                            if(Files.size(file)==Files.size(destination.resolve(rootPath.relativize(file).toString()))){
                                xmlFile.setPath(destination.resolve(rootPath.relativize(file).toString()).toString());
                                xmlFile.setStatus(true);
                                log.info("file already exists- "+destination.resolve(rootPath.relativize(file).toString()));
                            }else {
                                Files.delete(destination.resolve(rootPath.relativize(file).toString()));
                                log.info("file exists but file sizes not equals- "+destination.resolve(rootPath.relativize(file).toString()));
                                Files.copy(file, destination.resolve(rootPath.relativize(file).toString()));
                                xmlFile.setPath(destination.resolve(rootPath.relativize(file).toString()).toString());
                                xmlFile.setStatus(true);
                            }
                        }else {
                            Files.copy(file, destination.resolve(rootPath.relativize(file).toString()));
                            xmlFile.setPath(destination.resolve(rootPath.relativize(file).toString()).toString());
                            xmlFile.setStatus(true);
                            log.info("created file - "+destination.resolve(rootPath.relativize(file).toString()));
                        }
                    }
                    xmlFile.setFtpArchive(archive);
                    if (xmlFile.getStatus()){
                        if (fz.equals("fz44")){
                            parseFz44(xmlFile);
                        }else{
                             parse(xmlFile);
                        }
                    }
                    xmlFileRepository.save(xmlFile);
                    //result.add(xmlFile);
                    return FileVisitResult.CONTINUE;
                }
    	    });
    	}
    	return result;
    }

    private void unzipWithName(FTPArchive archive, String name){
        log.info("Unzipping " + archive.getName());
        File workingDir = new File("downloaded/unzipped");
        workingDir.mkdirs();
        try {
            archive.setXmlFiles(unzipFileWithName(Paths.get(archive.getPath()), workingDir.toPath(),archive,name));
        } catch (ZipError e) {
            archive.setStatus(false);
            archive.setMessage(e.getMessage());
            log.error("ZipError unzipping "+archive.getName()+" "+e.getMessage(),e);
        } catch (Exception e) {
            archive.setStatus(false);
            archive.setMessage(e.getMessage());
            log.error("error unzipping "+archive.getName()+" "+e.getMessage(),e);
        }
    }

    private List<XMLFile> unzipFileWithName(Path filePath, final Path destination, final FTPArchive archive, final String name) throws IOException {
        Map<String, String> zipProperties = new HashMap<>();
        zipProperties.put("create", "false");
        zipProperties.put("encoding", "UTF-8");
        URI zipFile = URI.create("jar:file:" + filePath.toUri().getPath().replace(" ", "%20"));
        final List<XMLFile> result = new ArrayList<>();
        log.info("unzipping file - "+filePath.getFileName());

        try (FileSystem zipfs = FileSystems.newFileSystem(zipFile, zipProperties)) {
            final Path rootPath = zipfs.getPath("/");
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = destination.resolve(rootPath.relativize(dir).toString());
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    XMLFile xmlFile = new XMLFile();
                    xmlFile.setName(file.getFileName().toString());
                    if(!file.getFileName().toString().equals(name))
                        return FileVisitResult.CONTINUE;
                    if (Files.size(file)==0){
                        log.info("skipping file with size = 0 bytes "+file.getFileName());
                        return FileVisitResult.CONTINUE;
                    }else{
                        if(Files.exists(destination.resolve(rootPath.relativize(file).toString()))){
                            if(Files.size(file)==Files.size(destination.resolve(rootPath.relativize(file).toString()))){
                                xmlFile.setPath(destination.resolve(rootPath.relativize(file).toString()).toString());
                                xmlFile.setStatus(true);
                                log.info("file already exists- "+destination.resolve(rootPath.relativize(file).toString()));
                            }else {
                                Files.delete(destination.resolve(rootPath.relativize(file).toString()));
                                log.info("file exists but file sizes not equals- "+destination.resolve(rootPath.relativize(file).toString()));
                                Files.copy(file, destination.resolve(rootPath.relativize(file).toString()));
                                xmlFile.setPath(destination.resolve(rootPath.relativize(file).toString()).toString());
                                xmlFile.setStatus(true);
                            }
                        }else {
                            Files.copy(file, destination.resolve(rootPath.relativize(file).toString()));
                            xmlFile.setPath(destination.resolve(rootPath.relativize(file).toString()).toString());
                            xmlFile.setStatus(true);
                            log.info("created file - "+destination.resolve(rootPath.relativize(file).toString()));
                        }
                    }
                    xmlFile.setFtpArchive(archive);
                    result.add(xmlFile);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return result;
    }

    private void parseFz44(XMLFile file){
        log.info("parsing "+file.getName());
        try {
            file.setPurchase( fz44Parser.parse(file));
            file.setStatus(true);
        } catch (Exception e) {
            file.setStatus(false);
            file.setMessage(e.getMessage());
            log.error(e.getMessage(),e);
        }
    }

    private void parse(XMLFile file){
    	log.info("parsing "+file.getName());
    	try {
    	    file.setPurchase(fz223Parser.parse(file));
    		file.setStatus(true);
        } catch (Exception e) {
        	file.setStatus(false);
        	file.setMessage(e.getMessage());
            log.error(e.getMessage(),e);
        }
    }

    private void sendEmails(Download unload){
        List<Purchase> purchases = new ArrayList<>();
        for(FTPArchive archive:unload.getFtpArchives()){
            if (archive.getXmlFiles()!=null)
                for(XMLFile xmlFile:archive.getXmlFiles()){
                    if(xmlFile!=null && xmlFile.getPurchase()!=null && xmlFile.getPurchase().isAfter())
                        purchases.add(xmlFile.getPurchase());
                }
        }
    	mailService.sendNotification(purchases);
    }
    
    private Date getDate(String filename){
        Calendar calendar = new GregorianCalendar(Integer.valueOf(filename.substring(36,40)),
                Integer.valueOf(filename.substring(40,42))-1,Integer.valueOf(filename.substring(42,44)));
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    private Date getDateFz44(String filename){
        Calendar calendar = new GregorianCalendar(Integer.valueOf(filename.substring(32,36)),
                Integer.valueOf(filename.substring(36,38))-1,Integer.valueOf(filename.substring(38,40)));
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    private boolean archiveInDb(String name){
        Iterable<FTPArchive> archives = ftpArchiveRepository
                .findAll(
                    QFTPArchive.fTPArchive.name.eq(name).and(QFTPArchive.fTPArchive.status.isTrue())
                );
        return archives.iterator().hasNext();
    }

    private FTPClient connectToFTP(String serverAddress, String userId,
                                   String password, String remoteDirectory) throws Exception{
        FTPClient ftp = new FTPClient();
        ftp.setAutodetectUTF8(true);
        ftp.connect(serverAddress);
        if(!ftp.login(userId, password)){
            ftp.logout();
            throw new Exception("Cant connect to remote ftp. Invalid ftp properties");
        }
        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())){
            ftp.disconnect();
            throw new Exception("Cant connect to remote ftp. Invalid ftp properties");
        }
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
        ftp.changeWorkingDirectory(remoteDirectory);
        return ftp;
    }
}
