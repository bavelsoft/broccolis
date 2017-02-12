package com.bavelsoft.broccolies.util.filter;

import com.bavelsoft.broccolies.util.MoreElementFilters;
import com.google.common.collect.Sets;
import com.google.testing.compile.CompilationRule;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class SkipInheritedMethodsTest {

    public @Rule CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;


    @Before
    public void setup() {
        elements = rule.getElements();
        types = rule.getTypes();
    }

    private Object[][] params() {
        return new Object[][]{

                {false, Object.class, Sets.newHashSet("baseMethod()", "concreteMethod()", "ConcreteClass()", "interfaceMethod()")},
                {true, Object.class, Sets.newHashSet("baseMethod()", "concreteMethod()", "ConcreteClass()", "interfaceMethod()")},

                {false, BaseClass.class, Sets.newHashSet(
                        "interfaceMethod()",
                        "ConcreteClass()",
                        "concreteMethod()",
                        // object's elements
                        "finalize()", "toString()", "getClass()", "notifyAll()", "hashCode()",
                        "wait()", "wait(long,int)", "notify()", "equals(java.lang.Object)", "clone()", "wait(long)")},

                {true, BaseClass.class, Sets.newHashSet("concreteMethod()", "ConcreteClass()")},


                {false, ConcreteClass.class, Sets.newHashSet(
                        "interfaceMethod()",
                        "baseMethod()",
                        // object's elements
                        "finalize()", "toString()", "getClass()", "notifyAll()", "hashCode()",
                        "wait()", "wait(long,int)", "notify()", "equals(java.lang.Object)", "clone()", "wait(long)")},
                {true, ConcreteClass.class, Collections.emptySet()},

                {false, BaseInterface.class, Sets.newHashSet(
                        "baseMethod()",
                        "ConcreteClass()",
                        "concreteMethod()",
                        // object's elements
                        "finalize()", "toString()", "getClass()", "notifyAll()", "hashCode()",
                        "wait()", "wait(long,int)", "notify()", "equals(java.lang.Object)", "clone()", "wait(long)")},

                {true, BaseInterface.class, Sets.newHashSet(
                        "baseMethod()",
                        "ConcreteClass()",
                        "concreteMethod()")},

                {false, SomeClass.class, Sets.newHashSet(
                        "interfaceMethod()",
                        "baseMethod()",
                        "ConcreteClass()",
                        "concreteMethod()",
                        // object's elements
                        "finalize()", "toString()", "getClass()", "notifyAll()", "hashCode()",
                        "wait()", "wait(long,int)", "notify()", "equals(java.lang.Object)", "clone()", "wait(long)")},

                {true, SomeClass.class, Sets.newHashSet(
                        "interfaceMethod()",
                        "baseMethod()",
                        "ConcreteClass()",
                        "concreteMethod()")},

        };
    }


    @Test
    @Parameters(method = "params")
    public void testSkipMethods(boolean checkSuperType, Class enclosingClassToSkip, Set<String> expectedElementsNames) {
        TypeElement concreteClass = elements.getTypeElement(ConcreteClass.class.getCanonicalName());
        List<? extends Element> allMembers = elements.getAllMembers(concreteClass);

        Collection<? extends Element> filtered = MoreElementFilters.filter(allMembers,
                MoreElementFilters.notEnclosedIn(checkSuperType, elements.getTypeElement(enclosingClassToSkip.getCanonicalName())));

        assertEquals(expectedElementsNames.size(), filtered.size());
        Set<String> actualElementsNames = filtered.stream().map(Object::toString).collect(Collectors.toSet());
        assertEquals(expectedElementsNames, actualElementsNames);
    }

    private static class ConcreteClass extends BaseClass {
        void concreteMethod() {}
    }

    private static class BaseClass implements BaseInterface {
        void baseMethod() {}
    }

    private interface BaseInterface {
        default void interfaceMethod() {}
    }

    private static class SomeClass {}

}
