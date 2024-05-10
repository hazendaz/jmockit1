package otherTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mockit.Deencapsulation;

import org.junit.jupiter.api.Test;

final class DeencapsulationTest {

    static final class Subclass extends BaseClass {
        final int INITIAL_VALUE = new Random().nextInt();
        final int initialValue = -1;

        @SuppressWarnings("unused")
        private static final Integer constantField = 123;

        private static StringBuilder buffer;
        @SuppressWarnings("unused")
        private static char static1;
        @SuppressWarnings("unused")
        private static char static2;

        static StringBuilder getBuffer() {
            return buffer;
        }

        static void setBuffer(StringBuilder buffer) {
            Subclass.buffer = buffer;
        }

        private String stringField;
        private int intField;
        private int intField2;
        private List<String> listField;

        int getIntField() {
            return intField;
        }

        void setIntField(int intField) {
            this.intField = intField;
        }

        int getIntField2() {
            return intField2;
        }

        void setIntField2(int intField2) {
            this.intField2 = intField2;
        }

        String getStringField() {
            return stringField;
        }

        void setStringField(String stringField) {
            this.stringField = stringField;
        }

        List<String> getListField() {
            return listField;
        }

        void setListField(List<String> listField) {
            this.listField = listField;
        }
    }

    final Subclass anInstance = new Subclass();

    @Test
    void getInstanceFieldByName() {
        anInstance.setIntField(3);
        anInstance.setStringField("test");
        anInstance.setListField(Collections.<String>emptyList());

        Integer intValue = Deencapsulation.getField(anInstance, "intField");
        String stringValue = Deencapsulation.getField(anInstance, "stringField");
        List<String> listValue = Deencapsulation.getField(anInstance, "listField");

        assertEquals(anInstance.getIntField(), intValue.intValue());
        assertEquals(anInstance.getStringField(), stringValue);
        assertSame(anInstance.getListField(), listValue);
    }

