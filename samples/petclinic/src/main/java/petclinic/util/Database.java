package petclinic.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Provides access to the application database, allowing transient instances of entity classes to be persisted, and
 * persistent instances to be recovered or removed from the database.
 */
@Transactional
public class Database {
    @PersistenceContext
    private EntityManager em;

    /**
     * Finds an entity in the application database given its class and unique id.
     *
     * @param <E>
     *            the element type
     * @param entityClass
     *            the entity class
     * @param id
     *            the id
     *
     * @return the persistent entity if found, or <code>null</code> if not found
     */
    @Nullable
    public <E extends BaseEntity> E findById(@NonNull Class<E> entityClass, int id) {
        return em.find(entityClass, id);
    }

    /**
     * Finds one or more persistent entities of a certain type in the application database.
     *
     * @param <E>
     *            the element type
     * @param qlStatement
     *            a JPQL "select" statement that locates entities of the same type
     * @param qlArgs
     *            zero or more argument values for the positional query parameters specified in the JPQL statement, in
     *            the same order as the parameter positions
     *
     * @return the list of zero or more entities found, in an arbitrary order or in the order specified by an "order by"
     *         clause (if any)
     *
     * @see #find(int, String, Object...)
     */
    @NonNull
    public <E extends BaseEntity> List<E> find(@NonNull String qlStatement, @NonNull Object... qlArgs) {
        return find(0, qlStatement, qlArgs);
    }

    /**
     * Finds one or more persistent entities of a certain type in the application database, up to a given maximum number
     * of entities.
     *
     * @param <E>
     *            the element type
     * @param maxResults
     *            the maximum number of resulting entities to be returned, or <code>0</code> if there is no limit
     * @param qlStatement
     *            a JPQL "select" statement that locates entities of the same type
     * @param qlArgs
     *            zero or more argument values for the positional query parameters specified in the JPQL statement, in
     *            the same order as the parameter positions
     *
     * @return the list of zero or more entities found, in an arbitrary order or in the order specified by an "order by"
     *         clause (if any)
     */
    @NonNull
    public <E extends BaseEntity> List<E> find(@NonNegative int maxResults, @NonNull String qlStatement,
            @NonNull Object... qlArgs) {
        Query query = em.createQuery(qlStatement);

        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }

        for (int i = 0; i < qlArgs.length; i++) {
            query.setParameter(i + 1, qlArgs[i]);
        }

        return query.getResultList();
    }

    /**
     * Saves the state of a given entity to the application database, whether it is new (still transient, with no id) or
     * already persisted (with an id).
     * <p>
     * In either case, the persistence context is synchronized to the application database, so that any pending
     * "inserts", "updates" or "deletes" get executed at this time.
     *
     * @param entity
     *            the entity
     */
    public void save(@NonNull BaseEntity entity) {
        if (entity.isNew()) {
            em.persist(entity);
        } else if (!em.contains(entity)) { // in case it is a detached entity
            em.merge(entity);
        }

        em.flush();
    }

    /**
     * Removes a given persistent entity from the application database.
     *
     * @param entity
     *            the entity
     */
    public void remove(@NonNull BaseEntity entity) {
        em.remove(entity);
    }
}
