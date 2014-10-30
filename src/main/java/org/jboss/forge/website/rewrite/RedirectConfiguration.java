/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.rewrite;

import javax.servlet.ServletContext;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Redirect;
import org.ocpsoft.rewrite.servlet.config.Resource;
import org.ocpsoft.rewrite.servlet.config.ServletMapping;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RewriteConfiguration
public class RedirectConfiguration extends HttpConfigurationProvider
{
   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      return ConfigurationBuilder
               .begin()
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Path.matches("/sh"))
               )
               .perform(Redirect.permanent("https://raw.githubusercontent.com/forge/core/master/forge-install.sh"))
               /*
                * Redirect requests to the old website to the /1.x/ path
                */

               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Path.matches("/blog/atom.xml"))
               )
               .perform(Redirect.permanent(context.getContextPath() + "/atom.xml"))
               
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Path.matches("/{p}"))
                        .andNot(Path.matches("/1.x/{*}"))
                        .andNot(Resource.exists("/{p}"))
                        .andNot(ServletMapping.includes("/{p}"))
               )
               .perform(Redirect.permanent(context.getContextPath() + "/1.x/{p}"))
               .where("p").matches(".*");
   }

   @Override
   public int priority()
   {
      return 100;
   }

}
