package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.servlet.ServletContextListener;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mockit.internal.ClassFile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CapturingImplementationsTest.
 */
final class CapturingImplementationsTest {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(CapturingImplementationsTest.class);

    /**
     * The Interface ServiceToBeStubbedOut.
     */
    interface ServiceToBeStubbedOut {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /** The unused. */
    // Just to cause any implementing classes to be stubbed out.
    @Capturing
    ServiceToBeStubbedOut unused;

    /**
     * The Class ServiceLocator.
     */
    static final class ServiceLocator {

        /**
         * Gets the single instance of ServiceLocator.
         *
         * @param <S>
         *            the generic type
         * @param serviceInterface
         *            the service interface
         *
         * @return single instance of ServiceLocator
         */
        @SuppressWarnings("unused")
        static <S> S getInstance(Class<S> serviceInterface) {
            ServiceToBeStubbedOut service = new ServiceToBeStubbedOut() {
                @Override
                public int doSomething() {
                    return 10;
                }
            };
            // noinspection unchecked
            return (S) service;
        }
    }

    /**
     * Capture implementation loaded by service locator.
     */
    @Test
    void captureImplementationLoadedByServiceLocator() {
        ServiceToBeStubbedOut service = ServiceLocator.getInstance(ServiceToBeStubbedOut.class);
        assertEquals(0, service.doSomething());
    }

    /**
     * The Interface Service1.
     */
    public interface Service1 {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /**
     * The Class Service1Impl.
     */
    static final class Service1Impl implements Service1 {
        @Override
        public int doSomething() {
            return 1;
        }
    }

    /** The mock service 1. */
    @Capturing
    Service1 mockService1;

    /**
     * Capture implementation using mock field.
     */
    @Test
    void captureImplementationUsingMockField() {
        Service1 service = new Service1Impl();

        new Expectations() {
            {
                mockService1.doSomething();
                returns(2, 3);
            }
        };

        assertEquals(2, service.doSomething());
        assertEquals(3, new Service1Impl().doSomething());
    }

    /**
     * The Interface Service2.
     */
    public interface Service2 {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /**
     * The Class Service2Impl.
     */
    static final class Service2Impl implements Service2 {
        @Override
        public int doSomething() {
            return 1;
        }
    }

    /**
     * Capture implementation using mock parameter.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureImplementationUsingMockParameter(@Capturing final Service2 mock) {
        Service2Impl service = new Service2Impl();

        new Expectations() {
            {
                mock.doSomething();
                returns(3, 2);
            }
        };

        assertEquals(3, service.doSomething());
        assertEquals(2, new Service2Impl().doSomething());
    }

    /**
     * The Class AbstractService.
     */
    public abstract static class AbstractService {
        /**
         * Do something.
         *
         * @return true, if successful
         */
        protected abstract boolean doSomething();
    }

    /**
     * The Class DefaultServiceImpl.
     */
    static final class DefaultServiceImpl extends AbstractService {
        @Override
        protected boolean doSomething() {
            return true;
        }
    }

    /**
     * Capture implementation of abstract class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureImplementationOfAbstractClass(@Capturing AbstractService mock) {
        assertFalse(new DefaultServiceImpl().doSomething());

        assertFalse(new AbstractService() {
            @Override
            protected boolean doSomething() {
                throw new RuntimeException();
            }
        }.doSomething());
    }

    /** The Constant customLoadedClass. */
    static final Class<? extends Service2> customLoadedClass = new ClassLoader() {
        @Override
        protected Class<? extends Service2> findClass(String name) {
            byte[] bytecode = ClassFile.readBytesFromClassFile(name.replace('.', '/'));
            // noinspection unchecked
            return (Class<? extends Service2>) defineClass(name, bytecode, 0, bytecode.length);
        }
    }.findClass(Service2Impl.class.getName());

    /** The service 2. */
    Service2 service2;

    /**
     * Instantiate custom loaded class.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void instantiateCustomLoadedClass() throws Exception {
        Constructor<?> defaultConstructor = customLoadedClass.getDeclaredConstructors()[0];
        defaultConstructor.setAccessible(true);
        service2 = (Service2) defaultConstructor.newInstance();
    }

    /**
     * Capture class previously loaded by class loader other than context.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureClassPreviouslyLoadedByClassLoaderOtherThanContext(@Capturing final Service2 mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = 15;
            }
        };

        assertEquals(15, service2.doSomething());
    }

    /**
     * The Interface Service3.
     */
    public interface Service3 {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /** The proxy instance. */
    static Service3 proxyInstance;

    /**
     * Generate dynamic proxy class.
     */
    @BeforeAll
    static void generateDynamicProxyClass() {
        ClassLoader loader = Service3.class.getClassLoader();
        Class<?>[] interfaces = { Service3.class };
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            fail("Should be mocked out");
            return null;
        };

