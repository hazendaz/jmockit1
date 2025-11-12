/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class CascadingParametersTest.
 */
@ExtendWith(JMockitExtension.class)
@SuppressWarnings("ConstantConditions")
@TestMethodOrder(MethodName.class)
class CascadingParametersTest {

    /**
     * The Class Foo.
     */
    static class Foo {

        /**
         * Gets the bar.
         *
         * @return the bar
         */
        Bar getBar() {
            return null;
        }

        /**
         * Global bar.
         *
         * @return the bar
         */
        static Bar globalBar() {
            return null;
        }

        /**
         * Do something.
         *
         * @param s
         *            the s
         */
        void doSomething(String s) {
            throw new RuntimeException(s);
        }

        /**
         * Gets the int value.
         *
         * @return the int value
         */
        int getIntValue() {
            return 1;
        }

        /**
         * Gets the boolean value.
         *
         * @return the boolean value
         */
        Boolean getBooleanValue() {
            return true;
        }

        /**
         * Gets the list.
         *
         * @return the list
         */
        final List<Integer> getList() {
            return null;
        }

        /**
         * Gets the map.
         *
         * @return the map
         */
        HashMap<?, ?> getMap() {
            return null;
        }
    }

    /**
     * The Class Bar.
     */
    static class Bar {

        /**
         * Instantiates a new bar.
         */
        Bar() {
            throw new RuntimeException();
        }

        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return 1;
        }

        /**
         * Gets the baz.
         *
         * @return the baz
         */
        Baz getBaz() {
            return null;
        }

        /**
         * Gets the baz.
         *
         * @param i
         *            the i
         *
         * @return the baz
         */
        Baz getBaz(@SuppressWarnings("unused") int i) {
            return null;
        }

        /**
         * Gets the enum.
         *
         * @return the enum
         */
        AnEnum getEnum() {
            return null;
        }

