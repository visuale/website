/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.website.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DownloadResult implements Closeable
{
   private static final Logger log = Logger.getLogger(DownloadResult.class.getName());

   private String contentType;
   private String url;

   private CloseableHttpClient client;

   private HttpResponse response;

   public DownloadResult(String url)
   {
      this.url = url;
   }

   public InputStream getStream() throws IOException
   {
      request();
      if (response.getStatusLine().getStatusCode() == 200)
      {
         this.contentType = response.getEntity().getContentType().getValue();
         return response.getEntity().getContent();
      }
      else
      {
         throw new IllegalStateException("failed! (server returned status code: "
                  + response.getStatusLine().getStatusCode() + ") for URL [" + url + "]");
      }
   }

   public String getContentType()
   {
      request();
      return contentType;
   }

   private void request()
   {
      if (this.client == null && this.response == null)
      {
         try
         {
            this.client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(url);
            this.response = client.execute(get);
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
   }

   @Override
   public void close() throws IOException
   {
      if (client != null)
         client.close();
   }

}
