package com.bavelsoft.broccolies.util;


import com.google.auto.common.MoreTypes;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class MoreElementFilters {

    private MoreElementFilters() {}

    public static final Predicate<Element> METHOD = element -> element.getKind() == ElementKind.METHOD;
    public static final Predicate<Element> EXECUTABLE = element -> element.getKind() == ElementKind.METHOD ||
            element.getKind() == ElementKind.CONSTRUCTOR;
    public static final Predicate<Element> NOT_STATIC = element -> !element.getModifiers().contains(Modifier.STATIC);
    public static final Predicate<Element> NOT_NATIVE = element -> !element.getModifiers().contains(Modifier.NATIVE);
    public static final Predicate<Element> NOT_FINAL = element -> !element.getModifiers().contains(Modifier.FINAL);

    public static final Predicate<Element> PUBLIC = element -> element.getModifiers().contains(Modifier.PUBLIC);

    public static final Predicate<Element> FIELD = element -> element.getKind().isField();

    public static final Predicate<Element> EXECUTABLE_WITH_ONE_PARAM = EXECUTABLE.and(element -> {
        ExecutableElement executableElement = (ExecutableElement) element;
        return executableElement.getParameters().size() == 1;
    });

    public static final Predicate<Element> SETTER =
            reduceWithAnd(METHOD, EXECUTABLE_WITH_ONE_PARAM, NOT_STATIC, NOT_NATIVE)
            .and(element -> element.getSimpleName().toString().startsWith("set"))
            .and(PUBLIC);

    public static Predicate<Element> notEnclosedIn(final boolean checkSuperType,
                                                   final TypeElement enclosingElementToSkip) {
        return element -> {
            if (element.getEnclosingElement() != null
                    && element.getEnclosingElement().getKind().isClass()
                    || element.getEnclosingElement().getKind().isInterface()) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                return !(checkSuperType
                        ? isAssignableFrom(enclosingElement, enclosingElementToSkip)
                        : MoreTypes.equivalence().equivalent(enclosingElement.asType(), enclosingElementToSkip.asType()));
            }
            return true;
        };
    }

    /**
     * Determines if the class or interface represented by
     * {@code superType} parameter is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * {@code thisType} parameter. It returns {@code true} if so;
     * otherwise it returns {@code false}.
     *
     * @param superType the super type
     * @param thisType  the type element to be checked
     * @return true if thisType extends superType, otherwise - false
     */
    static boolean isAssignableFrom(final TypeElement superType, final TypeElement thisType) {

        if (MoreTypes.equivalence().equivalent(superType.asType(), thisType.asType())
                || (thisType.getKind().isInterface() && thisType.getInterfaces().isEmpty() && equals(superType, Object.class))) {
            return true;
        }

        List<TypeMirror> parentTypes = Stream.concat(
                Stream.of(thisType.getSuperclass()).filter(tm -> tm.getKind() != TypeKind.NONE),
                thisType.getInterfaces().stream()
        ).collect(toList());

        if (parentTypes.isEmpty()) return false;

        boolean match = parentTypes.stream()
                .anyMatch(t -> MoreTypes.equivalence().equivalent(t, superType.asType()));

        return match || parentTypes.stream().map(MoreTypes::asTypeElement).map(te -> isAssignableFrom(superType, te)).anyMatch(m -> m);
    }


    private static boolean equals(TypeElement element, Class<?> cl) {
        return element.getQualifiedName().contentEquals(cl.getCanonicalName());
    }

    private static boolean notEquals(TypeElement element, Class<?> cl) {
        return !equals(element, cl);
    }

    @SafeVarargs
    public static Collection<? extends Element> filter(Collection<? extends Element> elements,
                                                       Predicate<Element>... predicates) {
        return elements.stream()
                .filter(reduceWithAnd(predicates))
                .collect(toList());
    }

    @SafeVarargs
    public static <T> Predicate<T> reduceWithAnd(Predicate<T>... predicates) {
        return Arrays.stream(predicates).reduce((t) -> true, Predicate::and);
    }
}