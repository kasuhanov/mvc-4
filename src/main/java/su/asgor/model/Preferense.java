package su.asgor.model;

import javax.persistence.*;

import su.asgor.config.gson.Exclude;

@Entity
@Table(name = "preferense")
public class Preferense {
	@Id
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String name;
    @Column(columnDefinition = "TEXT")
    private Double value;
    @ManyToOne
    @JoinColumn(name = "purchase_id")
    @Exclude
    private Purchase purchase;

    public Preferense() { }

    public Preferense(Long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }
}
