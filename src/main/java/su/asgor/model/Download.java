package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "download")
public class Download {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="download_seq_gen")
    @SequenceGenerator(name="download_seq_gen", sequenceName="download_seq")
    private long id;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @JsonIgnore
    @OneToMany(mappedBy = "download", cascade=CascadeType.PERSIST)
    private List<FTPArchive> ftpArchives = new ArrayList<>();
    @Transient
    private int succeeded = 0;
    @Transient
    private int failed = 0;
    @Transient
    private int failedArchive = 0;
    @Column(columnDefinition = "TEXT")
    private String message = "";

    public Download(){}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

	public List<FTPArchive> getFtpArchives() {
		return ftpArchives;
	}

	public void setFtpArchives(List<FTPArchive> ftpArchives) {
		this.ftpArchives = ftpArchives;
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

    public int getFailedArchive() {
        return failedArchive;
    }

    public void setFailedArchive(int failedArchive) {
        this.failedArchive = failedArchive;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Download{" +
                "id=" + id +
                ", date=" + date +
                ", message='" + message + '\'' +
                '}';
    }
}