        /**
         * Static method.
         *
         * @return the string
         */
        static String staticMethod() {
            return "notMocked";
        }
    }

    /**
     * The Class SubBar.
     */
    static final class SubBar extends Bar {
    }

    /**
     * The Interface Baz.
     */
    public interface Baz {

        /**
         * Run it.
         */
        void runIt();

        /**
         * Gets the date.
         *
         * @return the date
         */
        Date getDate();
    }

    /**
     * The Enum AnEnum.
     */
    enum AnEnum {
        /** The First. */
        First,
        /** The Second. */
        Second,
        /** The Third. */
        Third
    }

    /** The cascaded bar 1. */
    static Bar cascadedBar1;

    /** The cascaded bar 2. */
    static Bar cascadedBar2;

    /**
     * Cascade one level during replay.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeOneLevelDuringReplay(@Mocked Foo foo) {
        cascadedBar1 = foo.getBar();
        assertEquals(0, cascadedBar1.doSomething());

        cascadedBar2 = Foo.globalBar();
        assertEquals(0, cascadedBar2.doSomething());

        Bar bar = foo.getBar();
        assertSame(cascadedBar1, bar);

        Bar globalBar = Foo.globalBar();
        assertSame(cascadedBar2, globalBar);
        assertNotSame(bar, globalBar);

        foo.doSomething("test");
        assertEquals(0, foo.getIntValue());
        assertFalse(foo.getBooleanValue());
        assertTrue(foo.getList().isEmpty());

        Map<?, ?> map = foo.getMap();
        assertNull(map);
    }

    /**
     * Verify that previous cascaded instances have been discarded.
     *
     * @param foo
     *            the foo
     */
    @Test
    void verifyThatPreviousCascadedInstancesHaveBeenDiscarded(@Mocked Foo foo) {
        Bar bar = foo.getBar();
        assertNotSame(cascadedBar1, bar);

        Bar globalBar = Foo.globalBar();
        assertNotSame(cascadedBar2, globalBar);
    }

    /**
     * Verify that static methods and constructors are not mocked when cascading.
     *
     * @param foo
     *            the foo
     */
    @Test
    void verifyThatStaticMethodsAndConstructorsAreNotMockedWhenCascading(@Mocked Foo foo) {
        foo.getBar();

        assertEquals("notMocked", Bar.staticMethod());

        try {
            new Bar();
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * Verify that static methods and constructors are mocked when cascaded mock is mocked normally.
     *
     * @param mockFoo
     *            the mock foo
     * @param mockBar
     *            the mock bar
     */
    @Test
    void verifyThatStaticMethodsAndConstructorsAreMockedWhenCascadedMockIsMockedNormally(@Mocked Foo mockFoo,
            @Mocked Bar mockBar) {
        assertSame(mockBar, mockFoo.getBar());
        assertEquals(0, mockBar.doSomething());
        assertNull(Bar.staticMethod());
        new Bar();
    }

    /**
     * Use available mocked instance of subclass as cascaded instance.
     *
     * @param foo
     *            the foo
     * @param bar
     *            the bar
     */
    @Test
    void useAvailableMockedInstanceOfSubclassAsCascadedInstance(@Mocked Foo foo, @Mocked SubBar bar) {
        Bar cascadedBar = foo.getBar();

        assertSame(bar, cascadedBar);
    }

    /**
     * Replace cascaded instance with first one of two injectable instances.
     *
     * @param foo
     *            the foo
     * @param bar1
     *            the bar 1
     * @param bar2
     *            the bar 2
     */
    @Test
    void replaceCascadedInstanceWithFirstOneOfTwoInjectableInstances(@Mocked final Foo foo, @Injectable final Bar bar1,
            @Injectable Bar bar2) {
        new Expectations() {
            {
                foo.getBar();
                result = bar1;
            }
        };

        Bar cascadedBar = foo.getBar();

        assertSame(bar1, cascadedBar);
        assertEquals(0, bar1.doSomething());
        assertEquals(0, bar2.doSomething());
    }

    /**
     * Cascade one level during record.
     *
     * @param mockFoo
     *            the mock foo
     */
    @Test
    void cascadeOneLevelDuringRecord(@Mocked final Foo mockFoo) {
        final List<Integer> list = Arrays.asList(1, 2, 3);

        new Expectations() {
            {
                mockFoo.doSomething(anyString);
                minTimes = 2;
                mockFoo.getBar().doSomething();
                result = 2;
                Foo.globalBar().doSomething();
                result = 3;
                mockFoo.getBooleanValue();
                result = true;
                mockFoo.getIntValue();
                result = -1;
                mockFoo.getList();
                result = list;
            }
        };

        Foo foo = new Foo();
        foo.doSomething("1");
        assertEquals(2, foo.getBar().doSomething());
        foo.doSomething("2");
        assertEquals(3, Foo.globalBar().doSomething());
        assertTrue(foo.getBooleanValue());
        assertEquals(-1, foo.getIntValue());
        assertSame(list, foo.getList());
    }

    /**
     * Cascade one level during verify.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeOneLevelDuringVerify(@Mocked final Foo foo) {
        Bar bar = foo.getBar();
        bar.doSomething();
        bar.doSomething();

        Foo.globalBar().doSomething();

        assertEquals(0, foo.getIntValue());
        assertFalse(foo.getBooleanValue());

        assertTrue(foo.getList().isEmpty());

        new Verifications() {
            {
                foo.getBar().doSomething();
                minTimes = 2;
                Foo.globalBar().doSomething();
                times = 1;
            }
        };

        new VerificationsInOrder() {
            {
                foo.getIntValue();
                foo.getBooleanValue();
            }
        };
    }

    /**
     * Cascade two levels during replay.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeTwoLevelsDuringReplay(@Mocked Foo foo) {
        foo.getBar().getBaz().runIt();
    }

    /**
     * Cascade two levels during record.
     *
     * @param mockFoo
     *            the mock foo
     */
    @Test
    void cascadeTwoLevelsDuringRecord(@Mocked final Foo mockFoo) {
        new Expectations() {
            {
                mockFoo.getBar().doSomething();
                result = 1;
                Foo.globalBar().doSomething();
                result = 2;

                mockFoo.getBar().getBaz().runIt();
                times = 2;
            }
        };

        Foo foo = new Foo();
        assertEquals(1, foo.getBar().doSomething());
        assertEquals(2, Foo.globalBar().doSomething());

        Baz baz = foo.getBar().getBaz();
        baz.runIt();
        baz.runIt();
    }

    /**
     * Cascade one level and verify invocation on last mock only.
     *
     * @param foo
     *            the foo
     * @param bar
     *            the bar
     */
    @Test
    void cascadeOneLevelAndVerifyInvocationOnLastMockOnly(@Mocked Foo foo, @Injectable final Bar bar) {
        Bar fooBar = foo.getBar();
        assertSame(bar, fooBar);
        fooBar.doSomething();

        new Verifications() {
            {
                bar.doSomething();
            }
        };
    }

    /**
     * Cascade two levels with invocation recorded on last mock only.
     *
     * @param foo
     *            the foo
     * @param baz
     *            the baz
     */
    @Test
    void cascadeTwoLevelsWithInvocationRecordedOnLastMockOnly(@Mocked Foo foo, @Mocked final Baz baz) {
        new Expectations() {
            {
                baz.runIt();
                times = 1;
            }
        };

        Baz cascadedBaz = foo.getBar().getBaz();
        cascadedBaz.runIt();
    }

    /**
     * Cascade two levels and verify invocation on last mock only.
     *
     * @param foo
     *            the foo
     * @param baz
     *            the baz
     */
    @Test
    void cascadeTwoLevelsAndVerifyInvocationOnLastMockOnly(@Mocked Foo foo, @Mocked final Baz baz) {
        Baz cascadedBaz = foo.getBar().getBaz();
        assertSame(baz, cascadedBaz);
        cascadedBaz.runIt();

        new Verifications() {
            {
                baz.runIt();
            }
        };
    }

    // Tests using the java.lang.Process and java.lang.ProcessBuilder classes //////////////////////////////////////////

    /**
     * Cascade on JRE classes.
     *
     * @param pb
     *            the pb
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cascadeOnJREClasses(@Mocked final ProcessBuilder pb) throws Exception {
        new Expectations() {
            {
                ProcessBuilder sameBuilder = pb.directory((File) any);
                assertSame(sameBuilder, pb);

                Process process = sameBuilder.start();
                process.getOutputStream().write(5);
                process.exitValue();
                result = 1;
            }
        };

        Process process = new ProcessBuilder("test").directory(Path.of("myDir").toFile()).start();
        process.getOutputStream().write(5);
        process.getOutputStream().flush();
        assertEquals(1, process.exitValue());
    }

    /**
     * Return same mocked instance through cascading even with multiple candidates available.
     *
     * @param pb1
     *            the pb 1
     * @param pb2
     *            the pb 2
     */
    @Test
    void returnSameMockedInstanceThroughCascadingEvenWithMultipleCandidatesAvailable(@Injectable ProcessBuilder pb1,
            @Injectable ProcessBuilder pb2) {
        assertSame(pb1, pb1.command("a"));
        assertSame(pb2, pb2.command("b"));
    }

    /**
     * Creates the OS process to copy temp files.
     *
     * @param pb
     *            the pb
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void createOSProcessToCopyTempFiles(@Mocked final ProcessBuilder pb) throws Exception {
        // Code under test creates a new process to execute an OS-specific command.
        String cmdLine = "copy /Y *.txt D:\\TEMP";
        File wrkDir = Path.of("C:\\TEMP").toFile();
        Process copy = new ProcessBuilder().command(cmdLine).directory(wrkDir).start();
        int exit = copy.waitFor();

        if (exit != 0) {
            throw new RuntimeException("Process execution failed");
        }

        // Verify the desired process was created with the correct command.
        new Verifications() {
            {
                pb.command(withSubstring("copy")).start();
            }
        };
    }

    // Tests using java.net classes ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record and verify expectations on cascaded mocks.
     *
     * @param anySocket
     *            the any socket
     * @param cascadedChannel
     *            the cascaded channel
     * @param inetAddr
     *            the inet addr
     *
     * @throws Exception
     *             the exception
     */
    // TODO JWL 2/18/2024 Test not allowed on jdk21
    @Disabled
    @Test
    void recordAndVerifyExpectationsOnCascadedMocks(@Mocked Socket anySocket,
            @Mocked final SocketChannel cascadedChannel, @Mocked InetSocketAddress inetAddr) throws Exception {
        Socket sk = new Socket();
        SocketChannel ch = sk.getChannel();

        if (!ch.isConnected()) {
            SocketAddress sa = new InetSocketAddress("remoteHost", 123);
            ch.connect(sa);
        }

        InetAddress adr1 = sk.getInetAddress();
        InetAddress adr2 = sk.getLocalAddress();
        assertNotSame(adr1, adr2);
        sk.close();

        new Verifications() {
            {
                cascadedChannel.connect((SocketAddress) withNotNull());
            }
        };
    }

    /**
     * A factory for creating Socket objects.
     */
    static final class SocketFactory {

        /**
         * Creates a new Socket object.
         *
         * @return the socket
         */
        public Socket createSocket() {
            return new Socket();
        }

        /**
         * Creates a new Socket object.
         *
         * @param host
         *            the host
         * @param port
         *            the port
         *
         * @return the socket
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public Socket createSocket(String host, int port) throws IOException {
            return new Socket(host, port);
        }
    }

    /**
     * Cascade one level with argument matchers.
     *
     * @param sf
     *            the sf
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cascadeOneLevelWithArgumentMatchers(@Mocked final SocketFactory sf) throws Exception {
        new Expectations() {
            {
                sf.createSocket(anyString, 80);
                result = null;
            }
        };

        assertNull(sf.createSocket("expected", 80));
        assertNotNull(sf.createSocket("unexpected", 8080));
    }

    /**
     * Record and verify one level deep.
     *
     * @param sf
     *            the sf
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void recordAndVerifyOneLevelDeep(@Mocked final SocketFactory sf) throws Exception {
        final OutputStream out = new ByteArrayOutputStream();

        new Expectations() {
            {
                sf.createSocket().getOutputStream();
                result = out;
            }
        };

        assertSame(out, sf.createSocket().getOutputStream());
    }

    /**
     * Record and verify on two cascading mocks of the same type.
     *
     * @param sf1
     *            the sf 1
     * @param sf2
     *            the sf 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void recordAndVerifyOnTwoCascadingMocksOfTheSameType(@Mocked final SocketFactory sf1,
            @Mocked final SocketFactory sf2) throws Exception {
        final OutputStream out1 = new ByteArrayOutputStream();
        final OutputStream out2 = new ByteArrayOutputStream();

        new Expectations() {
            {
                sf1.createSocket().getOutputStream();
                result = out1;
                sf2.createSocket().getOutputStream();
                result = out2;
            }
        };

        assertSame(out1, sf1.createSocket().getOutputStream());
        assertSame(out2, sf2.createSocket().getOutputStream());

        new VerificationsInOrder() {
            {
                sf1.createSocket().getOutputStream();
                sf2.createSocket().getOutputStream();
            }
        };
    }

    /**
     * Record and verify same invocation on mocks returned from invocations with different arguments.
     *
     * @param sf
     *            the sf
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void recordAndVerifySameInvocationOnMocksReturnedFromInvocationsWithDifferentArguments(
            @Mocked final SocketFactory sf) throws Exception {
        new Expectations() {
            {
                sf.createSocket().getPort();
                result = 1;
                sf.createSocket("first", 80).getPort();
                result = 2;
                sf.createSocket("second", 80).getPort();
                result = 3;
                sf.createSocket(anyString, 81).getPort();
                result = 4;
            }
        };

        assertEquals(1, sf.createSocket().getPort());
        assertEquals(2, sf.createSocket("first", 80).getPort());
        assertEquals(3, sf.createSocket("second", 80).getPort());
        assertEquals(4, sf.createSocket("third", 81).getPort());

        new VerificationsInOrder() {
            {
                sf.createSocket().getPort();
                times = 1;
                sf.createSocket("first", 80).getPort();
                sf.createSocket("second", 80).getPort();
                sf.createSocket(anyString, 81).getPort();
                maxTimes = 1;
                sf.createSocket("fourth", -1);
                times = 0;
            }
        };
    }

    /**
     * Cascade on inherited method.
     *
     * @param sc
     *            the sc
     */
    @Test
    void cascadeOnInheritedMethod(@Mocked SocketChannel sc) {
        assertNotNull(sc.provider());
    }

    /**
     * Record and verify with mixed cascade levels.
     *
     * @param sf
     *            the sf
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void recordAndVerifyWithMixedCascadeLevels(@Mocked final SocketFactory sf) throws Exception {
        new Expectations() {
            {
                sf.createSocket("first", 80).getKeepAlive();
                result = true;
                sf.createSocket("second", anyInt).getChannel().close();
                times = 1;
            }
        };

        sf.createSocket("second", 80).getChannel().close();
        assertTrue(sf.createSocket("first", 80).getKeepAlive());
        sf.createSocket("first", 8080).getChannel().provider().openPipe();

        new Verifications() {
            {
                sf.createSocket("first", 8080).getChannel().provider().openPipe();
            }
        };
    }

    // Cascading other Java SE types ///////////////////////////////////////////////////////////////////////////////////

    /**
     * The Class SomeClass.
     */
    static class SomeClass {
        /**
         * Do something.
         *
         * @return the future
         */
        Future<Foo> doSomething() {
            return null;
        }
    }

    /**
     * Cascade A future.
     *
     * @param mock
     *            the mock
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cascadeAFuture(@Mocked SomeClass mock) throws Exception {
        Future<Foo> f = mock.doSomething();
        Foo foo = f.get();

        assertNotNull(foo);
    }

    // Other tests /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record expectation on cascaded mock.
     *
     * @param foo
     *            the foo
     * @param mockBar
     *            the mock bar
     */
    @Test
    void recordExpectationOnCascadedMock(@Mocked Foo foo, @Mocked final Bar mockBar) {
        new Expectations() {
            {
                mockBar.doSomething();
                times = 1;
                result = 123;
            }
        };

        Bar bar = foo.getBar();
        assertEquals(123, bar.doSomething());
    }

    /**
     * Override two cascaded mocks of the same type.
     *
     * @param foo1
     *            the foo 1
     * @param foo2
     *            the foo 2
     * @param mockBar1
     *            the mock bar 1
     * @param mockBar2
     *            the mock bar 2
     */
    @Test
    void overrideTwoCascadedMocksOfTheSameType(@Mocked final Foo foo1, @Mocked final Foo foo2,
            @Mocked final Bar mockBar1, @Mocked final Bar mockBar2) {
        new Expectations() {
            {
                foo1.getBar();
                result = mockBar1;
                foo2.getBar();
                result = mockBar2;
                mockBar1.doSomething();
                mockBar2.doSomething();
            }
        };

        Bar bar1 = foo1.getBar();
        Bar bar2 = foo2.getBar();
        bar1.doSomething();
        bar2.doSomething();
    }

    /**
     * Override two cascaded mocks of the same type but replay in different order.
     *
     * @param foo1
     *            the foo 1
     * @param foo2
     *            the foo 2
     * @param mockBar1
     *            the mock bar 1
     * @param mockBar2
     *            the mock bar 2
     */
    @Test
    void overrideTwoCascadedMocksOfTheSameTypeButReplayInDifferentOrder(@Mocked final Foo foo1, @Mocked final Foo foo2,
            @Injectable final Bar mockBar1, @Mocked final Bar mockBar2) {
        assertThrows(MissingInvocation.class, () -> {
            new Expectations() {
                {
                    foo1.getBar();
                    result = mockBar1;
                    foo2.getBar();
                    result = mockBar2;
                }
            };

            Bar bar1 = foo1.getBar();
            Bar bar2 = foo2.getBar();
            bar2.doSomething();
            bar1.doSomething();

            new VerificationsInOrder() {
                {
                    mockBar1.doSomething();
                    mockBar2.doSomething();
                }
            };
        });
    }

    /**
     * Cascaded enum.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadedEnum(@Mocked final Foo mock) {
        new Expectations() {
            {
                mock.getBar().getEnum();
                result = AnEnum.Second;
            }
        };

        assertEquals(AnEnum.Second, mock.getBar().getEnum());
    }

    /**
     * Cascaded enum returning consecutive values through result field.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadedEnumReturningConsecutiveValuesThroughResultField(@Mocked final Foo mock) {
        new Expectations() {
            {
                mock.getBar().getEnum();
                result = AnEnum.First;
                result = AnEnum.Second;
                result = AnEnum.Third;
            }
        };

        assertSame(AnEnum.First, mock.getBar().getEnum());
        assertSame(AnEnum.Second, mock.getBar().getEnum());
        assertSame(AnEnum.Third, mock.getBar().getEnum());
    }

    /**
     * Cascaded enum returning consecutive values through returns method.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadedEnumReturningConsecutiveValuesThroughReturnsMethod(@Mocked final Foo mock) {
        new Expectations() {
            {
                mock.getBar().getEnum();
                returns(AnEnum.First, AnEnum.Second, AnEnum.Third);
            }
        };

        assertSame(AnEnum.First, mock.getBar().getEnum());
        assertSame(AnEnum.Second, mock.getBar().getEnum());
        assertSame(AnEnum.Third, mock.getBar().getEnum());
    }

    /**
     * Override last cascaded object with non mocked instance.
     *
     * @param foo
     *            the foo
     */
    @Test
    void overrideLastCascadedObjectWithNonMockedInstance(@Mocked final Foo foo) {
        final Date newDate = new Date(123);
        assertEquals(123, newDate.getTime());

        new Expectations() {
            {
                foo.getBar().getBaz().getDate();
                result = newDate;
            }
        };

        assertSame(newDate, new Foo().getBar().getBaz().getDate());
        assertEquals(123, newDate.getTime());
    }

    /**
     * Return declared mocked instance from multi level cascading.
     *
     * @param mockedDate
     *            the mocked date
     * @param foo
     *            the foo
     */
    @Test
    void returnDeclaredMockedInstanceFromMultiLevelCascading(@Mocked Date mockedDate, @Mocked Foo foo) {
        Date newDate = new Date(123);
        assertEquals(0, newDate.getTime());

        Date cascadedDate = new Foo().getBar().getBaz().getDate();

        assertSame(mockedDate, cascadedDate);
        assertEquals(0, newDate.getTime());
        assertEquals(0, mockedDate.getTime());
    }

    /**
     * Return injectable mock instance from multi level cascading.
     *
     * @param mockDate
     *            the mock date
     * @param foo
     *            the foo
     */
    @Test
    void returnInjectableMockInstanceFromMultiLevelCascading(@Injectable Date mockDate, @Mocked Foo foo) {
        Date newDate = new Date(123);
        assertEquals(123, newDate.getTime());

        Date cascadedDate = new Foo().getBar().getBaz().getDate();

        assertSame(mockDate, cascadedDate);
        assertEquals(123, newDate.getTime());
        assertEquals(0, mockDate.getTime());
    }

    /**
     * The Class Factory.
     */
    static class Factory {
        /**
         * Creates the.
         *
         * @return the factory
         */
        static Factory create() {
            return null;
        }
    }

    /**
     * The Class Client.
     */
    static class Client {
        /**
         * Gets the other client.
         *
         * @return the other client
         */
        OtherClient getOtherClient() {
            return null;
        }
    }

    /**
     * The Class OtherClient.
     */
    static class OtherClient {
        /** The Constant F. */
        static final Factory F = Factory.create();
    }

    /**
     * Cascade during static initialization of cascading class.
     *
     * @param mock1
     *            the mock 1
     * @param mock2
     *            the mock 2
     */
    @Test
    void cascadeDuringStaticInitializationOfCascadingClass(@Mocked Factory mock1, @Mocked Client mock2) {
        assertNotNull(mock2.getOtherClient());
        assertNotNull(OtherClient.F);
    }

    /**
     * The Interface LevelZero.
     */
    public interface LevelZero {
        /**
         * Gets the foo.
         *
         * @return the foo
         */
        Runnable getFoo();
    }

    /**
     * The Interface LevelOne.
     */
    public interface LevelOne extends LevelZero {
    }

    /**
     * The Interface LevelTwo.
     */
    public interface LevelTwo extends LevelOne {
    }

    /**
     * Creates the cascaded mock from method defined two levels up an interface hierarchy.
     *
     * @param mock
     *            the mock
     */
    @Test
    void createCascadedMockFromMethodDefinedTwoLevelsUpAnInterfaceHierarchy(@Mocked LevelTwo mock) {
        assertNotNull(mock.getFoo());
    }

    /**
     * The Class AbstractClass.
     */
    public abstract static class AbstractClass implements LevelZero {
    }

    /**
     * Cascade type returned from interface implemented by abstract class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeTypeReturnedFromInterfaceImplementedByAbstractClass(@Mocked AbstractClass mock) {
        Runnable foo = mock.getFoo();
        assertNotNull(foo);
    }

    /**
     * Produce different cascaded instances of same interface from different invocations.
     *
     * @param bar
     *            the bar
     */
    @Test
    void produceDifferentCascadedInstancesOfSameInterfaceFromDifferentInvocations(@Mocked Bar bar) {
        Baz cascaded1 = bar.getBaz(1);
        Baz cascaded2 = bar.getBaz(2);
        Baz cascaded3 = bar.getBaz(1);

        assertSame(cascaded1, cascaded3);
        assertNotSame(cascaded1, cascaded2);
    }

    /**
     * Cascade from java management API.
     *
     * @param mngmntFactory
     *            the mngmnt factory
     */
    @Test
    void cascadeFromJavaManagementAPI(@Mocked ManagementFactory mngmntFactory) {
        CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();

        assertNotNull(compilation);
        assertNull(compilation.getName());
    }

    /**
     * The Interface AnInterface.
     */
    public interface AnInterface {
        /**
         * Gets the package private class.
         *
         * @return the package private class
         */
        NonPublicTestedClass getPackagePrivateClass();
    }

    /**
     * Cascade from method in public interface returning package private type.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromMethodInPublicInterfaceReturningPackagePrivateType(@Mocked AnInterface mock) {
        NonPublicTestedClass ret = mock.getPackagePrivateClass();

        assertNull(ret);
    }

    /**
     * The Class CustomException.
     */
    public static final class CustomException extends Throwable {

        private static final long serialVersionUID = 1L;
    }

    /**
     * The Class AClass.
     */
    static class AClass {
        /**
         * Gets the exception.
         *
         * @return the exception
         */
        CustomException getException() {
            return new CustomException();
        }
    }

    /**
     * Cascade from method returning A throwable subclass.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromMethodReturningAThrowableSubclass(@Mocked AClass mock) {
        CustomException t = mock.getException();

        assertNull(t);
    }

    /**
     * The Class First.
     */
    static class First {
        /**
         * Gets the second.
         *
         * @param <T>
         *            the generic type
         * @param aClass
         *            the a class
         *
         * @return the second
         */
        <T extends Second> T getSecond(@SuppressWarnings("unused") Class<T> aClass) {
            return null;
        }
    }

    /**
     * The Class Second.
     */
    static class Second {
        /**
         * Gets the something.
         *
         * @return the something
         */
        Runnable getSomething() {
            return null;
        }
    }

    /**
     * Cascade from method returning type provided by class parameter then from cascaded instance.
     *
     * @param first
     *            the first
     */
    @Test
    void cascadeFromMethodReturningTypeProvidedByClassParameterThenFromCascadedInstance(@Mocked First first) {
        Second second = first.getSecond(Second.class);
        Runnable runnable = second.getSomething();

        assertNotNull(runnable);
    }

}
