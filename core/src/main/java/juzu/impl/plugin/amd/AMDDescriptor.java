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
package juzu.impl.plugin.amd;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import juzu.Scope;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.common.NameLiteral;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AMDDescriptor extends Descriptor {
  
  private final List<AssetMetaData> modules;
  
  public AMDDescriptor(List<AssetMetaData> modules) {
    this.modules = modules;
  }
  
  public List<AssetMetaData> getModules() {
    return modules;
  }
  
  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Tools.list(
      BeanDescriptor.createFromBean(
        AssetManager.class,
        Scope.SINGLETON,
        Collections.<Annotation>singletonList(new NameLiteral("juzu.asset_manager.amd"))));
  }
}
