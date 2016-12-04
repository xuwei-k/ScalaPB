// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package com.google.protobuf.descriptor

import scala.collection.JavaConverters._

/** @param uninterpretedOption
  *   The parser stores options it doesn't recognize here. See above.
  */
@SerialVersionUID(0L)
final case class OneofOptions(
    uninterpretedOption: scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption] = Nil
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[OneofOptions] with com.trueaccord.lenses.Updatable[OneofOptions] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      uninterpretedOption.foreach(uninterpretedOption => __size += 2 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(uninterpretedOption.serializedSize) + uninterpretedOption.serializedSize)
      __size
    }
    final override def serializedSize: Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
      uninterpretedOption.foreach { __v =>
        _output__.writeTag(999, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.descriptor.OneofOptions = {
      val __uninterpretedOption = (scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.UninterpretedOption] ++= this.uninterpretedOption)
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 7994 =>
            __uninterpretedOption += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.UninterpretedOption.defaultInstance)
          case tag => _input__.skipField(tag)
        }
      }
      com.google.protobuf.descriptor.OneofOptions(
          uninterpretedOption = __uninterpretedOption.result()
      )
    }
    def clearUninterpretedOption = copy(uninterpretedOption = scala.collection.Seq.empty)
    def addUninterpretedOption(__vs: com.google.protobuf.descriptor.UninterpretedOption*): OneofOptions = addAllUninterpretedOption(__vs)
    def addAllUninterpretedOption(__vs: TraversableOnce[com.google.protobuf.descriptor.UninterpretedOption]): OneofOptions = copy(uninterpretedOption = uninterpretedOption ++ __vs)
    def withUninterpretedOption(__v: scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]): OneofOptions = copy(uninterpretedOption = __v)
    def getField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 999 => uninterpretedOption
      }
    }
    override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.descriptor.OneofOptions
}

object OneofOptions extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.OneofOptions] with com.trueaccord.scalapb.JavaProtoSupport[com.google.protobuf.descriptor.OneofOptions, com.google.protobuf.DescriptorProtos.OneofOptions] with _root_.scala.Function1[scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption], com.google.protobuf.descriptor.OneofOptions] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.OneofOptions] with com.trueaccord.scalapb.JavaProtoSupport[com.google.protobuf.descriptor.OneofOptions, com.google.protobuf.DescriptorProtos.OneofOptions] with _root_.scala.Function1[scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption], com.google.protobuf.descriptor.OneofOptions] = this
  def toJavaProto(scalaPbSource: com.google.protobuf.descriptor.OneofOptions): com.google.protobuf.DescriptorProtos.OneofOptions = {
    val javaPbOut = com.google.protobuf.DescriptorProtos.OneofOptions.newBuilder
    javaPbOut.addAllUninterpretedOption(scalaPbSource.uninterpretedOption.map(com.google.protobuf.descriptor.UninterpretedOption.toJavaProto(_)).asJava)
    javaPbOut.build
  }
  def fromJavaProto(javaPbSource: com.google.protobuf.DescriptorProtos.OneofOptions): com.google.protobuf.descriptor.OneofOptions = com.google.protobuf.descriptor.OneofOptions(
    uninterpretedOption = javaPbSource.getUninterpretedOptionList.asScala.map(com.google.protobuf.descriptor.UninterpretedOption.fromJavaProto(_))
  )
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.descriptor.OneofOptions = {
    require(__fieldsMap.keys.forall(_.getContainingType() == descriptor), "FieldDescriptor does not match message type.")
    val __fields = descriptor.getFields
    com.google.protobuf.descriptor.OneofOptions(
      __fieldsMap.getOrElse(__fields.get(0), Nil).asInstanceOf[scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]]
    )
  }
  def descriptor: _root_.com.google.protobuf.Descriptors.Descriptor = DescriptorProtoCompanion.descriptor.getMessageTypes.get(12)
  def messageCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = {
    require(__field.getContainingType() == descriptor, "FieldDescriptor does not match message type.")
    var __out: _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = null
    __field.getNumber match {
      case 999 => __out = com.google.protobuf.descriptor.UninterpretedOption
    }
  __out
  }
  def enumCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__field)
  lazy val defaultInstance = com.google.protobuf.descriptor.OneofOptions(
  )
  implicit class OneofOptionsLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.descriptor.OneofOptions]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.descriptor.OneofOptions](_l) {
    def uninterpretedOption: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]] = field(_.uninterpretedOption)((c_, f_) => c_.copy(uninterpretedOption = f_))
  }
  final val UNINTERPRETED_OPTION_FIELD_NUMBER = 999
}
