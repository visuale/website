/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.jboss.forge.website.SiteConstants;
import org.jboss.forge.website.model.Addon;
import org.jboss.forge.website.model.Addon.Category;
import org.jboss.forge.website.model.Contributor;
import org.jboss.forge.website.model.Document;
import org.jboss.forge.website.model.News;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RepositoryService implements Serializable
{
   private static final long serialVersionUID = -5958264325903728496L;

   @Inject
   private Downloader downloader;

   public List<Addon> getAllAddons()
   {
      List<Addon> result = new ArrayList<>();

      List<Addon> community = fetchYamlList(SiteConstants.ADDON_REPO_URL_COMMUNITY, Addon.class);
      for (Addon addon : community)
      {
         addon.setCategory(Category.COMMUNITY);
      }
      result.addAll(community);

      List<Addon> core = fetchYamlList(SiteConstants.ADDON_REPO_URL_CORE, Addon.class);
      for (Addon addon : core)
      {
         addon.setCategory(Category.CORE);
      }
      result.addAll(core);

      return result;
   }

   public List<Addon> getRandomCommunityAddons(int count)
   {
      List<Addon> result = new ArrayList<>();
      List<Addon> addons = new ArrayList<>(fetchYamlList(SiteConstants.ADDON_REPO_URL_COMMUNITY, Addon.class));

      Random random = new Random(System.currentTimeMillis());
      while (result.size() < count && !addons.isEmpty())
      {
         result.add(addons.remove(random.nextInt(addons.size())));
      }

      return result;
   }

   public List<Document> getAllDocuments()
   {
      List<Document> result = new ArrayList<>();

      List<Document> getStarted = fetchYamlList(SiteConstants.DOCS_REPO_URL_GETSTARTED, Document.class);
      for (Document document : getStarted)
      {
         document.setCategory(org.jboss.forge.website.model.Document.Category.QUICKSTART);
      }
      result.addAll(getStarted);

      List<Document> tutorials = fetchYamlList(SiteConstants.DOCS_REPO_URL_TUTORIALS, Document.class);
      for (Document document : tutorials)
      {
         document.setCategory(org.jboss.forge.website.model.Document.Category.TUTORIAL);
      }
      result.addAll(tutorials);

      List<Document> advanced = fetchYamlList(SiteConstants.DOCS_REPO_URL_ADVANCED, Document.class);
      for (Document document : advanced)
      {
         document.setCategory(org.jboss.forge.website.model.Document.Category.ADVANCED);
      }
      result.addAll(advanced);

      return result;
   }

   public List<Document> getRelatedDocuments(Document document, int count)
   {
      List<Document> result = new ArrayList<>();
      List<Document> documents = getAllDocuments();

      Random random = new Random(System.currentTimeMillis());
      while (result.size() < count && !documents.isEmpty())
      {
         Document related = documents.remove(random.nextInt(documents.size()));
         if (document.getCategory() != null && document.getCategory().equals(related.getCategory()))
            result.add(related);
      }

      return result;
   }

   public List<Contributor> getRandomContributors(int count)
   {
      List<Contributor> result = new ArrayList<>();
      List<Contributor> contributors = getAllContributors();

      Random random = new Random(System.currentTimeMillis());
      while (result.size() < count && !contributors.isEmpty())
      {
         Contributor related = contributors.get(random.nextInt(contributors.size()));
         if (!result.contains(related))
         {
            result.add(related);
         }
      }

      return result;
   }

   public List<Contributor> getAllContributors()
   {
      String contributorsJson = downloader.download(SiteConstants.CONTRIBUTORS_JSON_URL);
      Contributor[] contributors = parseJson(contributorsJson, Contributor[].class);
      return Arrays.asList(contributors);
   }

   public List<Document> getRelatedDocuments(Addon addon, int count)
   {
      List<Document> result = new ArrayList<>();

      String tagString = addon.getTags();
      if (tagString != null && !tagString.isEmpty())
      {
         for (String tag : tagString.split(","))
         {
            tag = tag.trim();
            for (Document doc : getAllDocuments())
            {
               if ((doc.getSummary() != null && doc.getSummary().contains(tag))
                        || (doc.getSummary() != null && doc.getTitle().contains(tag)))
               {
                  result.add(doc);
               }
            }
         }
      }
      return new ArrayList<Document>();
   }

   public List<News> getAllNews()
   {
      List<News> news = fetchYamlList(SiteConstants.DOCS_REPO_URL_NEWS, News.class);
      return news;
   }

   public List<News> getNews(int count)
   {
      List<News> news = getAllNews();
      return news.subList(0, Math.min(count, news.size()));
   }

   /*
    * Helpers
    */
   private <T> T parseJson(String content, Class<T> type)
   {
      return new Gson().fromJson(content, type);
   }

   private <T> List<T> fetchYamlList(String url, Class<T> type)
   {
      String content = downloader.download(url);

      List<T> result = null;
      if (content != null)
         result = parseYaml(content, type);
      else
         result = new ArrayList<>();

      return result;
   }

   private <T> List<T> parseYaml(String content, Class<T> type)
   {
      List<T> result = new ArrayList<>();

      if (content != null)
      {
         for (String entry : content.trim().split("---"))
         {
            if (!entry.trim().isEmpty())
            {
               T obj = new Yaml().loadAs(entry, type);
               result.add(obj);
            }
         }
      }
      return result;
   }

}
