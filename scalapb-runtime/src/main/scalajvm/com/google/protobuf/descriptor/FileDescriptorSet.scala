// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package com.google.protobuf.descriptor
import _root_.scalapb.internal.compat.JavaConverters._

/** The protocol compiler can output a FileDescriptorSet containing the .proto
  * files it parses.
  */
@SerialVersionUID(0L)
final case class FileDescriptorSet(
    file: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto] = _root_.scala.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[FileDescriptorSet] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      file.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      __size += unknownFields.serializedSize
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      file.foreach { __v =>
        val __m = __v
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      unknownFields.writeTo(_output__)
    }
    def clearFile = copy(file = _root_.scala.Seq.empty)
    def addFile(__vs: com.google.protobuf.descriptor.FileDescriptorProto*): FileDescriptorSet = addAllFile(__vs)
    def addAllFile(__vs: Iterable[com.google.protobuf.descriptor.FileDescriptorProto]): FileDescriptorSet = copy(file = file ++ __vs)
    def withFile(__v: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]): FileDescriptorSet = copy(file = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => file
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(file.iterator.map(_.toPMessage).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.descriptor.FileDescriptorSet
}

object FileDescriptorSet extends scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.FileDescriptorSet] with scalapb.JavaProtoSupport[com.google.protobuf.descriptor.FileDescriptorSet, com.google.protobuf.DescriptorProtos.FileDescriptorSet] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.FileDescriptorSet] with scalapb.JavaProtoSupport[com.google.protobuf.descriptor.FileDescriptorSet, com.google.protobuf.DescriptorProtos.FileDescriptorSet] = this
  def toJavaProto(scalaPbSource: com.google.protobuf.descriptor.FileDescriptorSet): com.google.protobuf.DescriptorProtos.FileDescriptorSet = {
    val javaPbOut = com.google.protobuf.DescriptorProtos.FileDescriptorSet.newBuilder
    javaPbOut.addAllFile(_root_.scalapb.internal.compat.toIterable(scalaPbSource.file.iterator.map(com.google.protobuf.descriptor.FileDescriptorProto.toJavaProto)).asJava)
    javaPbOut.build
  }
  def fromJavaProto(javaPbSource: com.google.protobuf.DescriptorProtos.FileDescriptorSet): com.google.protobuf.descriptor.FileDescriptorSet = com.google.protobuf.descriptor.FileDescriptorSet(
    file = javaPbSource.getFileList.asScala.iterator.map(com.google.protobuf.descriptor.FileDescriptorProto.fromJavaProto).toSeq
  )
  def merge(`_message__`: com.google.protobuf.descriptor.FileDescriptorSet, `_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.descriptor.FileDescriptorSet = {
    val __file = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.FileDescriptorProto] ++= `_message__`.file)
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __file += _root_.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.FileDescriptorProto.defaultInstance)
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder(_message__.unknownFields)
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    com.google.protobuf.descriptor.FileDescriptorSet(
        file = __file.result(),
        unknownFields = if (_unknownFields__ == null) _message__.unknownFields else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.descriptor.FileDescriptorSet] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.google.protobuf.descriptor.FileDescriptorSet(
        file = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = DescriptorProtoCompanion.javaDescriptor.getMessageTypes().get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = DescriptorProtoCompanion.scalaDescriptor.messages(0)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = com.google.protobuf.descriptor.FileDescriptorProto
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.google.protobuf.descriptor.FileDescriptorSet(
    file = _root_.scala.Seq.empty
  )
  implicit class FileDescriptorSetLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.descriptor.FileDescriptorSet]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.google.protobuf.descriptor.FileDescriptorSet](_l) {
    def file: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]] = field(_.file)((c_, f_) => c_.copy(file = f_))
  }
  final val FILE_FIELD_NUMBER = 1
  def of(
    file: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]
  ): _root_.com.google.protobuf.descriptor.FileDescriptorSet = _root_.com.google.protobuf.descriptor.FileDescriptorSet(
    file
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[google.protobuf.FileDescriptorSet])
}
