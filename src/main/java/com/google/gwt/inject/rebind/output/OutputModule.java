/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.inject.rebind.output;

import com.google.gwt.inject.rebind.util.InjectorWriteContext;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module binding the components of the output package.
 */
public class OutputModule extends PrivateModule {

  public void configure() {
    install(new FactoryModuleBuilder()
        .build(FragmentPackageName.Factory.class));
    install(new FactoryModuleBuilder()
        .implement(InjectorWriteContext.class, GinjectorFragmentContext.class)
        .build(GinjectorFragmentContext.Factory.class));
    install(new FactoryModuleBuilder()
        .build(GinjectorFragmentOutputter.Factory.class));

    bind(GinjectorImplOutputter.class);
    bind(ReachabilityAnalyzer.class).in(Singleton.class);

    expose(FragmentPackageName.Factory.class);
    expose(GinjectorImplOutputter.class);
  }
}
