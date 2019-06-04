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

package za.co.absa.enceladus.migrations.framework.migration

import org.apache.log4j.{LogManager, Logger}
import za.co.absa.enceladus.migrations.framework.MigrationUtils
import za.co.absa.enceladus.migrations.framework.dao.DocumentDb

import scala.collection.mutable.ListBuffer

/**
  * A CollectionMigration represents an entity that provides ability to add, rename and remove collections
  *
  * In order to create a collection migration you need to extend from this trait and provide the requested
  * collection changes:
  *
  * {{{
  *   class MigrationTo1 extends MigrationBase with CollectionMigration {
  *
  *     addCollection("collection1_name")
  *     addCollection("collection2_name")
  *     addCollection("collection3_name")
  *
  *     removeCollection("collection4_name")
  *     removeCollection("collection5_name")
  *     removeCollection("collection6_name")
  *   }
  * }}}
  */
trait CollectionMigration extends Migration {

  type Index = (String, Seq[String])

  private val log: Logger = LogManager.getLogger("CollectionMigration")

  /**
    * This method is used by derived classes to add new collection as a step of migration process.
    *
    * @param collectionName A collection name to be added
    */
  def createCollection(collectionName: String): Unit = {
    if (collectionsToCreate.contains(collectionName)) {
      throw new IllegalArgumentException(s"Collection '$collectionName' is already added.")
    }
    if (collectionsToDrop.contains(collectionName)) {
      throw new IllegalArgumentException(s"Cannot both add and remove '$collectionName' as a migration step.")
    }
    collectionsToCreate += collectionName
  }

  /**
    * This method is used by derived classes to remove a collection as a step of migration process.
    *
    * @param collectionName A collection name to be added
    */
  def dropCollection(collectionName: String): Unit = {
    if (collectionsToDrop.contains(collectionName)) {
      throw new IllegalArgumentException(s"Collection '$collectionName' is already in the removal list.")
    }
    if (collectionsToCreate.contains(collectionName)) {
      throw new IllegalArgumentException(s"Cannot both add and remove '$collectionName' as a migration step.")
    }
    collectionsToDrop += collectionName
  }

  /**
    * This method is used by derived classes to rename a collection as a step of migration process.
    *
    * @param oldName A collection to be renamed
    * @param newName A new name for the collection
    */
  def renameCollection(oldName: String, newName: String): Unit = {
    if (collectionsToDrop.contains(oldName)) {
      throw new IllegalArgumentException(s"Collection '$oldName' is in the removal list.")
    }
    if (collectionsToCreate.contains(oldName)) {
      throw new IllegalArgumentException(s"Collection '$oldName' is in the list of new collections. Cannot rename it.")
    }
    collectionsToRename += oldName -> newName
  }

  /**
    * This method is used by derived classes to create an index on a collection.
    *
    * @param collectionName A collection for setting up an index
    * @param fields         A list of fields that the index should contain
    */
  def createIndex(collectionName: String, fields: Seq[String]): Unit = {
    indexesToCreate.append((collectionName, fields))
  }

  /**
    * This method is used by derived classes to drop an index on a collection.
    *
    * @param collectionName A collection for dropping up an index
    * @param fields         A list of fields that the index should contain
    */
  def dropIndex(collectionName: String, fields: Seq[String]): Unit = {
    indexesToDrop.append((collectionName, fields))
  }

  /** Returns a list of collections to be added during the migration */
  def getCollectionsToAdd: List[String] = collectionsToCreate.toList

  /** Returns a list of collections to be removed during the migration */
  def getCollectionsToRemove: List[String] = collectionsToDrop.toList

  /** Returns a list of collections to be removed during the migration */
  def getCollectionsToRename: List[(String, String)] = collectionsToRename.toList

  /**
    * If a migration adds or removes collections it should provide a new list of collections based
    * on the list of collections available for he previous version of a database.
    */
  override def applyCollectionChanges(collections: List[String]): List[String] = {
    var newCollections = collections
    for (c <- getCollectionsToAdd) {
      if (!newCollections.contains(c)) {
        newCollections = newCollections :+ c
      }
    }
    val collectionsToRemove = getCollectionsToRemove
    newCollections = newCollections.filterNot(c => collectionsToRemove.contains(c))

    val renameMap = collectionsToRename.toMap
    newCollections.map(collectionName => {
      renameMap.getOrElse(collectionName, collectionName)
    })
  }

  /**
    * Executes a migration on a given database and a list of collection names.
    */
  abstract override def execute(db: DocumentDb, collectionNames: Seq[String]): Unit = {
    super.execute(db, collectionNames)
    applyCreateCollections(db)
    applyDropCollections(db)
    applyRenameCollection(db)
    applyIndexDrop(db)
    applyIndexCreate(db)
  }

