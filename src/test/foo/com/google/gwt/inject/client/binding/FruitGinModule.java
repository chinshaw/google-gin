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
package com.google.gwt.inject.client.binding;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.name.Names;

public class FruitGinModule extends AbstractGinModule {

  public static final boolean EATEN = false;

  // TODO(schmitt):  Maybe fix this eventually.
  // Guice does not support byte constants.
  /*public static final byte ID = 0x41;*/
  public static final char INITIAL = 'a';
  public static final double VOLUME = 20.2;
  public static final float WEIGHT = 200.5f;
  public static final int SEEDS = 4;
  public static final long WORMS = 1;
  public static final short LEAVES = 2;
  public static final String NAME = "Apple";
  public static final Color COLOR = Color.Red;
  public static final Color ALTERNATIVE_COLOR = Color.Yellow;
  public static final Fruit.Family FAMILY = Fruit.Family.Rosaceae;

  protected void configure() {
    bindConstant().annotatedWith(Names.named("eaten")).to(EATEN);
    /*bindConstant().annotatedWith(Names.named("id")).to(ID);*/
    bindConstant().annotatedWith(Names.named("initial")).to(INITIAL);
    bindConstant().annotatedWith(Names.named("volume")).to(VOLUME);
    bindConstant().annotatedWith(Names.named("weight")).to(WEIGHT);
    bindConstant().annotatedWith(Names.named("seeds")).to(SEEDS);
    bindConstant().annotatedWith(Names.named("worms")).to(WORMS);
    bindConstant().annotatedWith(Names.named("leaves")).to(LEAVES);
    bindConstant().annotatedWith(Names.named("name")).to(NAME);
    bindConstant().annotatedWith(Names.named("color")).to(COLOR);

    // Bind enum with custom implementation.
    bindConstant().annotatedWith(Names.named("alternativeColor")).to(ALTERNATIVE_COLOR);
    bindConstant().annotatedWith(Names.named("family")).to(FAMILY);
    
    bindConstant().annotatedWith(Names.named("color-class")).to(Color.class);
    bindConstant().annotatedWith(Names.named("family-class")).to(Fruit.Family.class);

    bind(Plant.class).to(Tree.class).asEagerSingleton();
  }
}
