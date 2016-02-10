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
package com.google.gwt.inject.client.constructor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

public class ConstructorInjectTest extends GWTTestCase {

  public void testSimpleInject() {
    AnimalGinjector injector = GWT.create(AnimalGinjector.class);

    assertNotNull(injector.getCat().getName());
    assertEquals(AnimalGinModule.NAME, injector.getCat().getName());
  }

  public void testPrivateInject() {
    AnimalGinjector injector = GWT.create(AnimalGinjector.class);

    assertNotNull(injector.getDog().getName());
    assertEquals(AnimalGinModule.NAME, injector.getDog().getName());
  }

  public void testSelectDefaultConstructorInject() {
    AnimalGinjector injector = GWT.create(AnimalGinjector.class);

    assertEquals(Giraffe.DEFAULT_NECK_LENGTH, injector.getGiraffe().getNeckLength());
  }

  public String getModuleName() {
    return "com.google.gwt.inject.InjectTest";
  }
}