  private def applyCreateCollections(db: DocumentDb): Unit = {
    collectionsToCreate.foreach(c => {
      val newCollection = MigrationUtils.getVersionedCollectionName(c, targetVersion)
      if (db.isCollectionExists(newCollection)) {
        log.info(s"Dropping existing collection $newCollection")
        db.dropCollection(newCollection)
      }
      log.info(s"Adding new collection $newCollection")
      db.createCollection(newCollection)
    })
  }

  private def applyDropCollections(db: DocumentDb): Unit = {
    collectionsToDrop.foreach(c => {
      val collection = MigrationUtils.getVersionedCollectionName(c, targetVersion)
      log.info(s"Removing collection $collection")
      db.dropCollection(collection)
    })
  }

  private def applyRenameCollection(db: DocumentDb): Unit = {
    collectionsToRename.foreach {
      case (oldName, newName) =>
        val oldVersionedCollection = MigrationUtils.getVersionedCollectionName(oldName, targetVersion)
        val newVersionedCollection = MigrationUtils.getVersionedCollectionName(newName, targetVersion)
        log.info(s"Renaming collection $oldVersionedCollection -> $newVersionedCollection")
        db.renameCollection(oldVersionedCollection, newVersionedCollection)
    }
  }

  private def applyIndexCreate(db: DocumentDb): Unit = {
    indexesToCreate.foreach {
      case (collectionName, keys) =>
        val collection = MigrationUtils.getVersionedCollectionName(collectionName, targetVersion)
        log.info(s"Creating index '${keys.mkString(",")}' in '$collection'")
        db.createIndex(collection, keys)
    }
  }

  private def applyIndexDrop(db: DocumentDb): Unit = {
    indexesToDrop.foreach {
      case (collectionName, keys) =>
        val collection = MigrationUtils.getVersionedCollectionName(collectionName, targetVersion)
        log.info(s"Removing index '${keys.mkString(",")}' from '$collection'")
        db.dropIndex(collection, keys)
    }
  }

  /**
    * Validate the possibility of running a migration given a list of collection names.
    */
  abstract override def validate(collectionNames: Seq[String]): Unit = {
    super.validate(collectionNames)
    collectionsToCreate.foreach(collectionToMigrate =>
      if (collectionNames.contains(collectionToMigrate)) {
        throw new IllegalStateException(
          s"Attempt to add a collection that already exists in db version ${targetVersion - 1}: $collectionToMigrate.")
      }
    )
    collectionsToDrop.foreach(collectionToMigrate =>
      if (!collectionNames.contains(collectionToMigrate)) {
        throw new IllegalStateException(
          s"Attempt to drop a collection that does not exist in db version ${targetVersion - 1}: $collectionToMigrate.")
      }
    )
    collectionsToRename.foreach {
      case (oldName, newName) =>
        if (!collectionNames.contains(oldName)) {
          throw new IllegalStateException(
            s"Attempt to rename a collection that does not exist: $oldName.")
        }
        if (collectionNames.contains(newName)) {
          throw new IllegalStateException(
            s"Attempt to rename a collection to a one that already exists in db version ${targetVersion - 1}: " +
              s"$newName.")
        }
        if (collectionsToCreate.contains(oldName)) {
          throw new IllegalStateException(
            s"Cannot add and rename a collection as a part of single migration in db version ${targetVersion - 1}: " +
              s"$oldName.")
        }
        if (collectionsToCreate.contains(newName)) {
          throw new IllegalStateException(
            s"Cannot add and rename a collection as a part of single migration in db version ${targetVersion - 1}: " +
              s"$newName.")
        }
        if (collectionsToDrop.contains(oldName)) {
          throw new IllegalStateException(
            s"Cannot drop and rename a collection as a part of single migration in db version ${targetVersion - 1}: " +
              s"$oldName.")
        }
        if (collectionsToDrop.contains(newName)) {
          throw new IllegalStateException(
            s"Cannot drop and rename a collection as a part of single migration in db version ${targetVersion - 1}: " +
              s"$newName.")
        }
    }
  }

  override protected def validateMigration(): Unit = {
    if (targetVersion < 0) {
      throw new IllegalStateException("The target version of a CollectionMigration should be 0 or bigger.")
    }
  }

  private val collectionsToCreate = new ListBuffer[String]()
  private val collectionsToDrop = new ListBuffer[String]()
  private val collectionsToRename = new ListBuffer[(String, String)]()
  private val indexesToCreate = new ListBuffer[Index]()
  private val indexesToDrop = new ListBuffer[Index]()
}
