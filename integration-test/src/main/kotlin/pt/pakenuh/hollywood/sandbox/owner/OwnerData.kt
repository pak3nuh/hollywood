package pt.pakenuh.hollywood.sandbox.owner

data class Owner(val ownerId: OwnerId, val creditCard: CreditCard)
data class OwnerId(val id: String, val name: String)
data class CreditCard(val number: String, val plafond: Int)
