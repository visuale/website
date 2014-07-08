/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.website.view;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.jboss.forge.website.model.Contributor;
import org.jboss.forge.website.service.RepositoryService;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@Model
public class CommunityBean
{
   @Inject
   private RepositoryService service;

   public List<Contributor> getContributors()
   {
      return new ArrayList<>(service.getAllContributors());
   }

}
