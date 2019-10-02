/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.enceladus.dao.rest

import org.scalactic.{AbstractStringUniformity, Uniformity}
import za.co.absa.enceladus.dao.JsonSerializer
import za.co.absa.enceladus.model.test.VersionedModelMatchers
import za.co.absa.enceladus.model.test.factories.{DatasetFactory, MappingTableFactory, RunFactory, SchemaFactory}
import za.co.absa.enceladus.model.{Dataset, MappingTable, Run, Schema}

class JsonSerializerSuite extends BaseTestSuite with VersionedModelMatchers {

  "JsonSerializer" should {
    "handle Datasets" when {
      val datasetJson =
        """
          |{
          |  "name": "dummyName",
          |  "version": 1,
          |  "description": null,
          |  "hdfsPath": "/dummy/path",
          |  "hdfsPublishPath": "/dummy/publish/path",
          |  "schemaName": "dummySchema",
          |  "schemaVersion": 1,
          |  "dateCreated": "2017-12-04T16:19:17Z",
          |  "userCreated": "dummyUser",
          |  "lastUpdated": "2017-12-04T16:19:17Z",
          |  "userUpdated": "dummyUser",
          |  "disabled": false,
          |  "dateDisabled": null,
          |  "userDisabled": null,
          |  "conformance": [],
          |  "parent": null,
          |  "schedule": null,
          |  "createdMessage": {
          |    "menasRef": {
          |      "collection": null,
          |      "name": "dummyName",
          |      "version": 1
          |    },
          |    "updatedBy": "dummyUser",
          |    "updated": "2017-12-04T16:19:17Z",
          |    "changes": [
          |      {
          |        "field": "",
          |        "oldValue": null,
          |        "newValue": null,
          |        "message": "Dataset dummyName created."
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin
      val dataset = DatasetFactory.getDummyDataset()

      "serializing" in {
        val result = JsonSerializer.toJson(dataset)
        result should equal(datasetJson)(after being whiteSpaceNormalised)
      }
      "deserializing" in {
        val result = JsonSerializer.fromJson[Dataset](datasetJson)
        result should matchTo(dataset)
      }
    }

    "handle MappingTables" when {
      val mappingTableJson =
        """
          |{
          |  "name": "dummyName",
          |  "version": 1,
          |  "description": null,
          |  "hdfsPath": "/dummy/path",
          |  "schemaName": "dummySchema",
          |  "schemaVersion": 1,
          |  "defaultMappingValue": [],
          |  "dateCreated": "2017-12-04T16:19:17Z",
          |  "userCreated": "dummyUser",
          |  "lastUpdated": "2017-12-04T16:19:17Z",
          |  "userUpdated": "dummyUser",
          |  "disabled": false,
          |  "dateDisabled": null,
          |  "userDisabled": null,
          |  "parent": null,
          |  "createdMessage": {
          |    "menasRef": {
          |      "collection": null,
          |      "name": "dummyName",
          |      "version": 1
          |    },
          |    "updatedBy": "dummyUser",
          |    "updated": "2017-12-04T16:19:17Z",
          |    "changes": [
          |      {
          |        "field": "",
          |        "oldValue": null,
          |        "newValue": null,
          |        "message": "Mapping Table dummyName created."
          |      }
          |    ]
          |  },
          |  "defaultMappingValues": {}
          |}
          |""".stripMargin
      val mappingTable = MappingTableFactory.getDummyMappingTable()

      "serializing" in {
        val result = JsonSerializer.toJson(mappingTable)
        result should equal(mappingTableJson)(after being whiteSpaceNormalised)
      }
      "deserializing" in {
        val result = JsonSerializer.fromJson[MappingTable](mappingTableJson)
        result should matchTo(mappingTable)
      }
    }

    "handle Schemas" when {
      val schemaJson =
        """
          |{
          |  "name": "dummyName",
          |  "version": 1,
          |  "description": null,
          |  "dateCreated": "2017-12-04T16:19:17Z",
          |  "userCreated": "dummyUser",
          |  "lastUpdated": "2017-12-04T16:19:17Z",
          |  "userUpdated": "dummyUser",
          |  "disabled": false,
          |  "dateDisabled": null,
          |  "userDisabled": null,
          |  "fields": [],
          |  "parent": null,
          |  "createdMessage": {
          |    "menasRef": {
          |      "collection": null,
          |      "name": "dummyName",
          |      "version": 1
          |    },
          |    "updatedBy": "dummyUser",
          |    "updated": "2017-12-04T16:19:17Z",
          |    "changes": [
          |      {
          |        "field": "",
          |        "oldValue": null,
          |        "newValue": null,
          |        "message": "Schema dummyName created."
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin
      val schema = SchemaFactory.getDummySchema()

      "serializing" in {
        val result = JsonSerializer.toJson(schema)
        result should equal(schemaJson)(after being whiteSpaceNormalised)
      }
      "deserializing" in {
        val result = JsonSerializer.fromJson[Schema](schemaJson)
        result should matchTo(schema)
      }
    }

    "handle Runs" when {
      val uniqueId = "2f7ac049-7c78-4da0-9347-6096bf341618"
      val runJson =
        s"""
           |{
           |  "uniqueId": "$uniqueId",
           |  "runId": 1,
           |  "dataset": "dummyDataset",
           |  "datasetVersion": 1,
           |  "splineRef": {
           |    "sparkApplicationId": "dummySparkApplicationId",
           |    "outputPath": "dummyOutputPath"
           |  },
           |  "startDateTime": "04-12-2017 16:19:17 +0200",
           |  "runStatus": {
           |    "status": {
           |      "enumClass": "za.co.absa.atum.model.RunState",
           |      "value": "allSucceeded"
           |    },
           |    "error": null
           |  },
           |  "controlMeasure": {
           |    "metadata": {
           |      "sourceApplication": "dummySourceApplication",
           |      "country": "dummyCountry",
           |      "historyType": "dummyHistoryType",
           |      "dataFilename": "dummyDataFilename",
           |      "sourceType": "dummySourceType",
           |      "version": 1,
           |      "informationDate": "04-12-2017 16:19:17 +0200",
           |      "additionalInfo": {}
           |    },
           |    "runUniqueId": "$uniqueId",
           |    "checkpoints": []
           |  }
           |}
           |""".stripMargin

      val run = RunFactory.getDummyRun(
        uniqueId = Some(uniqueId),
        controlMeasure = RunFactory.getDummyControlMeasure(
          runUniqueId = Some(uniqueId)
        ))

      "serializing" in {
        val result = JsonSerializer.toJson(run)
        result should equal(runJson)(after being whiteSpaceNormalised)
      }
      "deserializing" in {
        val result = JsonSerializer.fromJson[Run](runJson)
        result should be(run)
      }
    }

    "keep JSON unchanged when desrializing to String" in {
      val expected = """{"test":"json"}"""
      val result = JsonSerializer.fromJson[String](expected)
      result should be(expected)
    }
  }

  val whiteSpaceNormalised: Uniformity[String] =
    new AbstractStringUniformity {
      def normalized(s: String): String = s.replaceAll("\\s+", "").trim

      override def toString: String = "whiteSpaceNormalised"
    }
}