package com.acme.commitviewer.util

import java.security.MessageDigest

object MD5 {

    val digest: String => String = (str: String) => {
      val digest = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"))
      digest.map("%02x".format(_)).mkString //converts to hexadecimal string
    }
}
