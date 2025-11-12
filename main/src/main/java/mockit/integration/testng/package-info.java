/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
/**
 * Provides integration with <em>TestNG</em> test runners, for version 6.2 or newer. This integration provides the
 * following benefits to test code:
 * <ol>
 * <li>Expected invocations specified through the Mocking API are automatically verified before the execution of a test
 * is completed.</li>
 * <li>Fake classes applied with the Faking API from inside a method annotated as a <code>@Test</code> or a
 * <code>@BeforeMethod</code> will be discarded right after the execution of the test method or the whole test,
 * respectively.</li>
 * </ol>
 */
package mockit.integration.testng;