        proxyInstance = (Service3) Proxy.newProxyInstance(loader, interfaces, invocationHandler);
    }

    /**
     * Capture dynamically generated proxy class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureDynamicallyGeneratedProxyClass(@Capturing final Service3 mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = 123;
            }
        };

        assertEquals(123, proxyInstance.doSomething());
    }

    /**
     * The Interface Interface.
     */
    interface Interface {
        /**
         * Op.
         */
        void op();
    }

    /**
     * The Interface SubInterface.
     */
    interface SubInterface extends Interface {
    }

    /**
     * The Class Implementation.
     */
    static class Implementation implements SubInterface {
        @Override
        public void op() {
            throw new RuntimeException();
        }
    }

    /**
     * Capture class implementing sub interface of captured interface.
     *
     * @param base
     *            the base
     */
    @Test
    void captureClassImplementingSubInterfaceOfCapturedInterface(@Capturing Interface base) {
        Interface impl = new Implementation();
        impl.op();
    }

    /**
     * Capture classes from the java management API.
     *
     * @param anyThreadMXBean
     *            the any thread MX bean
     */
    @Test
    void captureClassesFromTheJavaManagementAPI(@Capturing ThreadMXBean anyThreadMXBean) {
        ThreadMXBean threadingBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadingBean.getThreadCount();

        assertEquals(0, threadCount);
    }

    /**
     * Capture classes from the SAX parser API.
     *
     * @param anyParser
     *            the any parser
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void captureClassesFromTheSAXParserAPI(@Capturing final SAXParser anyParser) throws Exception {
        new Expectations() {
            {
                anyParser.isNamespaceAware();
                result = true;
            }
        };

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        boolean b = parser.isNamespaceAware();

        assertTrue(b);
    }

    /**
     * Capture classes from the java concurrency API.
     *
     * @param anyExecutorService
     *            the any executor service
     */
    @Test
    void captureClassesFromTheJavaConcurrencyAPI(@Capturing ExecutorService anyExecutorService) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(2);
        ExecutorService cachedThreadPoolExecutor = Executors.newCachedThreadPool();
        ExecutorService scheduledThreadPoolExecutor = Executors.newScheduledThreadPool(3);

        // These calls would throw a NPE unless mocked.
        singleThreadExecutor.submit((Runnable) null);
        threadPoolExecutor.submit((Runnable) null);
        cachedThreadPoolExecutor.submit((Runnable) null);
        scheduledThreadPoolExecutor.submit((Callable<Object>) null);
    }

    /**
     * The Interface Interface2.
     */
    interface Interface2 {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /**
     * The Interface SubInterface2.
     */
    interface SubInterface2 extends Interface2 {
    }

    /**
     * The Class ClassImplementingSubInterfaceAndExtendingUnrelatedBase.
     */
    static class ClassImplementingSubInterfaceAndExtendingUnrelatedBase extends Implementation
            implements SubInterface2 {
        @Override
        public int doSomething() {
            return 123;
        }
    }

    /**
     * Capture class which implements captured base interface and extends unrelated base.
     *
     * @param captured
     *            the captured
     */
    @Test
    void captureClassWhichImplementsCapturedBaseInterfaceAndExtendsUnrelatedBase(@Capturing Interface2 captured) {
        int i = new ClassImplementingSubInterfaceAndExtendingUnrelatedBase().doSomething();

        assertEquals(0, i);
    }

    /**
     * The Class Base.
     *
     * @param <T>
     *            the generic type
     */
    static class Base<T> {

        /**
         * Do something.
         *
         * @return the t
         */
        T doSomething() {
            return null;
        }

        /**
         * Do something.
         *
         * @param t
         *            the t
         */
        void doSomething(T t) {
            logger.info("test");
        }

        /**
         * Do something return.
         *
         * @param t
         *            the t
         *
         * @return the t
         */
        T doSomethingReturn(T t) {
            return t;
        }
    }

    /**
     * The Class Impl.
     */
    static final class Impl extends Base<Integer> {
        @Override
        Integer doSomething() {
            return 1;
        }

        @Override
        void doSomething(Integer i) {
        }

        @Override
        Integer doSomethingReturn(Integer t) {
            return null;
        }
    }

    /**
     * Capture implementations of generic type.
     *
     * @param anyInstance
     *            the any instance
     */
    @Test
    void captureImplementationsOfGenericType(@Capturing final Base<Integer> anyInstance) {
        new Expectations() {
            {
                anyInstance.doSomething();
                result = 2;
                anyInstance.doSomethingReturn(0);
                anyInstance.doSomething(0);
            }
        };

        Base<Integer> impl = new Impl();
        int i = impl.doSomething();
        impl.doSomethingReturn(0);
        impl.doSomething(0);

        assertEquals(2, i);
    }

