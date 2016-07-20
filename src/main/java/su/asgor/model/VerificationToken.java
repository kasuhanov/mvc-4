package su.asgor.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Table(name = "verification_token")
@Entity
public class VerificationToken {
    private static final int EXPIRATION = 60 * 24;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="token_seq_gen")
    @SequenceGenerator(name="token_seq_gen", sequenceName="token_seq")
    private Long id;
    private String token;
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    private Date expiryDate;

    public VerificationToken() {}

    public VerificationToken(String token, User user) {
        super();
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }
     
    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public String getToken() {
		return token;
	}

    public void setToken(String token) {
		this.token = token;
	}

    public User getUser() {
		return user;
	}

    public void setUser(User user) {
		this.user = user;
	}

    public Date getExpiryDate() {
		return expiryDate;
	}

    public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
}