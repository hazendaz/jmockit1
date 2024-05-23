package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class TestedClassWithFullAnnotatedDITest.
 */
@SuppressWarnings("ParameterHidesMemberVariable")
final class TestedClassWithFullAnnotatedDITest {

    /**
     * The Class DummyDataSource.
     */
    public static class DummyDataSource implements DataSource {

        /** The url. */
        private String url;

        /** The user. */
        private String user;

        /** The password. */
        String password;

        /**
         * Gets the url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the url.
         *
         * @param url
         *            the new url
         */
        @SuppressWarnings("unused")
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Gets the user.
         *
         * @return the user
         */
        public String getUser() {
            return user;
        }

        /**
         * Sets the user.
         *
         * @param user
         *            the new user
         */
        @SuppressWarnings("unused")
        public void setUser(String user) {
            this.user = user;
        }

        /**
         * Sets the password.
         *
         * @param password
         *            the new password
         */
        @SuppressWarnings("unused")
        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public Connection getConnection() {
            return null;
        }

        @Override
        public Connection getConnection(String username, String password) {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() {
            return null;
        }
    }

    /**
     * The Class TestedClass.
     */
    @DataSourceDefinition(name = "java:global/jdbc/testDS", className = "mockit.TestedClassWithFullAnnotatedDITest$DummyDataSource", url = "jdbc:testDb:test", user = "tests", password = "test123")
    static final class TestedClass {

        /** The action. */
        @Inject
        Runnable action;

        /** The dependency 1. */
        @Autowired
        ItfWithSingleImpl dependency1;

        /** The dependency 2. */
        @Resource
        ItfWithSingleImpl dependency2;

        /** The another dependency. */
        @Inject
        ItfWithTwoImpls anotherDependency;

        /** The log 1. */
        @Inject
        private Logger log1;

        /** The log 2. */
        @Inject
        private Logger log2;

        /** The collaborator. */
        Collaborator collaborator;

        /** The conversation. */
        @Inject
        Conversation conversation;

        /** The ds. */
        @Resource(lookup = "java:global/jdbc/testDS")
        DataSource ds;
    }

    /**
     * The Class PooledDataSource.
     */
    public static final class PooledDataSource extends DummyDataSource implements ConnectionPoolDataSource {
        @Override
        public PooledConnection getPooledConnection() {
            return null;
        }

        @Override
        public PooledConnection getPooledConnection(String user, String password) {
            return null;
        }
    }

    /**
     * The Class DistributedDataSource.
     */
    public static final class DistributedDataSource extends DummyDataSource implements XADataSource {
        @Override
        public XAConnection getXAConnection() {
            return null;
        }

        @Override
        public XAConnection getXAConnection(String user, String password) {
            return null;
        }
    }

    /**
     * The Class AnotherTestedClass.
     */
    @DataSourceDefinitions({
            @DataSourceDefinition(name = "regularDS", className = "mockit.TestedClassWithFullAnnotatedDITest$DummyDataSource", url = "jdbc:oracle:test", user = "tests", password = "test123"),
            @DataSourceDefinition(name = "pooledDS", className = "mockit.TestedClassWithFullAnnotatedDITest$PooledDataSource", url = "jdbc:hsqldb:db", user = "pool", password = "test123"),
            @DataSourceDefinition(name = "distributedDS", className = "mockit.TestedClassWithFullAnnotatedDITest$DistributedDataSource", url = "jdbc:postgresql:database", user = "xa", password = "test123") })
    static class AnotherTestedClass {

        /** The ds 1. */
        @Resource(lookup = "regularDS")
        DataSource ds1;

        /** The ds 2. */
        @Resource(lookup = "pooledDS")
        ConnectionPoolDataSource ds2;

        /** The ds 3. */
        @Resource(lookup = "distributedDS")
        XADataSource ds3;

        /** The ds 4. */
        @Resource(name = "regularDS")
        DataSource ds4;
    }

    /**
     * The Class Collaborator.
     */
    static class Collaborator {
    }

    /**
     * The Interface ItfWithSingleImpl.
     */
    public interface ItfWithSingleImpl {
    }