    /**
     * The Class Base2.
     */
    static class Base2 {
        /**
         * Base.
         */
        void base() {
        }
    }

    /**
     * The Class Sub.
     */
    static class Sub extends Base2 {
    }

    /**
     * The Class Sub2.
     */
    static class Sub2 extends Sub {
        @Override
        void base() {
            throw new RuntimeException();
        }
    }

    /**
     * Verify invocation to method from base class on captured subclass of intermediate subclass.
     *
     * @param sub
     *            the sub
     */
    @Test
    void verifyInvocationToMethodFromBaseClassOnCapturedSubclassOfIntermediateSubclass(@Capturing final Sub sub) {
        Sub impl = new Sub2();
        impl.base();

        new Verifications() {
            {
                sub.base();
            }
        };
    }

    /**
     * The Interface BaseItf.
     */
    public interface BaseItf {
        /**
         * Base.
         */
        void base();
    }

    /**
     * The Interface SubItf.
     */
    public interface SubItf extends BaseItf {
    }

    /**
     * Verify invocation to base interface method on captured implementation of sub interface.
     *
     * @param sub
     *            the sub
     */
    @Test
    void verifyInvocationToBaseInterfaceMethodOnCapturedImplementationOfSubInterface(@Capturing final SubItf sub) {
        SubItf impl = new SubItf() {
            @Override
            public void base() {
            }
        };
        impl.base();

        new Verifications() {
            {
                sub.base();
            }
        };
    }

    /**
     * The listener interface for receiving myAction events. The class that is interested in processing a myAction event
     * implements this interface, and the object created with that class is registered with a component using the
     * component's <code>addMyActionListener<code> method. When the myAction event occurs, that object's appropriate
     * method is invoked.
     */
    static final class MyActionListener implements ActionListener {
        @Override
        public void processAction(ActionEvent event) {
        }

        /**
         * Do something.
         *
         * @return true, if successful
         */
        boolean doSomething() {
            return true;
        }
    }

    /**
     * Capture user defined class implementing external API.
     *
     * @param actionListener
     *            the action listener
     */
    @Test
    void captureUserDefinedClassImplementingExternalAPI(@Capturing ActionListener actionListener) {
        boolean notCaptured = new MyActionListener().doSomething();
        assertFalse(notCaptured);
    }

    /**
     * Capture library class implementing interface from another library.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureLibraryClassImplementingInterfaceFromAnotherLibrary(@Capturing final ServletContextListener mock) {
        // noinspection UnnecessaryFullyQualifiedName
        ServletContextListener contextListener = new org.springframework.web.util.WebAppRootListener();
        contextListener.contextInitialized(null);

        new Verifications() {
            {
                mock.contextInitialized(null);
            }
        };
    }

    /**
     * The Class BaseGenericReturnTypes.
     */
    static class BaseGenericReturnTypes {

        /**
         * Method one.
         *
         * @return the class
         */
        Class<?> methodOne() {
            return null;
        }

        /**
         * Method two.
         *
         * @return the class
         */
        Class<?> methodTwo() {
            return null;
        }
    }

    /**
     * The Class SubGenericReturnTypes.
     */
    static class SubGenericReturnTypes extends BaseGenericReturnTypes {
    }

    /**
     * Capture method with generic return types.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureMethodWithGenericReturnTypes(@Capturing final BaseGenericReturnTypes mock) {
        new Expectations() {
            {
                mock.methodOne();
                result = BaseGenericReturnTypes.class;
                times = 1;

                mock.methodTwo();
                result = SubGenericReturnTypes.class;
                times = 1;
            }
        };
        SubGenericReturnTypes subBaseGenericReturnTypes = new SubGenericReturnTypes();
        assertEquals(BaseGenericReturnTypes.class, subBaseGenericReturnTypes.methodOne());
        assertEquals(SubGenericReturnTypes.class, subBaseGenericReturnTypes.methodTwo());
    }

    /**
     * The Class BaseR.
     */
    static class BaseR {

        /**
         * Foo.
         */
        void foo() {
        }

        /**
         * Bar.
         */
        void bar() {
        }
    }

    /**
     * The Class SubR.
     */
    static class SubR extends BaseR {
    }

    /**
     * Capture R.
     *
     * @param mock
     *            the mock
     */
    @Test
    void captureR(@Capturing final BaseR mock) {
        new Expectations() {
            {
                mock.foo();
                times = 1;

                mock.bar();
                times = 1;
            }
        };
        SubR subR = new SubR();
        subR.foo();
        subR.bar();
    }

}
