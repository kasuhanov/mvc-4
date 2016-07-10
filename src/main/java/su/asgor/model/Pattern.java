package su.asgor.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pattern")
public class Pattern {
    @Id
    private String id;
    private String pattern;
    @ManyToMany(mappedBy="patterns",fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    private List<Category> categories = new ArrayList<>();

    public Pattern() {}

    public Pattern(String pattern) {
        id = UUID.randomUUID().toString();
        this.pattern  = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category){
        categories.add(category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern1 = (Pattern) o;

        return pattern.equals(pattern1.pattern);

    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "pattern='" + pattern + '\'' +
                ", categories=" + categories +
                '}';
    }
}
