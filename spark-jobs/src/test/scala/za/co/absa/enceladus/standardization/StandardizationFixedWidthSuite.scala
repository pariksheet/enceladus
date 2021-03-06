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

package za.co.absa.enceladus.standardization

import org.apache.spark.sql.types.{DataType, StructType}
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.enceladus.dao.MenasDAO
import za.co.absa.enceladus.model.Dataset
import za.co.absa.enceladus.standardization.config.StandardizationConfig
import za.co.absa.enceladus.standardization.interpreter.StandardizationInterpreter
import za.co.absa.enceladus.standardization.interpreter.stages.PlainSchemaGenerator
import za.co.absa.enceladus.utils.fs.FileReader
import za.co.absa.enceladus.utils.testUtils.SparkTestBase
import za.co.absa.enceladus.utils.implicits.DataFrameImplicits.DataFrameEnhancements
import za.co.absa.enceladus.utils.udf.UDFLibrary

class StandardizationFixedWidthSuite extends FunSuite with SparkTestBase with MockitoSugar{
  private implicit val udfLibrary:UDFLibrary = new UDFLibrary()
  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  private val argsBase = ("--dataset-name Foo --dataset-version 1 --report-date 2020-06-22 --report-version 1 " +
    "--menas-auth-keytab src/test/resources/user.keytab.example " +
    "--raw-format fixed-width").split(" ")

  private implicit val dao: MenasDAO = mock[MenasDAO]

  private val dataSet = Dataset("Foo", 1, None, "", "", "SpecialChars", 1, conformance = Nil)

  private val baseSchema: StructType = DataType.fromJson(
    FileReader.readFileAsString("src/test/resources/data/standardization_fixed_width_suite_schema.json")
  ).asInstanceOf[StructType]

  test("Reading data from FixedWidth input") {
    val cmd = StandardizationConfig.getFromArguments(argsBase)

    val fixedWidthReader = new StandardizationPropertiesProvider().getFormatSpecificReader(cmd, dataSet)

    val inputSchema = PlainSchemaGenerator.generateInputSchema(baseSchema)
    val reader = fixedWidthReader.schema(inputSchema)

    val sourceDF = reader.load("src/test/resources/data/standardization_fixed_width_suite_data.txt")

    val expected = FileReader.readFileAsString("src/test/resources/data/standardization_fixed_width_suite_expected_non_trimmed.txt")
      .replace("\r\n", "\n")

    val destDF = StandardizationInterpreter.standardize(sourceDF, baseSchema, cmd.rawFormat)

    val actual = destDF.dataAsString(truncate = false)
    assert(actual == expected)
  }

  test("Reading data from FixedWidth input trimmed") {
    val cmd = StandardizationConfig.getFromArguments(argsBase ++ Array("--trimValues", "true"))

    val fixedWidthReader = new StandardizationPropertiesProvider().getFormatSpecificReader(cmd, dataSet)

    val inputSchema = PlainSchemaGenerator.generateInputSchema(baseSchema)
    val reader = fixedWidthReader.schema(inputSchema)

    val sourceDF = reader.load("src/test/resources/data/standardization_fixed_width_suite_data.txt")

    val expected = FileReader.readFileAsString("src/test/resources/data/standardization_fixed_width_suite_expected_trimmed.txt")
      .replace("\r\n", "\n")

    val destDF = StandardizationInterpreter.standardize(sourceDF, baseSchema, cmd.rawFormat)

    val actual = destDF.dataAsString(truncate = false)
    assert(actual == expected)
  }
}
