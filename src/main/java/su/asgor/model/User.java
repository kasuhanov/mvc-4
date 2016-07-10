package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "purchases")
public class User implements Principal{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="user_seq_gen")
    @SequenceGenerator(name="user_seq_gen", sequenceName="user_seq")
    private long id;
    private String email;
    private String password;
    private Boolean enabled = Boolean.FALSE;
    @Column(name = "notify_favs_change")
    private Boolean notifyFavsChange = Boolean.FALSE;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_purchase",  joinColumns = {
            @JoinColumn(name = "user_id", nullable = false, updatable = false) },
            indexes = {@Index(name = "user_purchase_index",columnList = "user_id, purchase_id")},
            inverseJoinColumns = { @JoinColumn(name = "purchase_id",
                    nullable = false, updatable = false) })
    @JsonIgnore
    private List<Purchase> favs;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_category",  joinColumns = {
            @JoinColumn(name = "user_id", nullable = false, updatable = false) },
            indexes = {@Index(name = "user_category_index",columnList = "user_id, category_id")},
            inverseJoinColumns = { @JoinColumn(name = "category_id",
                    nullable = false, updatable = false) })
    @JsonIgnore
    private List<Category> subscriptions = new ArrayList<>();

    public User() { }

    public User(long id) {
        this.id = id;
    }
    
	public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		//this.password =  new BCryptPasswordEncoder().encode(password);
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

    public List<Purchase> getFavs() {
        return favs;
    }

    public void setFavs(List<Purchase> favs) {
        this.favs = favs;
    }

    public List<Category> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Category> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Boolean getNotifyFavsChange() {
        return notifyFavsChange;
    }

    public void setNotifyFavsChange(Boolean notifyFavsChange) {
        this.notifyFavsChange = notifyFavsChange;
    }

    @Override
	public String toString() {
		return "User [email=" + email + ", password=" + password + "]";
	}

    @Override
    public String getName() {
        return email;
    }

}
