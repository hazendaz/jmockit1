/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.faking;

import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.MockUp;
import mockit.asm.classes.ClassReader;
import mockit.internal.BaseClassModifier;
import mockit.internal.ClassFile;
import mockit.internal.startup.Startup;
import mockit.internal.state.CachedClassfiles;
import mockit.internal.state.TestRun;

public final class FakeClassSetup {
    @Nonnull
    final Class<?> realClass;
    @Nullable
    private ClassReader rcReader;
    @Nonnull
    private final FakeMethods fakeMethods;
    @Nonnull
    final MockUp<?> fake;
    private final boolean forStartupFake;

    public FakeClassSetup(@Nonnull Class<?> realClass, @Nonnull Class<?> classToFake, @Nullable Type fakedType,
            @Nonnull MockUp<?> fake) {
        this(realClass, classToFake, fakedType, fake, null);
    }

    FakeClassSetup(@Nonnull Class<?> realClass, @Nullable Type fakedType, @Nonnull MockUp<?> fake,
            @Nullable byte[] realClassCode) {
        this(realClass, realClass, fakedType, fake, realClassCode);
    }

    FakeClassSetup(@Nonnull Class<?> realClass, @Nonnull Class<?> classToFake, @Nullable Type fakedType,
            @Nonnull MockUp<?> fake, @Nullable byte[] realClassCode) {
        this.realClass = classToFake;
        this.fake = fake;
        forStartupFake = Startup.initializing;
        rcReader = realClassCode == null ? null : new ClassReader(realClassCode);
        fakeMethods = new FakeMethods(realClass, fakedType);
        collectFakeMethods();
        registerFakeClassAndItsStates();
    }

    private void collectFakeMethods() {
        Class<?> fakeClass = fake.getClass();
        new FakeMethodCollector(fakeMethods).collectFakeMethods(fakeClass);
    }

    private void registerFakeClassAndItsStates() {
        fakeMethods.registerFakeStates(fake, forStartupFake);

        FakeClasses fakeClasses = TestRun.getFakeClasses();

        if (forStartupFake) {
            fakeClasses.addFake(fakeMethods.getFakeClassInternalName(), fake);
        } else {
            fakeClasses.addFake(fake);
        }
    }

    void redefineMethodsInGeneratedClass() {
        byte[] modifiedClassFile = modifyRealClass(realClass);

        if (modifiedClassFile != null) {
            applyClassModifications(realClass, modifiedClassFile);
        }
    }

    public void redefineMethods() {
        @Nullable
        Class<?> classToModify = realClass;

        while (classToModify != null && fakeMethods.hasUnusedFakes()) {
            byte[] modifiedClassFile = modifyRealClass(classToModify);

            if (modifiedClassFile != null) {
                applyClassModifications(classToModify, modifiedClassFile);
            }

            Class<?> superClass = classToModify.getSuperclass();
            classToModify = superClass == Object.class || superClass == Proxy.class ? null : superClass;
            rcReader = null;
        }

    }

    @Nullable
    private byte[] modifyRealClass(@Nonnull Class<?> classToModify) {
        if (rcReader == null) {
            rcReader = ClassFile.createReaderFromLastRedefinitionIfAny(classToModify);
        }

        FakedClassModifier modifier = new FakedClassModifier(rcReader, classToModify, fake, fakeMethods);
        rcReader.accept(modifier);

        return modifier.wasModified() ? modifier.toByteArray() : null;
    }

    @Nonnull
    BaseClassModifier createClassModifier(@Nonnull ClassReader cr) {
        return new FakedClassModifier(cr, realClass, fake, fakeMethods);
    }

    void applyClassModifications(@Nonnull Class<?> classToModify, @Nonnull byte[] modifiedClassFile) {
        ClassDefinition classDef = new ClassDefinition(classToModify, modifiedClassFile);
        Startup.redefineMethods(classDef);

        if (forStartupFake) {
            CachedClassfiles.addClassfile(classToModify, modifiedClassFile);
        } else {
            String fakeClassDesc = fakeMethods.getFakeClassInternalName();
            TestRun.mockFixture().addRedefinedClass(fakeClassDesc, classDef);
        }
    }
}
