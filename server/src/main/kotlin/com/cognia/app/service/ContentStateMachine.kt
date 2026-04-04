package com.cognia.app.service

object ContentStateMachine {
    private val validTransitions: Map<String, Set<String>> = mapOf(
        "DRAFT" to setOf("PENDING_REVIEW", "PUBLISHED"),
        "PENDING_REVIEW" to setOf("PUBLISHED", "REJECTED"),
        "REJECTED" to setOf("DRAFT"),
        "PUBLISHED" to setOf("ARCHIVED"),
        "ARCHIVED" to emptySet()
    )

    fun canTransition(from: String, to: String): Boolean {
        return validTransitions[from]?.contains(to) ?: false
    }

    fun validateTransition(from: String, to: String) {
        if (!canTransition(from, to)) {
            throw InvalidStateTransitionException(from, to)
        }
    }
}

class InvalidStateTransitionException(from: String, to: String) :
    RuntimeException("Invalid state transition: $from -> $to")
