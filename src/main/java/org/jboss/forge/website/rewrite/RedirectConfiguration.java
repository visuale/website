/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.rewrite;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.jboss.forge.website.SiteConstants;
import org.jboss.forge.website.service.Downloader;
import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Redirect;
import org.ocpsoft.rewrite.servlet.config.Resource;
import org.ocpsoft.rewrite.servlet.config.ServletMapping;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RewriteConfiguration
public class RedirectConfiguration extends HttpConfigurationProvider
{
   @Inject
   private Downloader downloader;

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
               .perform(new HttpOperation()
               {
                  @Override
                  public void performHttp(HttpServletRewrite event, EvaluationContext context)
                  {
                     String content = downloader.download(SiteConstants.SH_SCRIPT_URL);
                     PrintWriter writer;
                     try
                     {
                        writer = event.getResponse().getWriter();
                        writer.print(content);
                        writer.flush();
                     }
                     catch (IOException e)
                     {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                     }
                     event.handled();
                     event.abort();
                  }
               })
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
