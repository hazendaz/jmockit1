/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection.full;

import static java.util.Collections.enumeration;

import edu.umd.cs.findbugs.annotations.NonNull;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpSession;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mockit.internal.injection.InjectionPoint;
import mockit.internal.injection.InjectionState;

/**
 * Detects and resolves dependencies belonging to the <code>jakarta.servlet</code> API, namely
 * <code>ServletContext</code> and <code>HttpSession</code>.
 */
final class ServletJakartaDependencies {
    // Use a single SecureRandom instance for all sessions
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    static boolean isApplicable(@NonNull Class<?> dependencyType) {
        return dependencyType == HttpSession.class || dependencyType == ServletContext.class;
    }

    @NonNull
    private final InjectionState injectionState;

    ServletJakartaDependencies(@NonNull InjectionState injectionState) {
        this.injectionState = injectionState;
    }

    @NonNull
    Object createAndRegisterDependency(@NonNull Class<?> dependencyType) {
        if (dependencyType == ServletContext.class) {
            return createAndRegisterServletContext();
        }

        return createAndRegisterHttpSession();
    }

    @NonNull
    private ServletContext createAndRegisterServletContext() {
        ServletContext context = new ServletContext() {
            private final Map<String, String> init = new HashMap<>();
            private final Map<String, Object> attrs = new HashMap<>();

            @Override
            public String getContextPath() {
                return "";
            }

            @Override
            public ServletContext getContext(String uriPath) {
                return null;
            }

            @Override
            public int getMajorVersion() {
                return 3;
            }

            @Override
            public int getMinorVersion() {
                return 0;
            }

            @Override
            public int getEffectiveMajorVersion() {
                return 3;
            }

            @Override
            public int getEffectiveMinorVersion() {
                return 0;
            }

            @Override
            public String getMimeType(String file) {
                return null;
            }

            @Override
            public String getRealPath(String path) {
                return null;
            }

            @Override
            public Set<String> getResourcePaths(String path) {
                return null;
            }

            @Override
            public URL getResource(String path) {
                return getClass().getResource(path);
            }

            @Override
            public InputStream getResourceAsStream(String path) {
                return getClass().getResourceAsStream(path);
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return null;
            }

            @Override
            public RequestDispatcher getNamedDispatcher(String name) {
                return null;
            }

            @Override
            public String getServletContextName() {
                return null;
            }

            @Override
            public String getServerInfo() {
                return "JMockit 1.x";
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }

            @Override
            public void log(String msg) {
            }

            @Override
            public void log(String message, Throwable throwable) {
            }

            // Context initialization parameters.
            @Override
            public Enumeration<String> getInitParameterNames() {
                return enumeration(init.keySet());
            }

            @Override
            public String getInitParameter(String name) {
                return init.get(name);
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return init.put(name, value) == null;
            }

            // Context attributes.
            @Override
            public Enumeration<String> getAttributeNames() {
                return enumeration(attrs.keySet());
            }

            @Override
            public Object getAttribute(String name) {
                return attrs.get(name);
            }

            @Override
            public void setAttribute(String name, Object value) {
                attrs.put(name, value);
            }

            @Override
            public void removeAttribute(String name) {
                attrs.remove(name);
            }

            // Un-implemented methods, which may get a non-empty implementation eventually.
            @Override
            public ServletRegistration.Dynamic addServlet(String name, String className) {
                return null;
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String name, Servlet servlet) {
                return null;
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String nm, Class<? extends Servlet> c) {
                return null;
            }

            @Override
            public <T extends Servlet> T createServlet(Class<T> clazz) {
                return null;
            }

            @Override
            public ServletRegistration getServletRegistration(String servletName) {
                return null;
            }

            @Override
            public Map<String, ? extends ServletRegistration> getServletRegistrations() {
                return null;
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String name, String className) {
                return null;
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String name, Filter filter) {
                return null;
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String name, Class<? extends Filter> cl) {
                return null;
            }

            @Override
            public <T extends Filter> T createFilter(Class<T> clazz) {
                return null;
            }

            @Override
            public FilterRegistration getFilterRegistration(String filterName) {
                return null;
            }

            @Override
            public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
                return null;
            }

            @Override
            public SessionCookieConfig getSessionCookieConfig() {
                return null;
            }

            @Override
            public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            }

            @Override
            public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
                return null;
            }

            @Override
            public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
                return null;
            }

            @Override
            public void addListener(String className) {
            }

            @Override
            public <T extends EventListener> void addListener(T t) {
            }

            @Override
            public void addListener(Class<? extends EventListener> listenerClass) {
            }

            @Override
            public <T extends EventListener> T createListener(Class<T> clazz) {
                return null;
            }

            @Override
            public JspConfigDescriptor getJspConfigDescriptor() {
                return null;
            }

            @Override
            public void declareRoles(String... roleNames) {
            }

            @Override
            public String getVirtualServerName() {
                return null;
            }

            // Allow older servlet still (no overrides)
            @Override
            public Dynamic addJspFile(String servletName, String jspFile) {
                return null;
            }

            @Override
            public int getSessionTimeout() {
                return 0;
            }

            @Override
            public void setSessionTimeout(int sessionTimeout) {
            }

            @Override
            public String getRequestCharacterEncoding() {
                return null;
            }

            @Override
            public void setRequestCharacterEncoding(String encoding) {
            }

            @Override
            public String getResponseCharacterEncoding() {
                return null;
            }

            @Override
            public void setResponseCharacterEncoding(String encoding) {
            }
        };

        InjectionPoint injectionPoint = new InjectionPoint(ServletContext.class);
        InjectionState.saveGlobalDependency(injectionPoint, context);
        return context;
    }

    @NonNull
    private HttpSession createAndRegisterHttpSession() {
        HttpSession session = new HttpSession() {
            // Generate a secure random session ID (32 hex chars)
            private final String id = new BigInteger(130, SECURE_RANDOM).toString(32);
            private final long creationTime = System.currentTimeMillis();
            private final Map<String, Object> attrs = new HashMap<>();
            private int maxInactiveInterval;
            private boolean invalidated;

            @Override
            public String getId() {
                return id;
            }

            @Override
            public int getMaxInactiveInterval() {
                return maxInactiveInterval;
            }

            @Override
            public void setMaxInactiveInterval(int interval) {
                maxInactiveInterval = interval;
            }

            @Override
            public long getCreationTime() {
                checkValid();
                return creationTime;
            }

            @Override
            public long getLastAccessedTime() {
                checkValid();
                return creationTime;
            }

            @Override
            public boolean isNew() {
                checkValid();
                return false;
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                checkValid();
                return enumeration(attrs.keySet());
            }

            @Override
            public Object getAttribute(String name) {
                checkValid();
                return attrs.get(name);
            }

            @Override
            public void setAttribute(String name, Object value) {
                checkValid();
                attrs.put(name, value);
            }

            @Override
            public void removeAttribute(String name) {
                checkValid();
                attrs.remove(name);
            }

            @Override
            public void invalidate() {
                checkValid();
                attrs.clear();
                invalidated = true;
            }

            private void checkValid() {
                if (invalidated) {
                    throw new IllegalStateException("Session is invalid");
                }
            }

            @Override
            public ServletContext getServletContext() {
                ServletContext context = InjectionState.getGlobalDependency(new InjectionPoint(ServletContext.class));

                if (context == null) {
                    context = createAndRegisterServletContext();
                }

                return context;
            }

        };

        InjectionPoint injectionPoint = new InjectionPoint(HttpSession.class);
        injectionState.saveInstantiatedDependency(injectionPoint, session);
        return session;
    }
}
