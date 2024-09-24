package dev.samtrafton.contactbook

import kotlinx.serialization.Serializable
// The annotation allows us to use the kotlin serialization package
@Serializable
data class Contact(
    var id: Int,
    var name: String,
    var phoneNumber: String,
    var company: String
)
