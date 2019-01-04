/*
 * Copyright 2019 ABSA Group Limited
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

package za.co.absa.enceladus.testutils.exceptions

import org.apache.spark.sql.types.StructField

final case class CmpJobDatasetsDifferException(private val refPath: String,
                                                private val stdPath: String,
                                                private val outPath: String,
                                                private val expectedCount: Long,
                                                private val actualCount: Long,
                                                private val cause: Throwable = None.orNull)
  extends Exception("Expected and actual datasets differ.\n" +
                    s"Reference path: $refPath\n" +
                    s"Actual dataset path: $stdPath\n" +
                    s"Difference written to: $outPath\n" +
                    s"Count Expected( $expectedCount ) vs Actual( $actualCount )", cause)

final case class CmpJobSchemasDifferException(private val refPath: String,
                                              private val stdPath: String,
                                              private val diffSchema: Seq[StructField],
                                              private val cause: Throwable = None.orNull)
  extends Exception("Expected and actual datasets differ in schemas.\n"+
                    s"Reference path: $refPath\n" +
                    s"Actual dataset path: $stdPath\n" +
                    s"Difference is $diffSchema", cause)