    @Test
    public void attemptToGetInstanceFieldByNameWithWrongName() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.getField(anInstance, "noField");
        });
        assertEquals("No instance field of name \"noField\" found in class otherTests.BaseClass",
                throwable.getMessage());
    }

    @Test
    void getInheritedInstanceFieldByName() {
        anInstance.baseInt = 3;
        anInstance.baseString = "test";
        anInstance.baseSet = Collections.emptySet();

        Integer intValue = Deencapsulation.getField(anInstance, "baseInt");
        String stringValue = Deencapsulation.getField(anInstance, "baseString");
        Set<Boolean> listValue = Deencapsulation.getField(anInstance, "baseSet");

        assertEquals(anInstance.baseInt, intValue.intValue());
        assertEquals(anInstance.baseString, stringValue);
        assertSame(anInstance.baseSet, listValue);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getInstanceFieldByType() {
        anInstance.setStringField("by type");
        anInstance.setListField(new ArrayList<String>());

        String stringValue = Deencapsulation.getField(anInstance, String.class);
        List<String> listValue = Deencapsulation.getField(anInstance, List.class);
        List<String> listValue2 = Deencapsulation.getField(anInstance, ArrayList.class);

        assertEquals(anInstance.getStringField(), stringValue);
        assertSame(anInstance.getListField(), listValue);
        assertSame(listValue, listValue2);
    }

    @Test
    public void attemptToGetInstanceFieldByTypeWithWrongType() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.getField(anInstance, Byte.class);
        });
        assertEquals("Instance field of type byte or Byte not found in class otherTests.BaseClass",
                throwable.getMessage());
    }

    @Test
    public void attemptToGetInstanceFieldByTypeForClassWithMultipleFieldsOfThatType() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.getField(anInstance, int.class);
        });
        assertEquals("More than one instance field from which a value of type int "
                + "can be read exists in class otherTests.DeencapsulationTest$Subclass: "
                + "INITIAL_VALUE, initialValue", throwable.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getInheritedInstanceFieldByType() {
        Set<Boolean> fieldValueOnInstance = new HashSet<>();
        anInstance.baseSet = fieldValueOnInstance;

        Set<Boolean> setValue = Deencapsulation.getField(anInstance, fieldValueOnInstance.getClass());
        Set<Boolean> setValue2 = Deencapsulation.getField(anInstance, HashSet.class);

        assertSame(fieldValueOnInstance, setValue);
        assertSame(setValue, setValue2);
    }

    @Test
    void getInstanceFieldOnBaseClassByType() {
        anInstance.setLongField(15);

        long longValue = Deencapsulation.getField(anInstance, long.class);

        assertEquals(15, longValue);
    }

    @Test
    void getStaticFieldByName() {
        Subclass.setBuffer(new StringBuilder());

        StringBuilder b = Deencapsulation.getField(Subclass.class, "buffer");

        assertSame(Subclass.getBuffer(), b);
    }

    @Test
    public void attemptToGetStaticFieldByNameFromWrongClass() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.getField(BaseClass.class, "buffer");
        });
        assertEquals("No static field of name \"buffer\" found in class otherTests.BaseClass", throwable.getMessage());
    }

    @Test
    void getStaticFieldByType() {
        Subclass.setBuffer(new StringBuilder());

        StringBuilder b = Deencapsulation.getField(Subclass.class, StringBuilder.class);

        assertSame(Subclass.getBuffer(), b);
    }

    @Test
    void setInstanceFieldByName() {
        anInstance.setIntField2(1);

        Deencapsulation.setField(anInstance, "intField2", 901);

        assertEquals(901, anInstance.getIntField2());
    }

    @Test
    public void attemptToSetInstanceFieldByNameWithWrongName() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(anInstance, "noField", 901);
        });
        assertEquals("No instance field of name \"noField\" found in class otherTests.BaseClass",
                throwable.getMessage());
    }

    @Test
    void setInstanceFieldByType() {
        anInstance.setStringField("");

        Deencapsulation.setField(anInstance, "Test");

        assertEquals("Test", anInstance.getStringField());
    }

    @Test
    public void attemptToSetInstanceFieldByTypeWithWrongType() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(anInstance, (byte) 123);
        });
        assertEquals("Instance field of type byte or Byte not found in class otherTests.BaseClass",
                throwable.getMessage());
    }

    @Test
    public void attemptToSetInstanceFieldByTypeForClassWithMultipleFieldsOfThatType() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(anInstance, 901);
        });
        assertEquals(
                "More than one instance field to which a value of type int "
                        + "or Integer can be assigned exists in class "
                        + "otherTests.DeencapsulationTest$Subclass: INITIAL_VALUE, initialValue",
                throwable.getMessage());
    }

    @Test
    void setStaticFieldByName() {
        Subclass.setBuffer(null);

        Deencapsulation.setField(Subclass.class, "buffer", new StringBuilder());

        assertNotNull(Subclass.getBuffer());
    }

    @Test
    public void attemptToSetStaticFieldByNameWithWrongName() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(Subclass.class, "noField", null);
        });
        assertEquals("No static field of name \"noField\" found in class otherTests.BaseClass", throwable.getMessage());
    }

    @Test
    void setStaticFieldByType() {
        Subclass.setBuffer(null);

        Deencapsulation.setField(Subclass.class, new StringBuilder());

        assertNotNull(Subclass.getBuffer());
    }

    @Test
    public void attemptToSetFieldByTypeWithoutAValue() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(Subclass.class, null);
        });
        assertEquals("Missing field value when setting field by type", throwable.getMessage());
    }

    @Test
    public void attemptToSetStaticFieldByTypeWithWrongType() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(Subclass.class, new String());
        });
        assertEquals("Static field of type String not found in class otherTests.BaseClass", throwable.getMessage());
    }

    @Test
    public void attemptToSetStaticFieldByTypeForClassWithMultipleFieldsOfThatType() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            Deencapsulation.setField(Subclass.class, 'A');
        });
        assertEquals("More than one static field to which a value of type char or "
                + "Character can be assigned exists in class "
                + "otherTests.DeencapsulationTest$Subclass: static1, static2", throwable.getMessage());
    }

    @Test
    void setFinalInstanceFields() {
        Subclass obj = new Subclass();

        Deencapsulation.setField(obj, "INITIAL_VALUE", 123);
        Deencapsulation.setField(obj, "initialValue", 123);

        assertEquals(123, obj.INITIAL_VALUE);
        assertEquals(123, (int) Deencapsulation.getField(obj, "initialValue"));
        assertEquals(-1, obj.initialValue); // in this case, the compile-time constant gets embedded in client code
    }

    @Test
    public void attemptToSetAStaticFinalField() {
        Throwable throwable = assertThrows(RuntimeException.class, () -> {
            Deencapsulation.setField(Subclass.class, "constantField", 54);
        });
        assertEquals(
                "java.lang.IllegalAccessException: Can not set static final java.lang.Integer "
                        + "field otherTests.DeencapsulationTest$Subclass.constantField to java.lang.Integer",
                throwable.getMessage());
    }
}
