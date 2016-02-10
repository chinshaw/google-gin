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

package com.google.gwt.inject.client.nested;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Outer {

  public static class Inner {

    public static class InnerInner {

      private final String hello;
      @Inject @Named("world") private String world;

      @Inject
      public InnerInner(@Named("hello") String hello) {
        this.hello = hello;
      }

      public String getHello() {
        return hello;
      }

      public String getWorld() {
        return world;
      }
    }
  }
}
