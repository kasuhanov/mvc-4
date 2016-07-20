package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "lot")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Lot {
	@Id
    private String id;
    @Column(columnDefinition = "TEXT")
    private String name;
	@Column(columnDefinition = "TEXT")
    private String currency;
    @Column(columnDefinition = "TEXT")
    private String address;
    @Column(name = "start_price")
    private double startPrice;
    private Boolean forSMP;
    @ManyToOne
    @JoinColumn(name = "purchase_id")
    @JsonIgnore
    private Purchase purchase;
    @OneToMany(mappedBy = "lot", cascade={CascadeType.ALL},fetch = FetchType.LAZY)
    private List<LotItem> lotItems;
    
    public Lot() { }

    public Lot(String id) {
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public Boolean getForSMP() {
		return forSMP;
	}

	public void setForSMP(Boolean forSMP) {
		this.forSMP = forSMP;
	}

	public Purchase getPurchase() {
		return purchase;
	}

	public void setPurchase(Purchase purchase) {
		this.purchase = purchase;
	}

	public List<LotItem> getLotItems() {
		return lotItems;
	}

	public void setLotItems(List<LotItem> lotItems) {
		this.lotItems = lotItems;
	}

	@Override
	public String toString() {
		return "Lot [id=" + id + ", name=" + name + ", currency=" + currency + ", address=" + address + ", startPrice="
				+ startPrice + ", forSMP=" + forSMP + "]";
	}
	
}
