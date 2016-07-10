package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "customer")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "purchases")
public class Customer {
    @Id
    private long id;
    @Column(columnDefinition = "TEXT")
    private String name;
    @Column(columnDefinition = "TEXT")
    private String email;
    @Column(columnDefinition = "TEXT")
    private String phone;
    @Column(name = "postal_address", columnDefinition = "TEXT")
    private String postalAddress;
    @Column(columnDefinition = "TEXT")
    private String fax;
    @OneToMany(mappedBy = "customer")
    private List<Purchase> purchases;
    public Customer() { }

    public Customer(long id) {
        this.id = id;
    }

    public Customer(String name) {
        this.name = name;
    }
    
    public Customer(long id, String name, String email, String phone, String postalAddress) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.postalAddress = postalAddress;
	}

	public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", email=" + email + ", phone=" + phone + ", postalAddress="
				+ postalAddress + ", fax=" + fax + "]";
	}
	
}
