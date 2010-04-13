/*
 * ExceptionHandlerTest.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.transform.action;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.flagstone.transform.coder.ActionDecoder;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.DecoderRegistry;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

public final class ExceptionHandlerTest {

    private static String variable = "var";
    private static List<Action> tryActions = new ArrayList<Action>();
    private static List<Action> catchActions = new ArrayList<Action>();
    private static List<Action> finalActions = new ArrayList<Action>();

    static {
        tryActions.add(BasicAction.ADD);
        tryActions.add(BasicAction.END);
        catchActions.add(BasicAction.SUBTRACT);
        catchActions.add(BasicAction.END);
        finalActions.add(BasicAction.MULTIPLY);
        finalActions.add(BasicAction.END);
    }

    private static final transient int type = ActionTypes.EXCEPTION_HANDLER;
    private transient ExceptionHandler fixture;

    private final transient byte[] encoded = new byte[] { (byte) type, 0x11,
            0x00, 0x07, 0x02, 0x00, 0x02, 0x00, 0x02, 0x00, 0x76, 0x61, 0x72,
            0x00, ActionTypes.ADD, ActionTypes.END, ActionTypes.SUBTRACT,
            ActionTypes.END, ActionTypes.MULTIPLY, ActionTypes.END, };

    @Test(expected = IllegalArgumentException.class)
    public void checkAddNullActionToTryBlock() {
        fixture = new ExceptionHandler(variable, tryActions, catchActions,
                finalActions);
        fixture.addToTry(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkAddNullActionToCatchBlock() {
        fixture = new ExceptionHandler(variable, tryActions, catchActions,
                finalActions);
        fixture.addToCatch(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkAddNullActionToFinallyBlock() {
        fixture = new ExceptionHandler(variable, tryActions, catchActions,
                finalActions);
        fixture.addToFinally(null);
    }

    @Test
    public void checkCopy() {
        fixture = new ExceptionHandler(variable, tryActions, catchActions,
                finalActions);
        final ExceptionHandler copy = fixture.copy();

        assertNotSame(fixture.getTryActions(), copy.getTryActions());
        assertNotSame(fixture.getCatchActions(), copy.getCatchActions());
        assertNotSame(fixture.getFinalActions(), copy.getFinalActions());
        assertEquals(fixture.toString(), copy.toString());
    }

    @Test
    public void encode() throws CoderException {
        final SWFEncoder encoder = new SWFEncoder(encoded.length);
        final Context context = new Context();

        fixture = new ExceptionHandler(variable, tryActions, catchActions,
                finalActions);
        assertEquals(encoded.length, fixture.prepareToEncode(encoder, context));
        fixture.encode(encoder, context);

        assertTrue(encoder.eof());
        assertArrayEquals(encoded, encoder.getData());
    }

    @Test
    public void decode() throws CoderException {
        final SWFDecoder decoder = new SWFDecoder(encoded);
        final Context context = new Context();
        final DecoderRegistry registry = new DecoderRegistry();
        registry.setActionDecoder(new ActionDecoder());
        context.setRegistry(registry);

        fixture = new ExceptionHandler(decoder, context);

        assertTrue(decoder.eof());
        assertEquals(variable, fixture.getVariable());
        assertEquals(tryActions, fixture.getTryActions());
        assertEquals(catchActions, fixture.getCatchActions());
        assertEquals(finalActions, fixture.getFinalActions());
    }
}