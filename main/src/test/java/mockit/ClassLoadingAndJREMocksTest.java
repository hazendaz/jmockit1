/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;
import java.nio.file.Path;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class ClassLoadingAndJREMocksTest.
 */
@ExtendWith(JMockitExtension.class)
class ClassLoadingAndJREMocksTest {

    /**
     * The Class Foo.
     */
    static class Foo {
    }

    /**
     * Fake file.
     */
    @Test
    void fakeFile() {
        new MockUp<File>() {
            @Mock
            void $init(String name) {
            } // not necessary, except to verify non-occurrence of NPE

            @Mock
            boolean exists() {
                return true;
            }
        };

        new Foo(); // causes a class load
        assertTrue(Path.of("filePath").toFile().exists());
    }

    /**
     * Fake file safely using reentrant fake method.
     */
    @Test
    void fakeFileSafelyUsingReentrantFakeMethod() {
        new MockUp<File>() {
            @Mock
            boolean exists(Invocation inv) {
                File it = inv.getInvokedInstance();
                return "testFile".equals(it.getName()) || inv.<Boolean> proceed();
            }
        };

        checkForTheExistenceOfSeveralFiles();
    }

    /**
     * Check for the existence of several files.
     */
    void checkForTheExistenceOfSeveralFiles() {
        assertFalse(Path.of("someOtherFile").toFile().exists());
        assertTrue(Path.of("testFile").toFile().exists());
        assertFalse(Path.of("yet/another/file").toFile().exists());
        assertTrue(Path.of("testFile").toFile().exists());
    }

    /**
     * Fake file safely using proceed.
     */
    @Test
    void fakeFileSafelyUsingProceed() {
        new MockUp<File>() {
            @Mock
            boolean exists(Invocation inv) {
                File it = inv.getInvokedInstance();
                return "testFile".equals(it.getName()) || inv.<Boolean> proceed();
            }
        };

        checkForTheExistenceOfSeveralFiles();
    }

    /**
     * Attempt to mock non mockable JRE class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void attemptToMockNonMockableJREClass(@Mocked Integer mock) {
        assertNull(mock);
    }

    /**
     * Mock URL and URL connection.
     *
     * @param mockUrl
     *            the mock url
     * @param mockConnection
     *            the mock connection
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockURLAndURLConnection(@Mocked URL mockUrl, @Mocked URLConnection mockConnection) throws Exception {
        URLConnection conn = mockUrl.openConnection();

        assertSame(mockConnection, conn);
    }

    /**
     * Mock URL and http URL connection.
     *
     * @param mockUrl
     *            the mock url
     * @param mockConnection
     *            the mock connection
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockURLAndHttpURLConnection(@Mocked URL mockUrl, @Mocked HttpURLConnection mockConnection) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) mockUrl.openConnection();
        assertSame(mockConnection, conn);
    }

    /**
     * Mock URL and http URL connection with dynamic mock.
     *
     * @param mockHttpConnection
     *            the mock http connection
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockURLAndHttpURLConnectionWithDynamicMock(@Mocked final HttpURLConnection mockHttpConnection)
            throws Exception {
        final URL url = new URL("http://nowhere");

        new Expectations(url) {
            {
                url.openConnection();
                result = mockHttpConnection;
                mockHttpConnection.getOutputStream();
                result = new ByteArrayOutputStream();
            }
        };

        // Code under test:
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        OutputStream out = conn.getOutputStream();

        assertNotNull(out);

        new Verifications() {
            {
                mockHttpConnection.setDoOutput(true);
                mockHttpConnection.setRequestMethod("PUT");
            }
        };
    }

    /**
     * Read resource content.
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String readResourceContent() throws IOException {
        URL url = new URL("http://remoteHost/aResource");
        URLConnection connection = url.openConnection();

        connection.setConnectTimeout(1000);
        connection.connect();

        return connection.getContent().toString();
    }

    /**
     * Cascading mocked URL with injectable cascaded URL connection.
     *
     * @param anyUrl
     *            the any url
     * @param cascadedUrlConnection
     *            the cascaded url connection
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cascadingMockedURLWithInjectableCascadedURLConnection(@Mocked URL anyUrl,
            @Injectable final URLConnection cascadedUrlConnection) throws Exception {
        String testContent = recordURLConnectionToReturnContent(cascadedUrlConnection);

        String content = readResourceContent();

        assertEquals(testContent, content);
    }

    /**
     * Record URL connection to return content.
     *
     * @param urlConnection
     *            the url connection
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String recordURLConnectionToReturnContent(final URLConnection urlConnection) throws IOException {
        final String testContent = "testing";
        new Expectations() {
            {
                urlConnection.getContent();
                result = testContent;
            }
        };
        return testContent;
    }

    /**
     * Fake URL using injectable URL connection.
     *
     * @param urlConnection
     *            the url connection
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void fakeURLUsingInjectableURLConnection(@Injectable final URLConnection urlConnection) throws Exception {
        final String testContent = recordURLConnectionToReturnContent(urlConnection);
        new MockUp<URL>() {
            @Mock
            void $init(URL context, String spec, URLStreamHandler handler) {
            }

            @Mock
            URLConnection openConnection() {
                return urlConnection;
            }
        };

        String content = readResourceContent();

        assertEquals(testContent, content);
    }

    /**
     * Attempt to mock JRE class that is never mockable.
     *
     * @param mockClass
     *            the mock class
     */
    @Test
    @ExpectedException(IllegalArgumentException.class)
    public void attemptToMockJREClassThatIsNeverMockable(@Mocked Class<?> mockClass) {
        fail("Should never get here");
    }

