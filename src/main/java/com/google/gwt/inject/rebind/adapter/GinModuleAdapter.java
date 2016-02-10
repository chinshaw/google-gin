/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.inject.rebind.adapter;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.GinModule;
import com.google.gwt.inject.rebind.GinjectorBindings;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.internal.ProviderMethodsModule;

/**
 * Makes a {@link GinModule} available as a {@link Module}.
 */
public final class GinModuleAdapter implements Module {
  private final GinModule ginModule;
  private final GinjectorBindings bindings;
  private final boolean hideChildModules;
  
  public GinModuleAdapter(GinModule ginModule) {
    this(ginModule, null, false);
  }

  public GinModuleAdapter(GinModule ginModule, GinjectorBindings bindings) {
    this(ginModule, bindings, false);
  }

  public GinModuleAdapter(GinModule ginModule, GinjectorBindings bindings,
      boolean hideChildModules) {
    if (ginModule == null) {
      throw new NullPointerException("Installing a null module is not permitted");
    }

    this.ginModule = ginModule;
    this.bindings = bindings;
    this.hideChildModules = hideChildModules;
  }

  public void configure(Binder binder) {
    // For Guice error reporting, ignore the adapters
    binder = binder.skipSources(GinModuleAdapter.class, BinderAdapter.class,
        AbstractGinModule.class);

    ginModule.configure(new BinderAdapter(binder, bindings));

    // Install provider methods from the GinModule
    binder.install(ProviderMethodsModule.forObject(ginModule));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GinModuleAdapter) {
      GinModuleAdapter other = (GinModuleAdapter) obj;
      return ginModule.equals(other.ginModule);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return ginModule.hashCode();
  }
}
