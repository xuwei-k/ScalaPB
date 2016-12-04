// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.api



/** Method represents a method of an api.
  *
  * @param name
  *   The simple name of this method.
  * @param requestTypeUrl
  *   A URL of the input message type.
  * @param requestStreaming
  *   If true, the request is streamed.
  * @param responseTypeUrl
  *   The URL of the output message type.
  * @param responseStreaming
  *   If true, the response is streamed.
  * @param options
  *   Any metadata attached to the method.
  * @param syntax
  *   The source syntax of this method.
  */
@SerialVersionUID(0L)
final case class Method(
    name: String = "",
    requestTypeUrl: String = "",
    requestStreaming: Boolean = false,
    responseTypeUrl: String = "",
    responseStreaming: Boolean = false,
    options: scala.collection.Seq[com.google.protobuf.`type`.OptionProto] = Nil,
    syntax: com.google.protobuf.`type`.Syntax = com.google.protobuf.`type`.Syntax.SYNTAX_PROTO2
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[Method] with com.trueaccord.lenses.Updatable[Method] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (name != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, name) }
      if (requestTypeUrl != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, requestTypeUrl) }
      if (requestStreaming != false) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(3, requestStreaming) }
      if (responseTypeUrl != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(4, responseTypeUrl) }
      if (responseStreaming != false) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(5, responseStreaming) }
      options.foreach(options => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(options.serializedSize) + options.serializedSize)
      if (syntax != com.google.protobuf.`type`.Syntax.SYNTAX_PROTO2) { __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(7, syntax.value) }
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
      {
        val __v = name
        if (__v != "") {
          _output__.writeString(1, __v)
        }
      };
      {
        val __v = requestTypeUrl
        if (__v != "") {
          _output__.writeString(2, __v)
        }
      };
      {
        val __v = requestStreaming
        if (__v != false) {
          _output__.writeBool(3, __v)
        }
      };
      {
        val __v = responseTypeUrl
        if (__v != "") {
          _output__.writeString(4, __v)
        }
      };
      {
        val __v = responseStreaming
        if (__v != false) {
          _output__.writeBool(5, __v)
        }
      };
      options.foreach { __v =>
        _output__.writeTag(6, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      {
        val __v = syntax
        if (__v != com.google.protobuf.`type`.Syntax.SYNTAX_PROTO2) {
          _output__.writeEnum(7, __v.value)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.api.Method = {
      var __name = this.name
      var __requestTypeUrl = this.requestTypeUrl
      var __requestStreaming = this.requestStreaming
      var __responseTypeUrl = this.responseTypeUrl
      var __responseStreaming = this.responseStreaming
      val __options = (scala.collection.immutable.Vector.newBuilder[com.google.protobuf.`type`.OptionProto] ++= this.options)
      var __syntax = this.syntax
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __name = _input__.readString()
          case 18 =>
            __requestTypeUrl = _input__.readString()
          case 24 =>
            __requestStreaming = _input__.readBool()
          case 34 =>
            __responseTypeUrl = _input__.readString()
          case 40 =>
            __responseStreaming = _input__.readBool()
          case 50 =>
            __options += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.`type`.OptionProto.defaultInstance)
          case 56 =>
            __syntax = com.google.protobuf.`type`.Syntax.fromValue(_input__.readEnum())
          case tag => _input__.skipField(tag)
        }
      }
      com.google.protobuf.api.Method(
          name = __name,
          requestTypeUrl = __requestTypeUrl,
          requestStreaming = __requestStreaming,
          responseTypeUrl = __responseTypeUrl,
          responseStreaming = __responseStreaming,
          options = __options.result(),
          syntax = __syntax
      )
    }
    def withName(__v: String): Method = copy(name = __v)
    def withRequestTypeUrl(__v: String): Method = copy(requestTypeUrl = __v)
    def withRequestStreaming(__v: Boolean): Method = copy(requestStreaming = __v)
    def withResponseTypeUrl(__v: String): Method = copy(responseTypeUrl = __v)
    def withResponseStreaming(__v: Boolean): Method = copy(responseStreaming = __v)
    def clearOptions = copy(options = scala.collection.Seq.empty)
    def addOptions(__vs: com.google.protobuf.`type`.OptionProto*): Method = addAllOptions(__vs)
    def addAllOptions(__vs: TraversableOnce[com.google.protobuf.`type`.OptionProto]): Method = copy(options = options ++ __vs)
    def withOptions(__v: scala.collection.Seq[com.google.protobuf.`type`.OptionProto]): Method = copy(options = __v)
    def withSyntax(__v: com.google.protobuf.`type`.Syntax): Method = copy(syntax = __v)
    def getField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = name
          if (__t != "") __t else null
        }
        case 2 => {
          val __t = requestTypeUrl
          if (__t != "") __t else null
        }
        case 3 => {
          val __t = requestStreaming
          if (__t != false) __t else null
        }
        case 4 => {
          val __t = responseTypeUrl
          if (__t != "") __t else null
        }
        case 5 => {
          val __t = responseStreaming
          if (__t != false) __t else null
        }
        case 6 => options
        case 7 => {
          val __t = syntax.valueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
      }
    }
    override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.api.Method
}

