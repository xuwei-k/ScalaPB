package com.trueaccord.scalapb.grpc

import com.google.protobuf.Descriptors.ServiceDescriptor

abstract class ServiceCompanion[A <: ServiceCompanion[A]] {
  def descriptor: ServiceDescriptor
}
