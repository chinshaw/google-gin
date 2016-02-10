/**
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gwt.inject.rebind.reflect;

import java.lang.reflect.Type;

/**
 * Thrown if a source name is requested but cannot be provided.
 *
 * @author schmitt@google.com (Peter Schmitt)
 */
public class NoSourceNameException extends Exception {

  public NoSourceNameException(Type type) {
    super("Failed to find source name for " + type + ".");
  }
}