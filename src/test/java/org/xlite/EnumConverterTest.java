package org.xlite;

public class EnumConverterTest {

    @org.testng.annotations.Test
    public void testEnum() {

        EnumClass ec = new EnumClass();

        System.out.println("enum name: "+ec.one.name());
        System.out.println("enum ordinal: "+ec.one.ordinal());
        System.out.println("enum value: "+Enum.valueOf(SimpleEnum.class,"TWO"));
    }

    public static class EnumClass{
        public SimpleEnum one = SimpleEnum.ONE;
    }

    public static enum SimpleEnum {
        ONE, TWO, THREE
    }

}