    // The following tests exercise mocking of java.net.InetAddress, which is a sealed class as of JDK 17
    // (it permits Inet4Address and Inet6Address). Mocking a sealed class requires JMockit to redefine it
    // while preserving its PermittedSubclasses attribute; dropping that attribute makes the JVM reject the
    // redefinition ("attempted to change the class ... PermittedSubclasses attribute"). These end-to-end
    // tests confirm that a sealed JRE class is fully mockable: instance-method stubbing, static-method
    // stubbing, and recorded exceptions all behave as they did before sealed classes existed.

    /**
     * Mocks an instance method of the sealed {@link InetAddress} class.
     *
     * @param mockAddress
     *            the mock address
     */
    @Test
    void mockInstanceMethodOfSealedInetAddress(@Mocked InetAddress mockAddress) {
        new Expectations() {
            {
                mockAddress.getHostName();
                result = "mocked-host";
                mockAddress.getHostAddress();
                result = "10.0.0.5";
            }
        };

        assertEquals("mocked-host", mockAddress.getHostName());
        assertEquals("10.0.0.5", mockAddress.getHostAddress());
    }

    /**
     * Mocks the static {@link InetAddress#getByName(String)} factory and stubs the resulting instance.
     *
     * @param anyAddress
     *            any mocked address instance
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockStaticFactoryOfSealedInetAddress(@Mocked InetAddress anyAddress) throws Exception {
        new Expectations() {
            {
                InetAddress.getByName("db.example.com");
                result = anyAddress;
                anyAddress.getHostAddress();
                result = "192.0.2.7";
            }
        };

        InetAddress resolved = InetAddress.getByName("db.example.com");
        assertSame(anyAddress, resolved);
        assertEquals("192.0.2.7", resolved.getHostAddress());
    }

    /**
     * Mocks the static {@link InetAddress#getLocalHost()} factory, a common seam for tests that must not depend on the
     * host's real name resolution.
     *
     * @param anyAddress
     *            any mocked address instance
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockLocalHostOfSealedInetAddress(@Mocked InetAddress anyAddress) throws Exception {
        new Expectations() {
            {
                InetAddress.getLocalHost();
                result = anyAddress;
                anyAddress.getHostName();
                result = "myhost.local";
            }
        };

        assertEquals("myhost.local", InetAddress.getLocalHost().getHostName());
    }

    /**
     * Records a checked {@link UnknownHostException} from the mocked static factory, confirming exception recording
     * works for a sealed class.
     *
     * @param anyAddress
     *            any mocked address instance
     */
    @Test
    void recordCheckedExceptionFromSealedInetAddress(@Mocked InetAddress anyAddress) {
        new Expectations() {
            {
                try {
                    InetAddress.getByName(anyString);
                    result = new UnknownHostException("unresolved");
                } catch (UnknownHostException ignore) {
                    // The recorded call declares a checked exception; recording it cannot actually throw.
                }
            }
        };

        assertThrows(UnknownHostException.class, () -> InetAddress.getByName("no.such.host"));
    }
}