    /**
     * The Class SingleImpl.
     */
    public static final class SingleImpl implements ItfWithSingleImpl {
        /** The ejb. */
        @EJB
        ItfToBeMocked ejb;
    }

    /**
     * The Interface ItfWithTwoImpls.
     */
    public interface ItfWithTwoImpls {
    }

    /**
     * The Class Impl1.
     */
    @SuppressWarnings("unused")
    public static final class Impl1 implements ItfWithTwoImpls {
    }

    /**
     * The Class Impl2.
     */
    public static final class Impl2 implements ItfWithTwoImpls {
    }

    /**
     * The Interface ItfToBeMocked.
     */
    public interface ItfToBeMocked {
    }

    /** The dep 1. */
    @Tested
    SingleImpl dep1;

    /** The another dep. */
    @Tested
    Impl2 anotherDep;

    /** The collaborator. */
    @Tested
    Collaborator collaborator;

    /** The tested. */
    @Tested(fullyInitialized = true)
    TestedClass tested;

    /** The action. */
    // Without these injectables, a "missing @Injectable" exception occurs for each unresolved field.
    @Injectable
    Runnable action;

    /** The ejb. */
    @Injectable
    ItfToBeMocked ejb;

    /**
     * Inject initialized dependencies for interfaces having tested objects of implementation class types.
     */
    @Test
    void injectInitializedDependenciesForInterfacesHavingTestedObjectsOfImplementationClassTypes() {
        assertSame(action, tested.action);
        assertNotNull(tested.dependency1);
        assertSame(tested.dependency1, tested.dependency2);
        assertTrue(tested.anotherDependency instanceof Impl2);
        assertSame(ejb, ((SingleImpl) tested.dependency1).ejb);
    }

    /**
     * Inject logger fields with logger created with tested class name.
     */
    @Test
    void injectLoggerFieldsWithLoggerCreatedWithTestedClassName() {
        assertEquals(TestedClass.class.getName(), tested.log1.getName());
        assertSame(tested.log2, tested.log1);
    }

    /**
     * Inject non annotated field from matching tested field.
     */
    @Test
    void injectNonAnnotatedFieldFromMatchingTestedField() {
        assertSame(collaborator, tested.collaborator);
    }

    /** The conversation. */
    @Tested
    Conversation conversation;

    /**
     * Manage conversation context.
     */
    @Test
    void manageConversationContext() {
        assertNotNull(conversation);
        assertSame(tested.conversation, conversation);
        assertTrue(conversation.isTransient());

        assertEquals(0, conversation.getTimeout());
        conversation.setTimeout(1500);
        assertEquals(1500, conversation.getTimeout());

        assertNull(conversation.getId());

        conversation.begin();
        assertFalse(conversation.isTransient());
        assertNotNull(conversation.getId());

        conversation.end();
        assertTrue(conversation.isTransient());
        assertNull(conversation.getId());

        conversation.begin("test");
        assertFalse(conversation.isTransient());
        assertEquals("test", conversation.getId());
    }

    /**
     * Inject data source configured from single data source definition.
     */
    @Test
    void injectDataSourceConfiguredFromSingleDataSourceDefinition() {
        assertTrue(tested.ds instanceof DummyDataSource);

        DummyDataSource ds = (DummyDataSource) tested.ds;
        assertEquals("jdbc:testDb:test", ds.getUrl());
        assertEquals("tests", ds.getUser());
        assertEquals("test123", ds.password);
    }

    /** The tested 2. */
    @Tested(fullyInitialized = true)
    AnotherTestedClass tested2;

    /**
     * Inject multiple data sources configured from different data source definitions.
     */
    @Test
    void injectMultipleDataSourcesConfiguredFromDifferentDataSourceDefinitions() {
        assertTrue(tested2.ds1 instanceof DummyDataSource);
        assertTrue(tested2.ds2 instanceof PooledDataSource);
        assertTrue(tested2.ds3 instanceof DistributedDataSource);
        assertSame(tested2.ds1, tested2.ds4);
    }
}
