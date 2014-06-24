/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.rewrite;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.jboss.forge.website.model.News;
import org.jboss.forge.website.service.Downloader;
import org.jboss.forge.website.service.RepositoryService;
import org.jboss.forge.website.view.NewsBean;
import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RewriteConfiguration
public class FeedConfiguration extends HttpConfigurationProvider
{
   private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

   @Inject
   private RepositoryService service;

   @Inject
   private Downloader downloader;

   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      return ConfigurationBuilder.begin()

               .addRule()
               .when(Direction.isInbound().and(Path.matches("/atom.xml")))
               .perform(new HttpOperation()
               {
                  @Override
                  public void performHttp(HttpServletRewrite event, EvaluationContext context)
                  {
                     try
                     {
                        SyndFeed feed = new SyndFeedImpl();
                        feed.setFeedType("atom_1.0");

                        feed.setTitle("JBoss Forge Blog Feed");
                        feed.setLink("http://forge.jboss.org");
                        feed.setDescription("Stay up to date on JBoss Forge");

                        List<SyndEntry> entries = new ArrayList<>();

                        List<News> news = service.getAllNews();
                        for (News item : news)
                        {
                           SyndEntry entry = new SyndEntryImpl();
                           entry.setTitle(item.getTitle());
                           entry.setLink("http://forge.jboss.org/news/" + format.format(item.getDate()) + "/"
                                    + item.getTitle().replaceAll("\\+|\\s+", "-").replaceAll("[-]+", "-").toLowerCase());
                           entry.setAuthor(item.getAuthor());
                           entry.setPublishedDate(item.getDate());
                           entry.setUpdatedDate(item.getDate());

                           SyndContent description = new SyndContentImpl();
                           description.setType("text/plain");
                           description.setValue(item.getSummary());

                           SyndContent content = new SyndContentImpl();
                           content.setType("text/html");
                           content.setValue(NewsBean.getDocumentHtml(downloader, item));

                           entry.setDescription(description);
                           entry.setContents(Arrays.asList(content));

                           entries.add(entry);
                        }

                        feed.setEntries(entries);

                        try (Writer writer = new OutputStreamWriter(event.getResponse().getOutputStream()))
                        {
                           SyndFeedOutput output = new SyndFeedOutput();
                           output.output(feed, writer);
                        }
                     }
                     catch (Exception e)
                     {
                        e.printStackTrace();
                     }
                  }
               }.and(Response.setStatus(200)).and(Response.complete()))

      /*
       * Create the atom feed
       */
      ;
   }

   @Override
   public int priority()
   {
      return 50;
   }

}
