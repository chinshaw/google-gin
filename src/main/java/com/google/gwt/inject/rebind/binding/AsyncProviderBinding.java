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

import com.google.gwt.inject.rebind.reflect.NoSourceNameException;
import com.google.gwt.inject.rebind.reflect.ReflectUtil;
import com.google.gwt.inject.rebind.util.InjectorMethod;
import com.google.gwt.inject.rebind.util.NameGenerator;
import com.google.gwt.inject.rebind.util.Preconditions;
import com.google.gwt.inject.rebind.util.SourceSnippet;
import com.google.gwt.inject.rebind.util.SourceSnippetBuilder;
import com.google.gwt.inject.rebind.util.SourceSnippets;
import com.google.inject.Key;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Binding implementation for {@code AsyncProvider<T>} that generates
 * the following code for the provider:
 * 
 * <pre style=code>
 *   return new %provider_name% () {
 *      public void get(final AsyncCallback<%object_to_create%> callback) {
 *        GWT.runAsync(new RunAsyncCallback() {
 *          public void onSuccess() {
 *            callback.onSuccess(%provider_of_object_to_create%.get());
 *          }
 *          public void onFailure(Throwable ex) {
 *            callback.onFailure(ex);
 *          }
 *        }
 *      }
 *   }
 * 
 * </pre>
 */
public class AsyncProviderBinding extends AbstractBinding implements Binding {

  private ParameterizedType providerType;
  private final Key<?> providerKey;
  private final Key<?> targetKey;

  private AsyncProviderBinding(Key<?> providerKey, Key<?> targetKey) {
    super(Context.format("Implicit injection of %s", providerKey), targetKey);

    this.providerKey = Preconditions.checkNotNull(providerKey);
    providerType = (ParameterizedType) providerKey.getTypeLiteral().getType();
    this.targetKey = targetKey;
  }

  AsyncProviderBinding(Key<?> providerKey) {
    this(providerKey, ReflectUtil.getProvidedKey(providerKey));
  }

  public SourceSnippet getCreationStatements(NameGenerator nameGenerator,
      List<InjectorMethod> methodsOutput) throws NoSourceNameException {
    String providerTypeName = ReflectUtil.getSourceName(providerType);
    String targetKeyName = ReflectUtil.getSourceName(targetKey.getTypeLiteral());

    return new SourceSnippetBuilder()
        .append(providerTypeName).append(" result = new ")
        .append(providerTypeName).append("() { \n")
        .append("    public void get(")
        .append("final com.google.gwt.user.client.rpc.AsyncCallback<? super ")
        .append(targetKeyName).append("> callback) { \n")
        .append("      com.google.gwt.core.client.GWT.runAsync(")
        .append(targetKey.getTypeLiteral().getRawType().getCanonicalName())
        .append(".class,")
        .append("new com.google.gwt.core.client.RunAsyncCallback() { \n")
        .append("        public void onSuccess() { \n")
        .append("          callback.onSuccess(")
        .append(SourceSnippets.callGetter(targetKey)).append(");\n")
        .append("        }\n")
        .append("        public void onFailure(Throwable ex) { \n ")
        .append("          callback.onFailure(ex); \n" )
        .append("        } \n")
        .append("    }); \n")
        .append("    }\n")
        .append(" };\n")
        .build();
  }

  public Collection<Dependency> getDependencies() {    
    return Collections.singleton(new Dependency(providerKey, targetKey, false, true, getContext()));
  }
}
