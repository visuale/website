/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ocpsoft.common.util.Streams;
import org.ocpsoft.urlbuilder.util.Decoder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Singleton
public class Downloader implements Serializable
{
   private static final Logger log = Logger.getLogger(Downloader.class.getName());
   private static final long serialVersionUID = 1111860061208554914L;
   private static final long CACHE_EXPIRY_INTERVAL = 1000 * 60 * 60;

   // TODO Cache this on disk or dcp.jboss.org instead of in memory.
   private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

   public String download(String url) throws IllegalStateException
   {
      String content = getContentFromCache(url);

      if (content == null)
      {

         try (CloseableHttpClient client = HttpClientBuilder.create().build())
         {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200)
               content = Streams.toString(response.getEntity().getContent());

            if (response.getStatusLine().getStatusCode() / 10 == 20)
               cache.put(url, new CacheEntry(url, content, System.currentTimeMillis()));

            else
               throw new IllegalStateException("failed! (server returned status code: "
                        + response.getStatusLine().getStatusCode() + ") for URL [" + url + "]");
         }
         catch (IllegalStateException e)
         {
            log.log(Level.SEVERE, "Error downloading URL [" + url + "]", e);
            throw e;
         }
         catch (Exception e)
         {
            log.log(Level.SEVERE, "Error downloading URL [" + url + "]", e);
            throw new IllegalStateException("Failed to download: " + url, e);
         }

      }

      if (content == null)
      {
         content = getContentFromCacheUnrestricted(url);
      }

      return content;
   }

   public void invalidateCaches()
   {
      for (CacheEntry entry : cache.values())
      {
         entry.invalidate();
      }
   }

   public void invalidateCachesByAddress(String pattern)
   {
      pattern = ".*" + Pattern.quote(pattern) + ".*";
      log.info("Invalidating caches for pattern [" + pattern + "]");
      for (CacheEntry entry : cache.values())
      {
         if (entry.getAddress().matches(pattern)
                  || Decoder.query(entry.getAddress()).matches(pattern)
                  || Decoder.path(entry.getAddress()).matches(pattern))
         {
            log.info("Invalidating cache entry [" + entry.getAddress() + "] for pattern [" + pattern + "]");
            entry.invalidate();
         }
      }
   }

   private String getContentFromCache(String url)
   {
      String result = null;
      CacheEntry entry = cache.get(url);
      if (entry != null && System.currentTimeMillis() - entry.getTime() < CACHE_EXPIRY_INTERVAL)
         result = entry.getContent();
      return result;
   }

   private String getContentFromCacheUnrestricted(String url)
   {
      String result = null;
      CacheEntry entry = cache.get(url);
      if (entry != null)
         result = entry.getContent();
      return result;
   }
}
