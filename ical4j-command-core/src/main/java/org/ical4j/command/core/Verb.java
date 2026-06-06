/*
 * Copyright (c) 2025, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ical4j.command.core;

/**
 * The canonical set of action verbs shared by all command surfaces (CLI, MCP, agent).
 * <p>
 * Surfaces MUST name operations from this set and MUST NOT introduce synonyms for an
 * existing verb. Notably {@code create}/{@code add} are standardised on {@link #ADD} and
 * {@code delete}/{@code remove} on {@link #REMOVE}; the iTIP methods publish/request/reply
 * are specialised forms of {@link #SEND} (see {@link SendMethod}), not independent verbs.
 */
public enum Verb {

    /** Return many items of a type within a locus. */
    LIST,

    /** Return one item of a type, addressed by identifier. */
    GET,

    /** Synthesise a new item from a strategy/template (e.g. an event from a meeting template). */
    GENERATE,

    /** Place an item into a locus, whether newly made or pre-existing. */
    ADD,

    /** Modify an existing item in place. */
    UPDATE,

    /** Take an item out of a locus. */
    REMOVE,

    /** Duplicate an item into a (possibly different) locus. */
    COPY,

    /** Relocate an item from one locus to another. */
    MOVE,

    /** Derive a subset of a type within a locus by expression. */
    FILTER,

    /** Check an item or locus for conformance. */
    VALIDATE,

    /** Read a serialization family into a locus. */
    IMPORT,

    /** Write a serialization family out of a locus. */
    EXPORT,

    /** Transmit items over a channel (see {@link SendMethod} for iTIP specialisations). */
    SEND,

    /** Accept incoming items over a channel. */
    RECEIVE
}
