/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.inject.rebind.binding;

import static com.google.inject.internal.Annotations.getKey;

import com.google.gwt.inject.rebind.reflect.MethodLiteral;
import com.google.gwt.inject.rebind.reflect.NoSourceNameException;
import com.google.gwt.inject.rebind.reflect.ReflectUtil;
import com.google.gwt.inject.rebind.util.GuiceUtil;
import com.google.gwt.inject.rebind.util.InjectorMethod;
import com.google.gwt.inject.rebind.util.MethodCallUtil;
import com.google.gwt.inject.rebind.util.NameGenerator;
import com.google.gwt.inject.rebind.util.Preconditions;
import com.google.gwt.inject.rebind.util.PrettyPrinter;
import com.google.gwt.inject.rebind.util.SourceSnippet;
import com.google.gwt.inject.rebind.util.SourceSnippetBuilder;
import com.google.gwt.inject.rebind.util.SourceSnippets;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Binder producing assisted inject factories.
 * <p>
 * For each method in the factory interface, the binder will determine the
 * implementation type from the return type and the provided bindings. It will
 * then check all constructors in the implementation type against the method
 * parameters (taking named {@literal @}{@link Assisted} annotations into
 * account) and pick a matching one (if available). To inject the selected
 * constructor the binder will write a common method injection, replacing the
 * parameters commonly retrieved through key-specific getter methods with
 * values obtained from the matching method arguments. Finally, after an
 * instance has been constructed, it will be member-injected before it is
 * returned to the caller of the method.
 */
public class FactoryBinding extends AbstractBinding implements Binding {

  /**
   * If a factory method parameter isn't annotated it gets this annotation.
   */
  private static final Assisted DEFAULT_ANNOTATION = new Assisted() {
    public String value() {
      return "";
    }

    public Class<? extends Annotation> annotationType() {
      return Assisted.class;
    }

    @Override public boolean equals(Object o) {
      return o instanceof Assisted
          && ((Assisted) o).value().equals("");
    }

    @Override public int hashCode() {
      return 127 * "value".hashCode() ^ "".hashCode();
    }

    @Override public String toString() {
      return "@" + Assisted.class.getName() + "(value=)";
    }
  };


  private final List<AssistData> assistData = new ArrayList<AssistData>();
  private final Map<Key<?>, TypeLiteral<?>> collector;
  private final Key<?> factoryKey;
  private final TypeLiteral<?> factoryType;
  private final Set<Dependency> dependencies = new LinkedHashSet<Dependency>();

  /**
   * Collection of all implementations produced by this factory, each annotated
   * with @Assisted. This is used to gather all required member-inject methods.
   */
  private final Set<TypeLiteral<?>> implementations = new LinkedHashSet<TypeLiteral<?>>();

  private final GuiceUtil guiceUtil;
  private final MethodCallUtil methodCallUtil;

  FactoryBinding(Map<Key<?>, TypeLiteral<?>> collector, Key<?> factoryKey, Context context,
      GuiceUtil guiceUtil, MethodCallUtil methodCallUtil) {
    super(context, factoryKey);

    this.collector = Preconditions.checkNotNull(collector);
    this.factoryKey = factoryKey;
    this.factoryType = factoryKey.getTypeLiteral();
    this.guiceUtil = guiceUtil;
    this.methodCallUtil = methodCallUtil;

    try {
      matchMethods(Preconditions.checkNotNull(factoryKey));
    } catch (ErrorsException e) {
      e.getErrors().throwConfigurationExceptionIfErrorsExist();
    }
  }

