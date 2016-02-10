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
package com.google.gwt.inject.rebind.binding;

import com.google.gwt.inject.rebind.GuiceBindingVisitor;
import com.google.gwt.inject.rebind.util.Preconditions;
import com.google.gwt.inject.rebind.util.PrettyPrinter;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * Representation of a dependency edge.  Contains information about the source and target 
 * {@code Key<?>}s, as well as the properties of the edge (optional/requried, lazy/eager).
 *
 * <p>Dependencies also store the context in which the dependency arose.  This
 * is not part of the dependency's identity from the point of view of equals()
 * and hashCode(), which ultimately means that if the same dependency arises in
 * several different contexts, we will pick an arbitrary instance (specifically,
 * the first one encountered, see {@link DependencyGraph}).
 */
public class Dependency {
  
  private static final class GinjectorSourceClass {}
  
  /**
   * A unique key used to indicate that a dependency originated in the Ginjector; for instance, 
   * keys produced by the root Ginjector, or while visiting bindings in {@link GuiceBindingVisitor}
   * often have as their source {@link Dependency#GINJECTOR}.
   */
  public static final Key<?> GINJECTOR = Key.get(GinjectorSourceClass.class);
  
  private final Key<?> source;
  private final Key<?> target;
  private final boolean optional;
  private final boolean lazy;
  private final Context context;
  
  /**
   * Construct a dependency edge from the given source to target keys.  Creates a required, eager 
   * edge.
   * 
   * @param source The key that depends on target.  Can use {@link Dependency#GINJECTOR} as 
   *     described above.
   * @param target the key that is depended on
   * @param context a brief description of the context of the dependency (e.g., Foo.java:123)
   * @param contextArgs arguments to pretty-print into the context as with
   *     {@link PrettyPrinter#format}
   */
  public Dependency(Key<?> source, Key<?> target, String context, Object... contextArgs) {
    this(source, target, false, false, context, contextArgs);
  }

  /**
   * Construct a dependency edge from the given source to target keys.  Creates a required, eager 
   * edge.
   *
   * @param source The key that depends on target.  Can use {@link Dependency#GINJECTOR} as
   *     described above.
   * @param target the key that is depended on
   * @param context the context of the dependency (e.g., Foo.java:123)
   */
  public Dependency(Key<?> source, Key<?> target, Context context) {
    this(source, target, false, false, context);
  }
  
  /**
   * Construct a dependency edge from the given source to target keys.
   * 
   * @param source The key that depends on the target.  Can use {@link Dependency#GINJECTOR} as
   *     described above.
   * @param target the key that is depended on
   * @param optional {@code true} iff the dependency is optional.  Errors will not be reported if
   *     the target is unavailable.
   * @param lazy {@code true} iff the dependency is only needed on-demand (eg, by calling 
   *     {@link Provider#get}).  A cycle is only a problem if none of the edges are lazy.
   * @param context a brief description of the context of the dependency (e.g., Foo.java:123)
   * @param contextArgs arguments to pretty-print into the context as with
   *     {@link PrettyPrinter#format}
   */
  public Dependency(Key<?> source, Key<?> target, boolean optional, boolean lazy, String context,
      Object... contextArgs) {
    this(source, target, optional, lazy, Context.format(context, (Object[]) contextArgs));

    Preconditions.checkArgument(!context.isEmpty(), "dependency context must not be empty");
  }

  /**
   * Construct a dependency edge from the given source to target keys.
   * 
   * @param source The key that depends on the target.  Can use {@link Dependency#GINJECTOR} as
   *     described above.
   * @param target the key that is depended on
   * @param optional {@code true} iff the dependency is optional.  Errors will not be reported if
   *     the target is unavailable.
   * @param lazy {@code true} iff the dependency is only needed on-demand (eg, by calling 
   *     {@link Provider#get}).  A cycle is only a problem if none of the edges are lazy.
   * @param context the context of the dependency (e.g., Foo.java:123)
   */
  public Dependency(Key<?> source, Key<?> target, boolean optional, boolean lazy, Context context) {
    Preconditions.checkArgument(source != null, "null is not supported as the source");
    Preconditions.checkArgument(target != null && !target.equals(GINJECTOR),
        "null and GINJECTOR are not supported as the target.");
    Preconditions.checkArgument(context != null, "dependency context must not be null");
    this.source = source;
    this.target = target;
    this.optional = optional;
    this.lazy = lazy;
    this.context = context;
  }

  public Context getContext() {
    return context;
  }
  
  public Key<?> getTarget() {
    return target;
  }
  
  public Key<?> getSource() {
    return source;
  }
  
  public boolean isOptional() {
    return optional;
  }
  
  public boolean isLazy() {
    return lazy;
  }
  
  @Override
  public String toString() {
    return String.format("%s -> %s [context: %s, optional: %s, lazy: %s]",
        source, target, context, optional, lazy);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    
    if (obj == null) {
      return false;
    }
    
    if (obj instanceof Dependency) {
      Dependency other = (Dependency) obj;
      return optional == other.optional
          && lazy == other.lazy
          && (target == null ? other.target == null : target.equals(other.target))
          && (source == null ? other.source == null : source.equals(other.source));
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    int hashCode = source.hashCode();
    hashCode = hashCode * 31 + target.hashCode();
    hashCode = hashCode * 31 + (optional ? 2 : 0) + (lazy ? 1 : 0);
    return hashCode;
  }
}