object Method extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.api.Method] with _root_.scala.Function7[String, String, Boolean, String, Boolean, scala.collection.Seq[com.google.protobuf.`type`.OptionProto], com.google.protobuf.`type`.Syntax, com.google.protobuf.api.Method] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.api.Method] with _root_.scala.Function7[String, String, Boolean, String, Boolean, scala.collection.Seq[com.google.protobuf.`type`.OptionProto], com.google.protobuf.`type`.Syntax, com.google.protobuf.api.Method] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.api.Method = {
    require(__fieldsMap.keys.forall(_.getContainingType() == descriptor), "FieldDescriptor does not match message type.")
    val __fields = descriptor.getFields
    com.google.protobuf.api.Method(
      __fieldsMap.getOrElse(__fields.get(0), "").asInstanceOf[String],
      __fieldsMap.getOrElse(__fields.get(1), "").asInstanceOf[String],
      __fieldsMap.getOrElse(__fields.get(2), false).asInstanceOf[Boolean],
      __fieldsMap.getOrElse(__fields.get(3), "").asInstanceOf[String],
      __fieldsMap.getOrElse(__fields.get(4), false).asInstanceOf[Boolean],
      __fieldsMap.getOrElse(__fields.get(5), Nil).asInstanceOf[scala.collection.Seq[com.google.protobuf.`type`.OptionProto]],
      com.google.protobuf.`type`.Syntax.fromValue(__fieldsMap.getOrElse(__fields.get(6), com.google.protobuf.`type`.Syntax.SYNTAX_PROTO2.valueDescriptor).asInstanceOf[_root_.com.google.protobuf.Descriptors.EnumValueDescriptor].getNumber)
    )
  }
  def descriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ApiProto.descriptor.getMessageTypes.get(1)
  def messageCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = {
    require(__field.getContainingType() == descriptor, "FieldDescriptor does not match message type.")
    var __out: _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = null
    __field.getNumber match {
      case 6 => __out = com.google.protobuf.`type`.OptionProto
    }
  __out
  }
  def enumCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = {
    require(__field.getContainingType() == descriptor, "FieldDescriptor does not match message type.")
    __field.getNumber match {
      case 7 => com.google.protobuf.`type`.Syntax
    }
  }
  lazy val defaultInstance = com.google.protobuf.api.Method(
  )
  implicit class MethodLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.api.Method]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.api.Method](_l) {
    def name: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.name)((c_, f_) => c_.copy(name = f_))
    def requestTypeUrl: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.requestTypeUrl)((c_, f_) => c_.copy(requestTypeUrl = f_))
    def requestStreaming: _root_.com.trueaccord.lenses.Lens[UpperPB, Boolean] = field(_.requestStreaming)((c_, f_) => c_.copy(requestStreaming = f_))
    def responseTypeUrl: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.responseTypeUrl)((c_, f_) => c_.copy(responseTypeUrl = f_))
    def responseStreaming: _root_.com.trueaccord.lenses.Lens[UpperPB, Boolean] = field(_.responseStreaming)((c_, f_) => c_.copy(responseStreaming = f_))
    def options: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.collection.Seq[com.google.protobuf.`type`.OptionProto]] = field(_.options)((c_, f_) => c_.copy(options = f_))
    def syntax: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.`type`.Syntax] = field(_.syntax)((c_, f_) => c_.copy(syntax = f_))
  }
  final val NAME_FIELD_NUMBER = 1
  final val REQUEST_TYPE_URL_FIELD_NUMBER = 2
  final val REQUEST_STREAMING_FIELD_NUMBER = 3
  final val RESPONSE_TYPE_URL_FIELD_NUMBER = 4
  final val RESPONSE_STREAMING_FIELD_NUMBER = 5
  final val OPTIONS_FIELD_NUMBER = 6
  final val SYNTAX_FIELD_NUMBER = 7
}
