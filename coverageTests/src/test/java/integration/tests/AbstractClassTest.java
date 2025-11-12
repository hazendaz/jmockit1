/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractClassTest extends CoverageTest {
    AbstractClassWithNoExecutableLines tested;

    @BeforeEach
    void setUp() {
        tested = new AbstractClassWithNoExecutableLines() {
            @Override
            void doSomething(String s, boolean b) {
            }

            @Override
            int returnValue() {
                return 0;
            }
        };
    }

    @Test
    void useAbstractClass() {
        tested.doSomething("test", true);
        tested.returnValue();

        assertEquals(1, fileData.lineCoverageInfo.getExecutableLineCount());
        assertLines(6, 6, 1);
        assertEquals(100, fileData.lineCoverageInfo.getCoveragePercentage());
    }
}
