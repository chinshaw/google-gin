/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.inject.client.misc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.misc.subpackage.StaticSubEagerSingleton;
import com.google.inject.name.Names;

public class StaticInjectGinModule extends AbstractGinModule {

  public static final String NAME = "bar";

  protected void configure() {
    bindConstant().annotatedWith(Names.named("bar")).to(NAME);
    bindConstant().annotatedWith(Names.named("foo")).to("foo");

    bind(StaticEagerSingleton.class).asEagerSingleton();
    bind(StaticSubEagerSingleton.class).asEagerSingleton();

    requestStaticInjection(StaticClass.class);
    requestStaticInjection(StaticEagerSingleton.class);
    requestStaticInjection(StaticSubEagerSingleton.class);
  }
}
