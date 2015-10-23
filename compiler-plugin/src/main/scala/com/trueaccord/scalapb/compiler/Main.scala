package com.trueaccord.scalapb.compiler

import java.io.BufferedOutputStream

object Main extends App {
  val response = Process.runWithInputStream(System.in)
  Console.withOut(new BufferedOutputStream(System.out)){
    System.out.write(response.toByteArray)
    System.out.flush()
  }
}