  public SourceSnippet getCreationStatements(NameGenerator nameGenerator,
      List<InjectorMethod> methodsOutput) throws NoSourceNameException {
    String factoryTypeName = ReflectUtil.getSourceName(factoryType);
    SourceSnippetBuilder sb = new SourceSnippetBuilder();

    sb.append(factoryTypeName).append(" result = new ").append(factoryTypeName).append("() {");

    for (AssistData assisted : assistData) {
      // While it might seem that we could just create the return type directly
      // in the factory, that won't work.  The problem is that the return type
      // might have to be created in a different package from the factory: for
      // instance, it might inject a package-private object from its own
      // package.
      //
      // So here's the strategy: we generate a separate injector method that,
      // given the assisted parameters, creates the return value and performs
      // member injection on it, named "assistedCreate_FACTORY_RETURNTYPE".
      // Then we create a factory method that dispatches to that injector method
      // (which, again, may be in some other injector fragment).

      String returnName = ReflectUtil.getSourceName(assisted.implementation);

      String signature = ReflectUtil.signatureBuilder(assisted.method)
          .removeAbstractModifier()
          .build();

      SourceSnippet methodCall = methodCallUtil.createMethodCallWithInjection(
          assisted.constructor, null, assisted.parameterNames, nameGenerator, methodsOutput);

      SourceSnippet assistedCreateCall = callAssistedCreate(assisted, nameGenerator, methodsOutput);

      sb.append("\n\n    ").append(signature).append(" {")
         .append("\n      return ").append(assistedCreateCall).append(";")
         .append("\n    }"); // End method.
    }

    sb.append("\n};"); // End factory implementation.

    return sb.build();
  }

  private SourceSnippet callAssistedCreate(AssistData assisted, NameGenerator nameGenerator,
      List<InjectorMethod> methodsOutput) throws NoSourceNameException {
    String returnTypeName = ReflectUtil.getSourceName(assisted.implementation);
    String packageName = ReflectUtil.getUserPackageName(assisted.implementation);
    String factoryTypeName = ReflectUtil.getSourceName(factoryType);

    String assistedInjectMethodName =
        nameGenerator.getAssistedInjectMethodName(factoryKey, assisted.method.getName());
    String assistedInjectSignature = ReflectUtil.signatureBuilder(assisted.method)
        .withMethodName(assistedInjectMethodName)
        .removeAbstractModifier()
        .build();

    SourceSnippet memberInjectCall =
        SourceSnippets.callMemberInject(assisted.implementation, "result");
    SourceSnippet methodCall = methodCallUtil.createMethodCallWithInjection(
        assisted.constructor, null, assisted.parameterNames, nameGenerator, methodsOutput);

    SourceSnippet assistedInjectMethodBody = new SourceSnippetBuilder()
        .append(returnTypeName).append(" result = ").append(methodCall)
        .append("\n").append(memberInjectCall)
        .append("\nreturn result;")
        .build();

    methodsOutput.add(SourceSnippets.asMethod(false, assistedInjectSignature, packageName,
        assistedInjectMethodBody));

    List<String> parameterNames = new ArrayList<String>();
    for (int i = 0; i < assisted.method.getParameterKeys().size(); ++i) {
      parameterNames.add(ReflectUtil.formatParameterName(i));
    }

    return SourceSnippets.callMethod(assistedInjectMethodName, packageName, parameterNames);
  }

  public Collection<Dependency> getDependencies() {
    return dependencies;
  }

  @Override
  public Collection<TypeLiteral<?>> getMemberInjectRequests() {
    return Collections.unmodifiableCollection(implementations);
  }

  private void matchMethods(Key<?> factoryKey) throws ErrorsException {
    Errors errors = new Errors();
    dependencies.add(new Dependency(Dependency.GINJECTOR, factoryKey, getContext()));
    Class<?> factoryRawType = factoryType.getRawType();

    // getMethods() includes inherited methods from super-interfaces.
    for (Method method : factoryRawType.getMethods()) {
      Key<?> returnType = getKey(factoryType.getReturnType(method), method,
          method.getAnnotations(), errors);

      // Get parameters with annotations.
      List<TypeLiteral<?>> params = factoryType.getParameterTypes(method);
      Annotation[][] paramAnnotations = method.getParameterAnnotations();
      int p = 0;
      List<Key<?>> paramList = new ArrayList<Key<?>>();
      for (TypeLiteral<?> param : params) {
        Key<?> paramKey = getKey(param, method, paramAnnotations[p++], errors);
        paramList.add(assistKey(method, paramKey, errors));
      }

      // Try to match up the method to the constructor.
      TypeLiteral<?> implementation = collector.get(returnType);
      if (implementation == null) {
        implementation = returnType.getTypeLiteral();
      }
      Constructor<?> constructor =
          findMatchingConstructor(method, implementation, paramList, errors);

      if (constructor == null) {
        continue; // Errors are collected and thrown below.
      }

      // Calculate a map from method to constructor parameters and required
      // keys.
      String[] parameterNames = extractConstructorParameters(factoryKey, 
          implementation, constructor, paramList, errors, dependencies);

      TypeLiteral<?> methodDeclaringType = factoryType.getSupertype(method.getDeclaringClass());
      assistData.add(new AssistData(implementation, MethodLiteral.get(constructor, implementation),
          MethodLiteral.get(method, methodDeclaringType), parameterNames));
      implementations.add(implementation);

      dependencies.addAll(guiceUtil.getMemberInjectionDependencies(factoryKey, implementation));
    }

    errors.throwConfigurationExceptionIfErrorsExist();
  }

