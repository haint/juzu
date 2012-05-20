package org.juzu.plugin.less.impl;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.template.metamodel.TemplateMetaModelPlugin;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Path;
import org.juzu.impl.utils.QN;
import org.juzu.impl.utils.Spliterator;
import org.juzu.impl.utils.Tools;
import org.juzu.plugin.less.Less;
import org.juzu.plugin.less.impl.lesser.Compilation;
import org.juzu.plugin.less.impl.lesser.JSR223Context;
import org.juzu.plugin.less.impl.lesser.Lesser;
import org.juzu.plugin.less.impl.lesser.Result;

import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   private final HashMap<ElementHandle.Package, String[]> enabledMap = new HashMap<ElementHandle.Package, String[]>();

   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values)
   {
      if (fqn.equals(Less.class.getName()))
      {
         ElementHandle.Package handle = application.getHandle();
         List<String> resources = (List<String>)values.get("value");
         enabledMap.put(handle, resources.toArray(new String[resources.size()]));
      }
   }

   @Override
   public void preDestroy(ApplicationMetaModel application)
   {
      enabledMap.remove(application.getHandle());
   }

   @Override
   public void prePassivate(ApplicationMetaModel model)
   {
      String[] resources = enabledMap.remove(model.getHandle());
      if (resources != null && resources.length > 0)
      {
         // For now we use the hardcoded assets package
         QN pkg = model.getFQN().getPackageName().append("assets");

         //
         CompilerLessContext clc = new CompilerLessContext(model.model.env, pkg);

         //
         for (String resource : resources)
         {
            Path path = Path.parse(resource);

            //
            Path.Absolute to = Path.Absolute.create(pkg.append(path.getQN()), path.getRawName(), "css");

            //
            Lesser lesser;
            Result result;
            try
            {
               lesser = new Lesser(new JSR223Context());
               result = lesser.parse(clc, resource);
            }
            catch (Exception e)
            {
               throw new UnsupportedOperationException(e);
            }

            //
            if (result instanceof Compilation)
            {
               try
               {
                  Compilation compilation = (Compilation)result;
                  FileObject fo = model.model.env.createResource(StandardLocation.CLASS_OUTPUT, to);
                  Writer writer = fo.openWriter();
                  try
                  {
                     writer.write(compilation.getValue());
                  }
                  finally
                  {
                     Tools.safeClose(writer);
                  }
               }
               catch (IOException e)
               {
                  throw new UnsupportedOperationException(e);
               }
            }
         }
      }
   }
}
