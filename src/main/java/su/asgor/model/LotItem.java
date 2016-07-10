package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lot_item")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotItem {
	@Id
    private String id;
    @Column(columnDefinition = "TEXT")
    private String name;
    @Column(name="ordinal_number")
    private int ordinalNumber;
    @Column(name="code_okdp",columnDefinition = "TEXT")
    private String codeOkdp;
    @Column(name="code_okved",columnDefinition = "TEXT")
    private String codeOkved;
    @Column(name="code_okpd2",columnDefinition = "TEXT")
    private String codeOkpd2;
    @Column(name="code_okved2",columnDefinition = "TEXT")
    private String codeOkved2;
    private BigDecimal qty;
    @Column(name="okei_name",columnDefinition = "TEXT")
    private String okeiName;
    @Column(name="additional_info", columnDefinition = "TEXT")
    private String additionalInfo;
    @ManyToOne
    @JoinColumn(name = "lot_id")
    @JsonIgnore
    private Lot lot;
    //fz44
    private Double price;
    private Double sum;

    public LotItem() { }

    public String getId() {
		return id;
	}

    public void setId(String id) {
		this.id = id;
	}

    public String getName() {
		return name;
	}

    public void setName(String name) {
		this.name = name;
	}

    public int getOrdinalNumber() {
		return ordinalNumber;
	}

    public void setOrdinalNumber(int ordinalNumber) {
		this.ordinalNumber = ordinalNumber;
	}

    public String getCodeOkdp() {
		return codeOkdp;
	}

    public void setCodeOkdp(String codeOkdp) {
		this.codeOkdp = codeOkdp;
	}

    public String getCodeOkved() {
		return codeOkved;
	}

    public void setCodeOkved(String codeOkved) {
		this.codeOkved = codeOkved;
	}

    public BigDecimal getQty() {
		return qty;
	}

    public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

    public String getOkeiName() {
		return okeiName;
	}

    public void setOkeiName(String okeiName) {
		this.okeiName = okeiName;
	}

    public String getAdditionalInfo() {
		return additionalInfo;
	}

    public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

    public Lot getLot() {
		return lot;
	}

    public void setLot(Lot lot) {
		this.lot = lot;
	}

    public String getCodeOkved2() {
        return codeOkved2;
    }

    public void setCodeOkved2(String codeOkved2) {
        this.codeOkved2 = codeOkved2;
    }

    public String getCodeOkpd2() {
        return codeOkpd2;
    }

    public void setCodeOkpd2(String codeOkpd2) {
        this.codeOkpd2 = codeOkpd2;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    @Override
	public String toString() {
		return "LotItem [id=" + id + ", name=" + name + ", ordinalNumber=" + ordinalNumber + ", codeOkdp=" + codeOkdp
				+ ", codeOkved=" + codeOkved + ", qty=" + qty + ", okeiName=" + okeiName + ", additionalInfo="
				+ additionalInfo + "]";
	}
	
}