  /**
   * Matches constructor parameters to method parameters for injection and
   * records remaining parameters as required keys.
   */
  private String[] extractConstructorParameters(Key<?> factoryKey, TypeLiteral<?> implementation,
      Constructor constructor, List<Key<?>> methodParams, Errors errors,
      Set<Dependency> dependencyCollector) throws ErrorsException {

    // Get parameters with annotations.
    List<TypeLiteral<?>> ctorParams = implementation.getParameterTypes(constructor);
    Annotation[][] ctorParamAnnotations = constructor.getParameterAnnotations();

    int p = 0;
    String[] parameterNames = new String[ctorParams.size()];
    Set<Key<?>> keySet = new LinkedHashSet<Key<?>>();
    for (TypeLiteral<?> ctorParam : ctorParams) {
      Key<?> ctorParamKey = getKey(ctorParam, constructor, ctorParamAnnotations[p], errors);

      if (ctorParamKey.getAnnotationType() == Assisted.class) {
        if (!keySet.add(ctorParamKey)) {
          errors.addMessage(PrettyPrinter.format(
              "%s has more than one parameter of type %s annotated with @Assisted(\"%s\").  " +
              "Please specify a unique value with the annotation to avoid confusion.",
              implementation, ctorParamKey.getTypeLiteral().getType(), 
              ((Assisted) ctorParamKey.getAnnotation()).value()));
        }
        
        int location = methodParams.indexOf(ctorParamKey);

        // This should never happen since the constructor was already checked
        // in #[inject]constructorHasMatchingParams(..).
        Preconditions.checkState(location != -1);

        parameterNames[p] = ReflectUtil.formatParameterName(location);
      } else {
        dependencyCollector.add(new Dependency(factoryKey, ctorParamKey, false, true,
            constructor.toString()));
      }

      p++;
    }

    return parameterNames;
  }

  /**
   * Finds a constructor suitable for the method. If the implementation
   * contained any constructors marked with {@link AssistedInject}, this
   * requires all {@link Assisted} parameters to exactly match the parameters
   * (in any order) listed in the method. Otherwise, if no
   * {@link AssistedInject} constructors exist, this will default to looking
   * for a {@literal @}{@link Inject} constructor.
   */
  private Constructor findMatchingConstructor(Method method, TypeLiteral<?> implementation,
      List<Key<?>> paramList, Errors errors) throws ErrorsException {
    Constructor<?> matchingConstructor = null;
    boolean anyAssistedInjectConstructors = false;

    // Look for AssistedInject constructors...
    for (Constructor<?> constructor : implementation.getRawType().getDeclaredConstructors()) {
      if (constructor.isAnnotationPresent(AssistedInject.class)) {
        anyAssistedInjectConstructors = true;

        if (constructorHasMatchingParams(implementation, constructor, paramList, errors)) {
          if (matchingConstructor != null) {
            errors.addMessage(PrettyPrinter.format(
                "%s has more than one constructor annotated with @AssistedInject "
                    + "that matches the parameters in method %s.",
                implementation, method));
            return null;
          } else {
            matchingConstructor = constructor;
          }
        }
      }
    }

    if (matchingConstructor != null) {
      return matchingConstructor;
    }

    if (anyAssistedInjectConstructors) {
      errors.addMessage(PrettyPrinter.format(
          "%s has @AssistedInject constructors, but none of them match the "
              + "parameters in method %s.", implementation, method));
      return null;
    }

    // Look for @Inject constructors...
    Constructor<?> injectConstructor =
        (Constructor) InjectionPoint.forConstructorOf(implementation).getMember();

    if (injectConstructorHasMatchingParams(implementation, injectConstructor, paramList, errors)) {
      return injectConstructor;
    }

    // No matching constructor exists, complain.
    errors.addMessage(PrettyPrinter.format(
        "%s has no constructors matching the parameters in method %s.",
        implementation, method));
    return null;
  }

