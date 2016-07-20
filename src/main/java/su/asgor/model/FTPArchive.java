package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ftp_archive")
public class FTPArchive {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="ftp_arch_seq_gen")
    @SequenceGenerator(name="ftp_arch_seq_gen", sequenceName="ftp_arch_seq")
    private long id;
	@Column(columnDefinition = "TEXT")
    private String name;
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;
    @JsonIgnore
    @OneToMany(mappedBy = "ftpArchive", cascade=CascadeType.ALL)
    private List<XMLFile> xmlFiles;
    @ManyToOne
    @JoinColumn(name = "download_id", nullable = false)
    @JsonIgnore
    private Download download;
    private Boolean status;
    @Column(columnDefinition = "TEXT")
    private String message;
	@Column(columnDefinition = "TEXT")
    private String path;
    @Transient
    private int succeeded = 0;
    @Transient
    private int failed = 0;

    public FTPArchive(){}

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<XMLFile> getXmlFiles() {
		return xmlFiles;
	}

	public void setXmlFiles(List<XMLFile> xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	public Download getDownload() {
		return download;
	}

	public void setDownload(Download unload) {
		this.download = unload;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    public void setSucceeded(int succeeded){
        this.succeeded = succeeded;
    }

    public int getSucceeded(){
        return succeeded;
    }

    public void setFailed(int failed){
        this.failed = failed;
    }

    public int getFailed(){
        return failed;
    }
}
