package org.jboss.forge.website.view;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.forge.website.SiteConstants;
import org.jboss.forge.website.model.News;
import org.jboss.forge.website.service.Downloader;
import org.jboss.forge.website.service.RepositoryService;
import org.ocpsoft.common.util.Strings;
import org.ocpsoft.urlbuilder.Address;
import org.ocpsoft.urlbuilder.AddressBuilder;

/**
 * Backing bean for Document entities.
 * <p>
 * This class provides CRUD functionality for all Document entities. It focuses purely on Java EE 6 standards (e.g.
 * <tt>&#64;ConversationScoped</tt> for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or custom base class.
 */

@Named
@ConversationScoped
public class NewsBean implements Serializable
{
   private static final long serialVersionUID = -1447177331142569029L;

   @Inject
   private transient Downloader downloader;

   @Inject
   private RepositoryService service;

   private String documentTitle;
   private News document;

   private List<News> documents;
   private String searchQuery;

   public void load()
   {
      List<News> result = new ArrayList<>();
      List<News> documents = service.getAllNews();

      for (News document : documents)
      {
         if (Strings.isNullOrEmpty(searchQuery)
                  ||
                  (document.getTitle() != null && document.getTitle().toLowerCase().contains(searchQuery.toLowerCase()))
                  || (document.getSummary() != null && document.getSummary().toLowerCase()
                           .contains(searchQuery.toLowerCase()))
                  || (document.getAuthor() != null && document.getAuthor().toLowerCase()
                           .contains(searchQuery.toLowerCase())))
         {
            result.add(document);
         }

      }

      this.setDocuments(result);
   }

   public void retrieve() throws IOException
   {
      if (documentTitle != null)
      {
         for (News document : service.getAllNews())
         {
            if (documentTitle.equalsIgnoreCase(document.getTitle().replaceAll("-+", " ").replaceAll("\\s+", " ")))
            {
               this.document = document;
               this.documentTitle = document.getTitle();
               break;
            }
         }
      }

      if (document == null)
      {
         FacesContext.getCurrentInstance().getExternalContext().dispatch("/404");
      }
   }

   public String getSearchQuery()
   {
      return searchQuery;
   }

   public void setSearchQuery(String searchQuery)
   {
      this.searchQuery = searchQuery;
   }

   public List<News> getDocuments()
   {
      return documents;
   }

   public void setDocuments(List<News> documents)
   {
      this.documents = documents;
   }

   public String getDocumentHTML() throws MalformedURLException
   {
      String result = null;

      if (document != null)
      {
         Address address = AddressBuilder.begin().scheme("http").domain(SiteConstants.REDOCULOUS_DOMAIN)
                  .path("/api/v1/serve")
                  .query("repo", document.getRepo())
                  .query("ref", document.getRef())
                  .query("path", document.getPath()).build();

         try
         {
            result = downloader.download(address.toString());
         }
         catch (IllegalStateException e)
         {
            System.out.println("Failed to download document: " + address);
         }
      }

      if (Strings.isNullOrEmpty(result))
         result = "No Content";

      return result;
   }

   public News getDocument()
   {
      return this.document;
   }

   public String getDocumentTitle()
   {
      return documentTitle;
   }

   public void setDocumentTitle(String documentTitle)
   {
      this.documentTitle = documentTitle;
   }
}