  /**
   * Matching logic for {@literal @}{@link AssistedInject} constructor and
   * method parameters.
   *
   * This returns true if and only if all @Assisted parameters in the
   * constructor exactly match (in any order) all @Assisted parameters the
   * method's parameter.
   */
  private boolean constructorHasMatchingParams(TypeLiteral<?> type, Constructor<?> constructor,
      List<Key<?>> paramList, Errors errors) throws ErrorsException {
    List<TypeLiteral<?>> params = type.getParameterTypes(constructor);
    Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
    int p = 0;
    List<Key<?>> constructorKeys = new ArrayList<Key<?>>();
    for (TypeLiteral<?> param : params) {
      constructorKeys.add(getKey(param, constructor, paramAnnotations[p++], errors));
    }

    // Require that every key exist in the constructor to match up exactly.
    for (Key<?> key : paramList) {
      // If it didn't exist in the constructor set, we can't use it.
      if (!constructorKeys.remove(key)) {
        return false;
      }
    }

    // If any keys remain and their annotation is Assisted, we can't use it.
    for (Key<?> key : constructorKeys) {
      if (key.getAnnotationType() == Assisted.class) {
        return false;
      }
    }

    // All @Assisted params match up to the method's parameters.
    return true;
  }

  /**
   * Matching logic for {@literal @}{@link Inject} constructor and method
   * parameters.
   *
   * This returns true if all assisted parameters required by the constructor
   * are provided by the factory method.
   */
  private boolean injectConstructorHasMatchingParams(TypeLiteral<?> type,
      Constructor<?> constructor, List<Key<?>> paramList, Errors errors) throws ErrorsException {
    List<TypeLiteral<?>> params = type.getParameterTypes(constructor);
    Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
    int p = 0;
    for (TypeLiteral<?> param : params) {
      Key<?> paramKey = getKey(param, constructor, paramAnnotations[p++], errors);
      if(paramKey.getAnnotationType() == Assisted.class && !paramList.contains(paramKey)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns a key similar to {@code key}, but with an {@literal @}Assisted
   * binding annotation.
   *
   * This fails if another binding annotation is clobbered in the process. If
   * the key already has the {@literal @}Assisted annotation, it is returned
   * as-is to preserve any String value.
   */
  private <T> Key<T> assistKey(Method method, Key<T> key, Errors errors) throws ErrorsException {
    if (key.getAnnotationType() == null) {
      return Key.get(key.getTypeLiteral(), DEFAULT_ANNOTATION);
    } else if (key.getAnnotationType() == Assisted.class) {
      return key;
    } else {
      errors.withSource(method).addMessage(PrettyPrinter.format(
          "Only @Assisted is allowed for factory parameters, but found @%s",
          key.getAnnotationType()));
      throw errors.toException();
    }
  }

  private static class AssistData {
    final TypeLiteral<?> implementation;
    final MethodLiteral<?, Constructor<?>> constructor;
    final MethodLiteral<?, Method> method;
    final String[] parameterNames;

    private AssistData(TypeLiteral<?> implementation, MethodLiteral<?, Constructor<?>> constructor,
        MethodLiteral<?, Method> method, String[] parameterNames) {
      this.implementation = implementation;
      this.parameterNames = parameterNames;
      this.method = method;
      this.constructor = constructor;
    }
  }
}
