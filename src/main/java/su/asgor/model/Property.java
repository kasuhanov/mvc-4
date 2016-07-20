package su.asgor.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

@Entity
@Table(name = "property")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="property_seq_gen")
    @SequenceGenerator(name="property_seq_gen", sequenceName="property_seq")
    private long id;
    private String name;
    private String value;

    public Property(){}

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
