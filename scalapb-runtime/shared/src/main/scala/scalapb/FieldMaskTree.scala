package scalapb

import com.google.protobuf.field_mask.FieldMask
import scala.collection.mutable
import scala.collection.JavaConverters._
import FieldMaskTree.Node

object FieldMaskTree {
  private val FIELD_PATH_SEPARATOR_REGEX = "\\."

  private[scalapb] case class Node(
    children: mutable.TreeMap[String, Node] = mutable.TreeMap.empty[String, Node]
  )
}


final case class FieldMaskTree private[scalapb] (
  private val root: Node = Node()
) {

  /**
    * Creates a FieldMaskTree for a given FieldMask.
    */
  def this(mask: FieldMask) {
    this()
    mergeFromFieldMask(mask)
  }

  override def toString: String = ??? //FieldMaskUtil.toString(toFieldMask)

  private[scalapb] def addFieldPath(path: String): this.type = {
    val parts = path.split(FieldMaskTree.FIELD_PATH_SEPARATOR_REGEX)
    if (parts.isEmpty) {
      this
    } else {
      var node: Node = root
      var createNewBranch = false
      // Find the matching node in the tree.
      for (part <- parts) { // Check whether the path matches an existing leaf node.
        if (!createNewBranch && (node ne root) && node.children.isEmpty) { // The path to add is a sub-path of an existing leaf node.
          return this
        }
        node = node.children.getOrElse(
          part,
          {
            createNewBranch = true
            val tmp = FieldMaskTree.Node()
            node.children.put(part, tmp)
            tmp
          }
        )
      }
      // Turn the matching node into a leaf node (i.e., remove sub-paths).
      node.children.clear()
      this
    }
  }

  /**
    * Merges all field paths in a FieldMask into this tree.
    */
  private[scalapb] def mergeFromFieldMask(mask: FieldMask): this.type = {
    mask.paths.foreach(addFieldPath)
    this
  }

  /**
    * Converts this tree to a FieldMask.
    */
  private[scalapb] def toFieldMask: FieldMask = {
    if (root.children.isEmpty) {
      FieldMask.defaultInstance
    } else {
      val paths = new java.util.ArrayList[String]
      getFieldPaths(root, "", paths)
      FieldMask(paths.asScala)
    }
  }

  /**
    * Gathers all field paths in a sub-tree.
    */
  private def getFieldPaths(node: FieldMaskTree.Node, path: String, paths: java.util.List[String]): Unit = {
    if (node.children.isEmpty) {
      paths.add(path)
    } else {
      for ((key, value) <- node.children) {
        val childPath = if (path.isEmpty) {
          key
        } else {
          path + "." + key
        }
        getFieldPaths(value, childPath, paths)
      }
    }
  }

  /**
    * Adds the intersection of this tree with the given `path` to `output`.
    */
  private[scalapb] def intersectFieldPath(path: String, output: FieldMaskTree): Unit = {
    if (root.children.nonEmpty) {
      val parts = path.split(FieldMaskTree.FIELD_PATH_SEPARATOR_REGEX)
      if (parts.nonEmpty) {
        var node = root
        for (part <- parts) {
          if ((node ne root) && node.children.isEmpty) { // The given path is a sub-path of an existing leaf node in the tree.
            output.addFieldPath(path)
            return
          }
          node.children.get(part) match {
            case Some(x) =>
              node = x
            case None =>
              return
          }
        }
        // We found a matching node for the path. All leaf children of this matching
        // node is in the intersection.
        val paths = new java.util.ArrayList[String]
        getFieldPaths(node, path, paths)
        paths.asScala.foreach(output.addFieldPath(_))
      }
    }
  }
}
