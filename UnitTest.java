package com.xxx.xxx;

import org.junit.Test;

public class UnitTest {
    @Test
    public void test() {
        MyClass myClass = new MyClass();
        System.out.println(myClass.inner.a0);
        System.out.println(myClass.inner.a1);
        System.out.println(myClass.inner.a2);
    }
}

class ClassA {
}

class ClassB {
}

class ClassC {
}

abstract class BaseA<A0, A1, A2> {
    A0 a0 = getClass(0);
    A1 a1 = getClass(1);
    A2 a2 = getClass(2);

    <T> T getClass(int index) {
        try {
            return (T) TypeUtil.getClass(this, BaseA.class, index).newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }
}

abstract class BaseB<B0, B1> extends BaseA<B1, ClassB, B0> {
}

abstract class BaseC<C> {
    Inner inner = new Inner();

    protected BaseC() {
    }

    class Inner extends BaseB<ClassC, C> {
    }
}

class MyClass extends BaseC<ClassA> {
}