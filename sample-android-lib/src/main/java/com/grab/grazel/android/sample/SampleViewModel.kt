package com.grab.grazel.android.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(val firstName: String, val lastName: String) : Parcelable

class SampleViewModel {
    val user = User(firstName = "tom", lastName = "hanks")
}