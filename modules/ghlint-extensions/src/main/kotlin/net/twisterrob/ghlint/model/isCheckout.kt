package net.twisterrob.ghlint.model

public val Step.isCheckout: Boolean
	get() {
		val uses = this as? Step.Uses ?: return false
		return uses.uses.action == "actions/checkout"
	}
