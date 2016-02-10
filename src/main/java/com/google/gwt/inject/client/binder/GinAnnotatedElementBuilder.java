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
package com.google.gwt.inject.client.binder;

import java.lang.annotation.Annotation;

/**
 * Builder returned from calls to expose, used to add an (optional) annotation
 * to the element that is being exposed.  This is similar to 
 * {@link GinAnnotatedBindingBuilder} but doesn't allow specifying a value.
 * See the EDSL examples at {@link GinBinder}.
 */
public interface GinAnnotatedElementBuilder {
  /**
   * See the EDSL examples at {@link GinBinder}.
   */
  void annotatedWith(Class<? extends Annotation> annotationType);

  /**
   * See the EDSL examples at {@link GinBinder}.
   */
  void annotatedWith(Annotation annotation);
}
