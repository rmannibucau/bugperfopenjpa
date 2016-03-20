package org.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class RelationshipEntity {
    @Id
    @GeneratedValue
    private long id;
    private String value;

    @ManyToOne
    private MainEntity main;

    public MainEntity getMain() {
        return main;
    }

    public void setMain(MainEntity main) {
        this.main = main;
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

        RelationshipEntity that = (RelationshipEntity) o;

        return getId() == that.getId();

    }

    @Override
    public int hashCode() {
        return getId() == 0 ? super.hashCode() : (int) (getId() ^ (getId() >>> 32));
    }
}
