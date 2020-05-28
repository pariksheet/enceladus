/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.model

import org.codehaus.jackson.annotate.JsonProperty

case class SchemaField
(
  name: String,
  `type`: String,
  path: String,  // path up to this field

  // These fields are optional when the type of the field is "array".
  elementType: Option[String] = None,
  containsNull: Option[Boolean] = None,

  nullable: Boolean,
  metadata: Map[String, String],
  children: Seq[SchemaField]
) {
  @JsonProperty("absolutePath")
  def getAbsolutePath(): String = {
    if(path.isEmpty) name else s"${path}.${name}"
  }
}

object SchemaField {

  /**
   * Important non-exhastive [[SchemaField]] types (does not include primitive types
   */
  object TypeNames {
    val array: String = "array"
    val struct: String = "struct"
    val map: String = "map"
  }
}
