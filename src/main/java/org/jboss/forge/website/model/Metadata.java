/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.website.model;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class Metadata
{
   private String latestVersion;

   public String getLatestVersion()
   {
      return latestVersion;
   }

   public void setLatestVersion(String latestVersion)
   {
      this.latestVersion = latestVersion;
   }
}
