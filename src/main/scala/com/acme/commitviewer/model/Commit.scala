package com.acme.commitviewer.model

import java.util.Date

case class Commit(ref: String, author: String, date: Date, description: String)
