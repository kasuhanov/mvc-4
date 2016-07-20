package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import su.asgor.parser.generated.fz223.PurchaseNoticeStatusType;
import su.asgor.service.PurchaseService;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        name = "purchase",
        indexes = {
                @Index(columnList = "start_price", name = "purchase_price_index"),
                @Index(columnList = "name", name = "purchase_name_index"),
                @Index(columnList = "customer_id", name = "purchase_customer_id_index"),
                @Index(columnList = "submission_close_date", name = "purchase_submission_close_date_index"),
                @Index(columnList = "type", name = "purchase_type_index")
        }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Purchase {
    @Id
    private String id;
    private PurchaseType type;
    @Transient
    @JsonIgnore
    private PurchaseNoticeStatusType status;
    @Transient
    private Boolean completed = Boolean.FALSE;
    @Column(columnDefinition = "TEXT")
    private String name;
    @Column(name = "submission_close_date")
    @Temporal(TemporalType.DATE)
    private Date submissionCloseDate;
    @Column(name = "start_price")
    private double startPrice;
    @Column(name = "purchase_code_name",columnDefinition = "TEXT")
    private String purchaseCodeName;
    @ManyToMany(mappedBy="favs")
    @JsonIgnore
    private List<User> users;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "purchase_category",  joinColumns = {
            @JoinColumn(name = "purchase_id", nullable = false, updatable = false) },
            indexes = {@Index(name = "purchase_category_index",columnList = "purchase_id, category_id"),
                    @Index(name = "category_purchase_index",columnList = "category_id, purchase_id")},
            inverseJoinColumns = { @JoinColumn(name = "category_id",
                    nullable = false, updatable = false) })
    @JsonIgnore
    private List<Category> categories = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @OneToMany(mappedBy = "purchase", cascade={CascadeType.ALL})
    private List<Document> document;
    @OneToMany(mappedBy = "purchase", cascade={CascadeType.ALL},fetch = FetchType.LAZY)
    private List<Lot> lots;
    // AE
    @Column(name = "auction_time")
    @Temporal(TemporalType.DATE)
    private Date auctionTime;
    @Column(name = "publication_date")
    @Temporal(TemporalType.DATE)
    private Date publicationDate;
    @Column(name = "documentation_delivery_procedure", columnDefinition = "TEXT")
    private String documentationDeliveryProcedure;
    @Column(name = "documentation_delivery_place", columnDefinition = "TEXT")
    private String documentationDeliveryPlace;
    @Column(name = "electronic_place_name", columnDefinition = "TEXT")
    private String electronicPlaceName;
    @Column(name = "electronic_place_url", columnDefinition = "TEXT")
    private String electronicPlaceUrl;
    // OA
    @Column(name="auction_place", columnDefinition = "TEXT")
    private String auctionPlace;
    // ZK
    @Column(name="quotation_examination_place", columnDefinition = "TEXT")
    private String quotationExaminationPlace;
    @Column(name = "quotation_examination_time")
    @Temporal(TemporalType.DATE)
    private Date quotationExaminationTime;
    //OK
    @Column(name="envelope_opening_place", columnDefinition = "TEXT")
    private String envelopeOpeningPlace;
    @Column(name = "envelope_opening_time")
    @Temporal(TemporalType.DATE)
    private Date envelopeOpeningTime;
    @Column(name="examination_place", columnDefinition = "TEXT")
    private String examinationPlace;
    @Column(name = "examination_date_time")
    @Temporal(TemporalType.DATE)
    private Date examinationDateTime;
    @Column(name="summingup_place", columnDefinition = "TEXT")
    private String summingupPlace;
    @Column(name = "summingup_time")
    @Temporal(TemporalType.DATE)
    private Date summingupTime;
    // Contact
    @Column(name = "contact_first_name", columnDefinition = "TEXT")
    private String contactFirstName;
    @Column(name = "contact_middle_name", columnDefinition = "TEXT")
    private String contactMiddleName;
    @Column(name = "contact_last_name", columnDefinition = "TEXT")
    private String contactLastName;
    @Column(name = "contact_email", columnDefinition = "TEXT")
    private String contactEmail;
    @Column(name = "contact_phone", columnDefinition = "TEXT")
    private String contactPhone;
    //debug
    @Column(name = "file_name", columnDefinition = "TEXT")
    private String fileName;
    @Column(name = "event_date")
    @Temporal(TemporalType.DATE)
    private Date eventDate;
    @JsonIgnore
    @Transient
    private boolean after;
    //download
    @OneToMany(mappedBy="purchase")
    @JsonIgnore
    private List<XMLFile> xmlFiles;
    //fz44
    private String fz;
    @Column(name = "contract_guarantee")
    private Double contractGuarantee;
    @Column(name = "application_guarantee")
    private Double applicationGuarantee;
    @OneToMany(mappedBy = "purchase", cascade={CascadeType.ALL}, fetch = FetchType.LAZY)
    private List<Preferense> preferenses;
    @Column(name="submission_place", columnDefinition = "TEXT")
    private String submissionPlace;
    @Column(name = "submission_order", columnDefinition = "TEXT")
    private String submissionOrder;
    
    public Purchase() { }

    public Purchase setupCompleted(){
        setCompleted(PurchaseService.isCompleted(this));
        return this;
    }

    public Purchase(String id) {
        this.id = id;
    }
    
	public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

	public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Date getSubmissionCloseDate() {
        return submissionCloseDate;
    }

    public void setSubmissionCloseDate(Date submissionCloseDate) {
        this.submissionCloseDate = submissionCloseDate;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

	public PurchaseType getType() {
		return type;
	}

	public void setType(PurchaseType type) {
		this.type = type;
	}

	public PurchaseNoticeStatusType getStatus() {
		return status;
	}

	public String getPurchaseCodeName() {
		return purchaseCodeName;
	}

	public void setPurchaseCodeName(String purchaseCodeName) {
		this.purchaseCodeName = purchaseCodeName;
	}

	public void setStatus(PurchaseNoticeStatusType status) {
		this.status = status;
	}
	
	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public Date getAuctionTime() {
		return auctionTime;
	}

	public void setAuctionTime(Date auctionTime) {
		this.auctionTime = auctionTime;
	}

	public String getDocumentationDeliveryPlace() {
		return documentationDeliveryPlace;
	}

	public void setDocumentationDeliveryPlace(String documentationDeliveryPlace) {
		this.documentationDeliveryPlace = documentationDeliveryPlace;
	}

	public String getDocumentationDeliveryProcedure() {
		return documentationDeliveryProcedure;
	}

	public void setDocumentationDeliveryProcedure(String documentationDeliveryProcedure) {
		this.documentationDeliveryProcedure = documentationDeliveryProcedure;
	}

    public boolean isAfter() {
        return after;
    }

    public void setAfter(boolean after) {
        this.after = after;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getElectronicPlaceName() {
		return electronicPlaceName;
	}

	public void setElectronicPlaceName(String electronicPlaceName) {
		this.electronicPlaceName = electronicPlaceName;
	}

	public String getElectronicPlaceUrl() {
		return electronicPlaceUrl;
	}

	public void setElectronicPlaceUrl(String electronicPlaceUrl) {
		this.electronicPlaceUrl = electronicPlaceUrl;
	}

	public String getContactFirstName() {
		return contactFirstName;
	}

	public void setContactFirstName(String contactFirstName) {
		this.contactFirstName = contactFirstName;
	}

	public String getContactMiddleName() {
		return contactMiddleName;
	}

	public void setContactMiddleName(String contactMiddleName) {
		this.contactMiddleName = contactMiddleName;
	}

	public String getContactLastName() {
		return contactLastName;
	}

	public void setContactLastName(String contactLastName) {
		this.contactLastName = contactLastName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public List<Document> getDocument() {
		return document;
	}

	public void setDocument(List<Document> document) {
		this.document = document;
	}

	public String getAuctionPlace() {
		return auctionPlace;
	}

	public void setAuctionPlace(String auctionPlace) {
		this.auctionPlace = auctionPlace;
	}

	public String getQuotationExaminationPlace() {
		return quotationExaminationPlace;
	}

	public void setQuotationExaminationPlace(String quotationExaminationPlace) {
		this.quotationExaminationPlace = quotationExaminationPlace;
	}

	public Date getQuotationExaminationTime() {
		return quotationExaminationTime;
	}

	public void setQuotationExaminationTime(Date quotationExaminationTime) {
		this.quotationExaminationTime = quotationExaminationTime;
	}

	public String getEnvelopeOpeningPlace() {
		return envelopeOpeningPlace;
	}

	public void setEnvelopeOpeningPlace(String envelopeOpeningPlace) {
		this.envelopeOpeningPlace = envelopeOpeningPlace;
	}

	public Date getEnvelopeOpeningTime() {
		return envelopeOpeningTime;
	}

	public void setEnvelopeOpeningTime(Date envelopeOpeningTime) {
		this.envelopeOpeningTime = envelopeOpeningTime;
	}

	public String getExaminationPlace() {
		return examinationPlace;
	}

	public void setExaminationPlace(String examinationPlace) {
		this.examinationPlace = examinationPlace;
	}

	public Date getExaminationDateTime() {
		return examinationDateTime;
	}

	public void setExaminationDateTime(Date examinationDateTime) {
		this.examinationDateTime = examinationDateTime;
	}

	public String getSummingupPlace() {
		return summingupPlace;
	}

	public void setSummingupPlace(String summingupPlace) {
		this.summingupPlace = summingupPlace;
	}

	public Date getSummingupTime() {
		return summingupTime;
	}

	public void setSummingupTime(Date summingupTime) {
		this.summingupTime = summingupTime;
	}

	public List<Lot> getLots() {
		return lots;
	}

	public void setLots(List<Lot> lots) {
		this.lots = lots;
	}

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<XMLFile> getXmlFiles() {
        return xmlFiles;
    }

    public void setXmlFiles(List<XMLFile> xmlFiles) {
        this.xmlFiles = xmlFiles;
    }

    public String getFz() {
        return fz;
    }

    public void setFz(String fz) {
        this.fz = fz;
    }

    public Double getApplicationGuarantee() {
        return applicationGuarantee;
    }

    public void setApplicationGuarantee(Double applicationGuarantee) {
        this.applicationGuarantee = applicationGuarantee;
    }

    public Double getContractGuarantee() {
        return contractGuarantee;
    }

    public void setContractGuarantee(Double contractGuarantee) {
        this.contractGuarantee = contractGuarantee;
    }

    public List<Preferense> getPreferenses() {
        return preferenses;
    }

    public void setPreferenses(List<Preferense> preferenses) {
        this.preferenses = preferenses;
    }

    public String getSubmissionOrder() {
        return submissionOrder;
    }

    public void setSubmissionOrder(String submissionOrder) {
        this.submissionOrder = submissionOrder;
    }

    public String getSubmissionPlace() {
        return submissionPlace;
    }

    public void setSubmissionPlace(String submissionPlace) {
        this.submissionPlace = submissionPlace;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "auctionPlace='" + auctionPlace + '\'' +
                ", id=" + id +
                ", type=" + type +
                ", status=" + status +
                ", isCompleted=" + completed +
                ", name='" + name + '\'' +
                ", submissionCloseDate=" + submissionCloseDate +
                ", startPrice=" + startPrice +
                ", purchaseCodeName='" + purchaseCodeName + '\'' +
                ", users=" + users +
                ", categories=" + categories +
                ", customer=" + customer +
                ", document=" + document +
                ", lots=" + lots +
                ", auctionTime=" + auctionTime +
                ", publicationDate=" + publicationDate +
                ", documentationDeliveryProcedure='" + documentationDeliveryProcedure + '\'' +
                ", documentationDeliveryPlace='" + documentationDeliveryPlace + '\'' +
                ", electronicPlaceName='" + electronicPlaceName + '\'' +
                ", electronicPlaceUrl='" + electronicPlaceUrl + '\'' +
                ", quotationExaminationPlace='" + quotationExaminationPlace + '\'' +
                ", quotationExaminationTime=" + quotationExaminationTime +
                ", envelopeOpeningPlace='" + envelopeOpeningPlace + '\'' +
                ", envelopeOpeningTime=" + envelopeOpeningTime +
                ", examinationPlace='" + examinationPlace + '\'' +
                ", examinationDateTime=" + examinationDateTime +
                ", summingupPlace='" + summingupPlace + '\'' +
                ", summingupTime=" + summingupTime +
                ", contactFirstName='" + contactFirstName + '\'' +
                ", contactMiddleName='" + contactMiddleName + '\'' +
                ", contactLastName='" + contactLastName + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
