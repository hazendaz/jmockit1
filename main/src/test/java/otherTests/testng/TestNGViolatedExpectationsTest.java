package otherTests.testng;

import static org.testng.Assert.assertThrows;

import java.util.IllegalFormatCodePointException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.Collaborator;

import org.testng.annotations.Test;

// These tests are expected to fail, so they are kept inactive.
@Test(enabled = false)
public final class TestNGViolatedExpectationsTest {
    @Test // fails with a "missing invocation" error
    public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_1(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.doSomething();
            }
        };
    }

    @Test // fails with the exception thrown by tested code
    public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_2(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = new IllegalFormatCodePointException('x');
            }
        };

        mock.doSomething();
    }

    @Test // fails with an "unexpected invocation" error
    public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_3(@Mocked Collaborator mock) {
        new Expectations() {
            {
                new Collaborator();
                maxTimes = 1;
            }
        };

        new Collaborator();
        new Collaborator();
    }

    @Test
    public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_4(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = new IllegalFormatCodePointException('x');
                minTimes = 2;
            }
        };

        // fails with a "missing invocation" error after the exception thrown by tested code
        assertThrows(IllegalFormatCodePointException.class, () -> {
            mock.doSomething();
        });
    }

    @Test
    public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_5(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = new IllegalFormatCodePointException('x');
            }
        };

        // fails with a different exception than expected
        assertThrows(AssertionError.class, () -> {
            mock.doSomething();
        });
    }

    @Test
    public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_6(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                // fails without the expected exception being thrown
                assertThrows(AssertionError.class, () -> {
                    mock.doSomething();
                });
                result = new IllegalFormatCodePointException('x');
            }
        };
    }
}
