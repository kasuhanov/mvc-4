package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "xml_file")
public class XMLFile {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="xml_file_seq_gen")
    @SequenceGenerator(name="xml_file_seq_gen", sequenceName="xml_file_seq")
    private long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "purchase_id")
    @JsonIgnore
    private Purchase purchase;
    @Transient
    private String purchase_id;
    @ManyToOne
    @JoinColumn(name = "ftp_archive_id", nullable = false)
    @JsonIgnore
    private FTPArchive ftpArchive;
    private Boolean status;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String path;
    
    public XMLFile(){}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Purchase getPurchase() {
		return purchase;
	}

	public void setPurchase(Purchase purchase) {
		this.purchase = purchase;
	}

	public FTPArchive getFtpArchive() {
		return ftpArchive;
	}

	public void setFtpArchive(FTPArchive ftpArchive) {
		this.ftpArchive = ftpArchive;
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

    public String getPurchase_id() {
        return purchase_id;
    }

    public void setPurchase_id(String purchase_id) {
        this.purchase_id = purchase_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public XMLFile setup(){
        if(purchase!=null)purchase_id = purchase.getId();
        return this;
    }
}
