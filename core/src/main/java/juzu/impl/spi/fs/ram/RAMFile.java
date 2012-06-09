/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.spi.fs.ram;

import juzu.impl.utils.Content;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMFile extends RAMPath {

  /** . */
  private Content content;

  public RAMFile(RAMDir parent, String name) {
    super(parent, name);

    //
    this.content = new Content(System.currentTimeMillis(), "");
  }

  @Override
  public long getLastModified() {
    return content.getLastModified();
  }

  @Override
  public void touch() {
    content = content.touch();
  }

  public RAMFile update(Content content) {
    if (content == null) {
      throw new NullPointerException();
    }

    //
    this.content = content;

    //
    return this;
  }

  @Override
  public RAMDir addDir(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RAMFile addFile(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RAMPath getChild(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<RAMPath> getChildren() {
    throw new UnsupportedOperationException();
  }

  public Content getContent() {
    return content;
  }
}