package org.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.Set;

@Entity
public class MainEntity {
    @Id
    @GeneratedValue
    private long id;
    private String value;

    @OneToMany(mappedBy = "main")
    private Set<RelationshipEntity> related;

    public Collection<RelationshipEntity> getRelated() {
        return related;
    }

    public void setRelated(Set<RelationshipEntity> related) {
        this.related = related;
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (id == 0) {
            return super.equals(o);
        }
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MainEntity that = (MainEntity) o;

        return getId() == that.getId();

    }

    @Override
    public int hashCode() {
        return getId() == 0 ? super.hashCode() : (int) (getId() ^ (getId() >>> 32));
    }
}
