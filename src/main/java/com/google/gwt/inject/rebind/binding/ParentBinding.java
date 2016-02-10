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

import com.google.gwt.inject.rebind.ErrorManager;
import com.google.gwt.inject.rebind.GinjectorBindings;
import com.google.gwt.inject.rebind.reflect.NoSourceNameException;
import com.google.gwt.inject.rebind.reflect.ReflectUtil;
import com.google.gwt.inject.rebind.util.InjectorMethod;
import com.google.gwt.inject.rebind.util.NameGenerator;
import com.google.gwt.inject.rebind.util.Preconditions;
import com.google.gwt.inject.rebind.util.SourceSnippet;
import com.google.gwt.inject.rebind.util.SourceSnippetBuilder;
import com.google.gwt.inject.rebind.util.SourceSnippets;
import com.google.inject.Key;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Binding that represents a value inherited from higher in the injector hierarchy.
 * 
 * TODO(bchambers): It would be nice if we didn't need to have the creator/parent
 * paradigm for parent and child bindings, but it is the easiest way to add this
 * to Gin.
 */
public class ParentBinding extends AbstractBinding implements Binding {

  private final ErrorManager errorManager;
  private final Key<?> key;
  private final GinjectorBindings parentBindings;

  ParentBinding(ErrorManager errorManager, Key<?> key, GinjectorBindings parentBindings,
      Context context) {
    super(context);

    this.errorManager = Preconditions.checkNotNull(errorManager);
    this.key = Preconditions.checkNotNull(key);
    this.parentBindings = Preconditions.checkNotNull(parentBindings);
  }

  /**
   * The getter must be placed in the same package as the parent getter, to ensure that its return
   * type is visible.
   */
  public String getGetterMethodPackage() {
    Binding parentBinding = parentBindings.getBinding(key);
    if (parentBinding == null) {
      // The parent binding should exist by the time this is called.
      errorManager.logError("No parent binding found in %s for %s.", parentBindings, key);
      return "";
    } else {
      return parentBinding.getGetterMethodPackage();
    }
  }
  
  public GinjectorBindings getParentBindings() {
    return parentBindings;
  }

  public SourceSnippet getCreationStatements(NameGenerator nameGenerator,
      List<InjectorMethod> methodsOutput) throws NoSourceNameException {
    String type = ReflectUtil.getSourceName(key.getTypeLiteral());

    return new SourceSnippetBuilder()
        .append(type).append(" result = ")
        .append(SourceSnippets.callParentGetter(key, parentBindings)).append(";")
        .build();
  }

  public Collection<Dependency> getDependencies() {
    // ParentBindings are only added *after* resolution has happened, so their dependencies don't
    // matter
    return Collections.emptyList();
  }
}
