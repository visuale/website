package org.jboss.forge.website.rewrite;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.InboundRewrite;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.Transposition;

public class DateTransposition implements Transposition<String>
{
   private DateFormat inboundDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
   private DateFormat outboundDateFormat = new SimpleDateFormat("yyyy-MM-dd");

   @Override
   public String transpose(Rewrite event, EvaluationContext context, String value)
   {
      try
      {
         synchronized (inboundDateFormat)
         {
            if (event instanceof InboundRewrite)
               return inboundDateFormat.format(outboundDateFormat.parse(value));
            else
               return outboundDateFormat.format(inboundDateFormat.parse(value));
         }
      }
      catch (ParseException e)
      {
         return value;
      }
   }

}