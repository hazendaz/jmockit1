/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.faking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import mockit.internal.util.ClassLoad;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Holds state associated with fake class containing {@linkplain mockit.Mock annotated fakes}.
 */
public final class FakeStates {
    private static final Pattern SPACE = Pattern.compile(" ");

    /**
     * For each fake instance and each <code>@Mock</code> method containing the <code>Invocation</code> parameter, a
     * runtime state will be kept here.
     */
    @NonNull
    private final Map<Object, List<FakeState>> fakesToFakeStates;
    @NonNull
    private final Map<Object, List<FakeState>> startupFakesToFakeStates;
    @NonNull
    private final Set<FakeState> fakeStatesWithExpectations;

    public FakeStates() {
        startupFakesToFakeStates = new IdentityHashMap<>(2);
        fakesToFakeStates = new IdentityHashMap<>(8);
        fakeStatesWithExpectations = new LinkedHashSet<>(8);
    }

    void addStartupFakeAndItsFakeStates(@NonNull Object fake, @NonNull List<FakeState> fakeStates) {
        startupFakesToFakeStates.put(fake, fakeStates);
        registerExpectations(fakeStates);
    }

    void addFakeAndItsFakeStates(@NonNull Object fake, @NonNull List<FakeState> fakeStates) {
        fakesToFakeStates.put(fake, fakeStates);
        registerExpectations(fakeStates);
    }

    public void copyFakeStates(@NonNull Object previousFake, @NonNull Object newFake) {
        List<FakeState> fakeStates = fakesToFakeStates.get(previousFake);

        if (fakeStates != null) {
            List<FakeState> copiedFakeStates = new ArrayList<>(fakeStates.size());

            for (FakeState fakeState : fakeStates) {
                copiedFakeStates.add(new FakeState(fakeState));
            }

            fakesToFakeStates.put(newFake, copiedFakeStates);
            registerExpectations(copiedFakeStates);
        }
    }

    public void removeClassState(@NonNull Class<?> redefinedClass,
            @Nullable String internalNameForOneOrMoreFakeClasses) {
        removeFakeStates(redefinedClass);

        if (internalNameForOneOrMoreFakeClasses != null) {
            if (internalNameForOneOrMoreFakeClasses.indexOf(' ') < 0) {
                removeFakeStates(internalNameForOneOrMoreFakeClasses);
            } else {
                String[] fakeClassesInternalNames = SPACE.split(internalNameForOneOrMoreFakeClasses);

                for (String fakeClassInternalName : fakeClassesInternalNames) {
                    removeFakeStates(fakeClassInternalName);
                }
            }
        }
    }

    private void removeFakeStates(@NonNull Class<?> redefinedClass) {
        Iterator<List<FakeState>> itr = fakesToFakeStates.values().iterator();

        while (itr.hasNext()) {
            List<FakeState> fakeStates = itr.next();
            FakeState fakeState = fakeStates.get(0);

            if (fakeState.getRealClass() == redefinedClass) {
                fakeStates.forEach(fakeStatesWithExpectations::remove);
                fakeStates.clear();
                itr.remove();
            }
        }
    }

    private void removeFakeStates(@NonNull String fakeClassInternalName) {
        Class<?> fakeClass = ClassLoad.loadClass(fakeClassInternalName.replace('/', '.'));
        Iterator<Entry<Object, List<FakeState>>> itr = fakesToFakeStates.entrySet().iterator();

        while (itr.hasNext()) {
            Entry<Object, List<FakeState>> fakeAndFakeStates = itr.next();
            Object fake = fakeAndFakeStates.getKey();

            if (fake.getClass() == fakeClass) {
                fakeAndFakeStates.getValue().forEach(fakeStatesWithExpectations::remove);
                itr.remove();
            }
        }
    }

    public boolean updateFakeState(@NonNull Object fake, @NonNegative int fakeStateIndex) {
        FakeState fakeState = getFakeState(fake, fakeStateIndex);
        return fakeState.update();
    }

    @NonNull
    FakeState getFakeState(@NonNull Object fake, @NonNegative int fakeStateIndex) {
        List<FakeState> fakeStates = startupFakesToFakeStates.get(fake);

        if (fakeStates == null) {
            fakeStates = fakesToFakeStates.get(fake);
        }

        FakeState fakeState = fakeStates.get(fakeStateIndex);
        assert fakeState != null;
        return fakeState;
    }

    public void verifyMissingInvocations() {
        for (FakeState fakeState : fakeStatesWithExpectations) {
            fakeState.verifyMissingInvocations();
        }
    }

    public void resetExpectations() {
        for (FakeState fakeState : fakeStatesWithExpectations) {
            fakeState.reset();
        }
    }

    private void registerExpectations(@NonNull List<FakeState> fakeStates) {
        for (FakeState fakeState : fakeStates) {
            if (fakeState.isWithExpectations()) {
                fakeStatesWithExpectations.add(fakeState);
            }
        }
    }
}
