package mockit;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Set;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedMethodsTest.
 */
@ExtendWith(JMockitExtension.class)
class TestedMethodsTest {

    /**
     * The Interface Dependency.
     */
    public interface Dependency {
    }

    /**
     * The Interface AnotherDependency.
     */
    public interface AnotherDependency {
    }

    /**
     * The Class DependencyImpl.
     */
    static class DependencyImpl implements Dependency {
    }

    /**
     * The Interface BaseDAO.
     *
     * @param <T>
     *            the generic type
     */
    @SuppressWarnings("unused")
    public interface BaseDAO<T extends Serializable> {
    }

    /**
     * The Interface ConcreteDAO.
     */
    public interface ConcreteDAO extends BaseDAO<String> {
    }

    /**
     * The Class DAOImpl.
     */
    static class DAOImpl implements ConcreteDAO {
    }

    /**
     * The Class TestedClass.
     */
    static class TestedClass {
        /** The dependency. */
        Dependency dependency;
        /** The dao. */
        ConcreteDAO dao;
        /** The another dependency. */
        AnotherDependency anotherDependency;
        /** The set. */
        Set<?> set;
    }

    /**
     * Resolve dependency interfaces.
     *
     * @param dependencyInterface
     *            the dependency interface
     *
     * @return the class<? extends dependency>
     */
    @Tested
    static Class<? extends Dependency> resolveDependencyInterfaces(Class<Dependency> dependencyInterface) {
        assertSame(Dependency.class, dependencyInterface);
        return DependencyImpl.class;
    }

    /**
     * Resolve DAO interfaces.
     *
     * @param daoInterface
     *            the dao interface
     *
     * @return the class
     */
    @Tested
    Class<?> resolveDAOInterfaces(Class<? extends BaseDAO<?>> daoInterface) {
        assertSame(ConcreteDAO.class, daoInterface);
        return DAOImpl.class;
    }

    /**
     * Resolve anything else.
     *
     * @param anyInterface
     *            the any interface
     *
     * @return the class
     */
    @Tested
    Class<?> resolveAnythingElse(Class<?> anyInterface) {
        assertSame(AnotherDependency.class, anyInterface);
        return null;
    }

    /** The tested. */
    @Tested(fullyInitialized = true)
    TestedClass tested;

    /**
     * Inject interface implementations from classes returned from tested methods.
     */
    @Test
    void injectInterfaceImplementationsFromClassesReturnedFromTestedMethods() {
        assertTrue(tested.dependency instanceof DependencyImpl);
        assertTrue(tested.dao instanceof DAOImpl);
        assertNull(tested.anotherDependency);
        assertNull(tested.set);
    }

    /**
     * The Class DAO1.
     */
    static final class DAO1 {
    }

    /**
     * The Class DAO2.
     */
    static final class DAO2 {
    }

    /**
     * The Interface BaseService.
     */
    public interface BaseService {
    }

    /**
     * The Class BaseServiceImpl.
     *
     * @param <D>
     *            the generic type
     */
    static class BaseServiceImpl<D> {
        /** The dao. */
        D dao;
    }

    /**
     * The Interface Service1.
     */
    public interface Service1 extends BaseService {
    }

    /**
     * The Interface Service2.
     */
    public interface Service2 extends BaseService {
    }

    /**
     * The Class ConcreteService1.
     */
    static final class ConcreteService1 extends BaseServiceImpl<DAO1> implements Service1 {
    }

    /**
     * The Class ConcreteService2.
     */
    static final class ConcreteService2 extends BaseServiceImpl<DAO2> implements Service2 {
        /** The service 1. */
        Service1 service1;
    }

    /**
     * Resolve service interfaces.
     *
     * @param baseServiceType
     *            the base service type
     *
     * @return the class<? extends base service impl<?>>
     */
    @Tested
    Class<? extends BaseServiceImpl<?>> resolveServiceInterfaces(Class<? extends BaseService> baseServiceType) {
        if (baseServiceType == Service1.class) {
            return ConcreteService1.class;
        }
        if (baseServiceType == Service2.class) {
            return ConcreteService2.class;
        }
        return null;
    }

    /**
     * Creates the complex objects with generic dependencies.
     *
     * @param service2
     *            the service 2
     */
    @Test
    void createComplexObjectsWithGenericDependencies(@Tested(fullyInitialized = true) ConcreteService2 service2) {
        assertTrue(service2.dao instanceof DAO2);
        Service1 service1 = service2.service1;
        assertTrue(service1 instanceof ConcreteService1);
        assertTrue(((ConcreteService1) service1).dao instanceof DAO1);
    }
}
