package com.cognia.app.service

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class ContentStateMachineTest {

    @Test
    fun `DRAFT can transition to PENDING_REVIEW`() {
        assertTrue(ContentStateMachine.canTransition("DRAFT", "PENDING_REVIEW"))
    }

    @Test
    fun `DRAFT can transition to PUBLISHED`() {
        assertTrue(ContentStateMachine.canTransition("DRAFT", "PUBLISHED"))
    }

    @Test
    fun `PENDING_REVIEW can transition to PUBLISHED`() {
        assertTrue(ContentStateMachine.canTransition("PENDING_REVIEW", "PUBLISHED"))
    }

    @Test
    fun `PENDING_REVIEW can transition to REJECTED`() {
        assertTrue(ContentStateMachine.canTransition("PENDING_REVIEW", "REJECTED"))
    }

    @Test
    fun `REJECTED can transition to DRAFT`() {
        assertTrue(ContentStateMachine.canTransition("REJECTED", "DRAFT"))
    }

    @Test
    fun `PUBLISHED can transition to ARCHIVED`() {
        assertTrue(ContentStateMachine.canTransition("PUBLISHED", "ARCHIVED"))
    }

    @Test
    fun `ARCHIVED cannot transition to any state`() {
        assertFalse(ContentStateMachine.canTransition("ARCHIVED", "DRAFT"))
        assertFalse(ContentStateMachine.canTransition("ARCHIVED", "PENDING_REVIEW"))
        assertFalse(ContentStateMachine.canTransition("ARCHIVED", "PUBLISHED"))
        assertFalse(ContentStateMachine.canTransition("ARCHIVED", "REJECTED"))
    }

    @Test
    fun `DRAFT cannot transition to REJECTED`() {
        assertFalse(ContentStateMachine.canTransition("DRAFT", "REJECTED"))
    }

    @Test
    fun `DRAFT cannot transition to ARCHIVED`() {
        assertFalse(ContentStateMachine.canTransition("DRAFT", "ARCHIVED"))
    }

    @Test
    fun `PUBLISHED cannot transition to DRAFT`() {
        assertFalse(ContentStateMachine.canTransition("PUBLISHED", "DRAFT"))
    }

    @Test
    fun `REJECTED cannot transition to PUBLISHED`() {
        assertFalse(ContentStateMachine.canTransition("REJECTED", "PUBLISHED"))
    }

    @Test
    fun `validateTransition succeeds for valid transition`() {
        ContentStateMachine.validateTransition("DRAFT", "PENDING_REVIEW")
        // No exception means success
    }

    @Test
    fun `validateTransition throws for invalid transition`() {
        assertFailsWith<InvalidStateTransitionException> {
            ContentStateMachine.validateTransition("DRAFT", "REJECTED")
        }
    }

    @Test
    fun `validateTransition throws for unknown source state`() {
        assertFailsWith<InvalidStateTransitionException> {
            ContentStateMachine.validateTransition("UNKNOWN", "DRAFT")
        }
    }
}
