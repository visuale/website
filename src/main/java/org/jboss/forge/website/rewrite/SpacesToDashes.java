package org.jboss.forge.website.rewrite;

import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.Transposition;

public final class SpacesToDashes implements Transposition<String>
{
   @Override
   public String transpose(Rewrite event, EvaluationContext context, String value)
   {
      if (Direction.isOutbound().evaluate(event, context))
         return value.replaceAll("\\+|\\s+", "-").replaceAll("[-]+", "-").toLowerCase();
      else
         return value.replaceAll("-", " ").replaceAll("\\s+", " ");
   }
}