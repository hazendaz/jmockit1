package petclinic.util;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

/**
 * Base class for all entity types, containing the id property, which is automatically generated.
 */
@MappedSuperclass
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = IDENTITY)
    protected Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == null;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseEntity)) {
            return false;
        }

        BaseEntity otherEntity = (BaseEntity) other;

        return id != null ? id.equals(otherEntity.id) : otherEntity.id == null;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public final int hashCode() {
        return id == null ? -1 : id;
    }
}
