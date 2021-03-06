/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package plugin.upload.resource;

import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.upload.AbstractUploadTestCase;
import org.apache.commons.fileupload.FileItem;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Resource
  @Route("/resource")
  public Response.Status resource(FileItem file, String text, Bean bean) throws IOException {
    if (file != null) {
      AbstractUploadTestCase.contentType = file.getContentType();
      AbstractUploadTestCase.content = file.getString();
      AbstractUploadTestCase.text = text;
      AbstractUploadTestCase.field = bean != null ? bean.field : null;
    }
    return Response.ok();
  }

  @View
  @Route("/index")
  public Response.Content index() {
    return Response.ok(
        "<form action='" + A_.resource(null, null) + "' method='post' enctype='multipart/form-data'>" +
        "<input type='text' id='text' name='text'>" +
        "<input type='text' id='field' name='field'>" +
        "<input type='file' id='file' name='file'>" +
        "<input type='submit' id='submit'>" +
        "</form>");
  }
}
