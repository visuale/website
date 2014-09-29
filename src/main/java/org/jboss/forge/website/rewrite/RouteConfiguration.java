/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.rewrite;

import javax.servlet.ServletContext;

import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Log;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Redirect;
import org.ocpsoft.rewrite.servlet.config.RequestParameter;
import org.ocpsoft.rewrite.servlet.config.Resource;
import org.ocpsoft.rewrite.servlet.config.SendStatus;
import org.ocpsoft.rewrite.servlet.config.ServletMapping;
import org.ocpsoft.rewrite.servlet.config.bind.RequestBinding;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RewriteConfiguration
public class RouteConfiguration extends HttpConfigurationProvider
{

   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      return ConfigurationBuilder
               .begin()

               /*
                * Page specific routes
                */
               .addRule(Join.path("/").to("/index").withChaining())
               .addRule(Join.path("/document/{title}").to("/document").withChaining())
               .where("title").transposedBy(new SpacesToDashes())

               .addRule(Join.path("/news/{date}/{title}").to("/news-entry").withChaining())
               .where("title").transposedBy(new SpacesToDashes()).bindsTo(RequestBinding.parameter("title"))
               .where("date").transposedBy(new DateTransposition())

               .addRule(Join.path("/addon/{id}").to("/addon").withChaining())

               /*
                * Block direct file access.
                */
               .addRule()
               .when(DispatchType.isRequest().and(Direction.isInbound())
                        .and(Path.matches("/{p}.xhtml"))
                        .and(Resource.exists("/{p}.xhtml"))
                        .andNot(ServletMapping.includes("/{p}")))
               .perform(Log.message(Level.INFO, "Blocked direct file access to {p}").and(SendStatus.error(404)))
               .where("p").matches(".*")

               /*
                * Application Routes
                */
               .addRule(Join.path("/{p}/").to("/faces/{p}/index.xhtml").withChaining())
               .when(Resource.exists("/{p}/index.xhtml"))
               .perform(Log.message(Level.INFO, "Joined path /{p}/ to /faces/{p}/index.xhtml"))
               .where("p").matches(".*")

               .addRule(Join.path("/{p}").to("/faces/{p}.xhtml").withChaining())
               .when(Resource.exists("/{p}.xhtml"))
               .perform(Log.message(Level.INFO, "Joined path /{p} to /faces/{p}.xhtml"))
               .where("p").matches(".*")

               .addRule()
               .when(DispatchType.isRequest().and(Direction.isInbound())
                        .and(RequestParameter.exists("ticket")).and(Path.matches("/auth")))
               .perform(Redirect.temporary(context.getContextPath()))

               /*
                * Resources routes
                */
               .addRule(Join.path("/faces/images/{p}").to("/faces/javax.faces.resource/{p}?ln=images"))
               .when(Resource.exists("/resources/images/{p}"))
               .perform(Log.message(Level.INFO,
                        "Joined path /faces/images/{p} to /faces/javax.faces.resource/{p}?ln=images"))
               .where("p").matches(".*")

      ;
   }

   @Override
   public int priority()
   {
      return 0;
   }

}
