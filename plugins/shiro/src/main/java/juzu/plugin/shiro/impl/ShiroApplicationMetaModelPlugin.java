/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package juzu.plugin.shiro.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import juzu.impl.common.FQN;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.plugin.shiro.Shiro;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroApplicationMetaModelPlugin extends ApplicationMetaModelPlugin
{
   
   private final Map<ElementHandle.Package, JSON> enableMap = new HashMap<ElementHandle.Package, JSON>();
   
   private final Map<ElementHandle<?>, Map<AnnotationKey, AnnotationState>> methods = new HashMap<ElementHandle<?>, Map<AnnotationKey, AnnotationState>>();
   
   public ShiroApplicationMetaModelPlugin()
   {
      super("shiro");
   }
   
   @Override
   public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
      return Tools.<Class<? extends java.lang.annotation.Annotation>>set(
         Shiro.class, 
         RequiresGuest.class, 
         RequiresUser.class,
         RequiresAuthentication.class,
         RequiresPermissions.class, 
         RequiresRoles.class);
    }
   
   @Override
   public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) 
   {
      ElementHandle.Package handle = metaModel.getHandle();
      if(key.getType().equals(new FQN(Shiro.class)))
      {
         JSON json = new JSON();
         enableMap.put(handle, json);
      }
      else 
      {
         emitConfig(key, added);
      }
   }
   
   private void emitConfig(AnnotationKey key, AnnotationState added) {
      if (key.getType().equals(new FQN(RequiresGuest.class)) ||
               key.getType().equals(new FQN(RequiresUser.class)) ||
               key.getType().equals(new FQN(RequiresAuthentication.class)) ||
               key.getType().equals(new FQN(RequiresRoles.class)) ||
               key.getType().equals(new FQN(RequiresPermissions.class)))
      {
         if(key.getElement() instanceof ElementHandle.Method)
         {
            Map<AnnotationKey, AnnotationState> annotations = methods.get(key.getElement());
            if(annotations == null)
            {
               annotations = new HashMap<AnnotationKey, AnnotationState>();
               methods.put(key.getElement(), annotations);
            }
            annotations.put(key, added);
         }
      }
   }
   
   private void emitConfig(JSON json, AnnotationKey key, AnnotationState added)
   {
      if(key.getType().equals(new FQN(RequiresGuest.class)))
      {
         json.set("guest", true);
      }
      else if(key.getType().equals(new FQN(RequiresUser.class)))
      {
         json.set("user", true);
      }
      else if(key.getType().equals(new FQN(RequiresAuthentication.class)))
      {
         json.set("authenticate", true);
      }
      else if(key.getType().equals(new FQN(RequiresPermissions.class)))
      {
         ArrayList<String> values = (ArrayList<String>)added.get("value");
         String logical = (String)added.get("logical");
         JSON foo = new JSON();
         foo.set("value", values);
         if(logical != null)
         {
            foo.set("logical", logical);
         }
         else
         {
            foo.set("logical", Logical.AND);
         }
         json.set("permission", foo);
      }
      else if(key.getType().equals(new FQN(RequiresRoles.class)))
      {
         ArrayList<String> values = (ArrayList<String>)added.get("value");
         String logical = (String)added.get("logical");
         JSON foo = new JSON();
         foo.set("value", values);
         if(logical != null)
         {
            foo.set("logical", logical);
         }
         else
         {
            foo.set("logical", Logical.AND);
         }
         json.set("role", foo);
      }
   }
   
   @Override
   public void postProcessEvents(ApplicationMetaModel metaModel)
   {
      ElementHandle.Package handle = metaModel.getHandle();
      JSON config = enableMap.get(handle);
      if(config != null)
      {
         ControllersMetaModel controllers = metaModel.getChild(ControllersMetaModel.KEY);
         JSON foo = new JSON();
         for(ControllerMetaModel controller : controllers)
         {
            for(MethodMetaModel method : controller)
            {
               Map<AnnotationKey, AnnotationState> annotations = methods.get(method.getHandle());
               if(annotations == null) 
               {
                  continue;
               }
               
               JSON bar = new JSON();
               for(Map.Entry<AnnotationKey, AnnotationState> entry : annotations.entrySet())
               {
                  emitConfig(bar, entry.getKey(), entry.getValue());
               }
               foo.set(method.getHandle().getMethodHandle().toString(), bar);
            }
         }
         config.set("methods", foo);
      }
   }
   
   @Override
   public void destroy(ApplicationMetaModel application) 
   {
      enableMap.remove(application.getHandle());
   }
   
   @Override
   public JSON getDescriptor(ApplicationMetaModel application) 
   {
      return enableMap.get(application.getHandle());
   }
}
