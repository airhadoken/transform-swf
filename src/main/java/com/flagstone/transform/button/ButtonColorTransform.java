/*
 * ButtonColorTransform.java
 * Transform
 *
 * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.button;


import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTag;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.ColorTransform;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * <p>
 * ButtonColorTransform defines the colour transform that is applied to each
 * shape that is used to draw a button.
 * </p>
 *
 * <p>
 * This class is only used in conjunction with {@link DefineButton}. The
 * {@link DefineButton2} class allows colour transforms to be specified in the
 * ButtonRecord object that identifies each shape that is displayed for a given
 * button state.
 * </p>
 *
 * @see DefineButton
 * @see DefineButton2
 */
//TODO(class)
public final class ButtonColorTransform implements MovieTag {

    private static final String FORMAT = "ButtonColorTransform: { identifier=%d; colorTransform=%s }";

    private int identifier;
    private ColorTransform colorTransform;

    private transient int length;

    /**
     * Creates and initialises a ButtonColorTransform object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws CoderException
     *             if an error occurs while decoding the data.
     */
    public ButtonColorTransform(final SWFDecoder coder, final Context context)
            throws CoderException {

        final int start = coder.getPointer();
        length = coder.readWord(2, false) & 0x3F;

        if (length == 0x3F) {
            length = coder.readWord(4, false);
        }
        final int end = coder.getPointer() + (length << 3);

        identifier = coder.readWord(2, false);
        colorTransform = new ColorTransform(coder, context);

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(), start >> 3, length,
                    (coder.getPointer() - end) >> 3);
        }
    }

    /**
     * Creates a ButtonColorTransform object with a colour transform for the
     * specified button.
     *
     * @param uid
     *            the unique identifier of a button that this object applies to.
     *            Must be in the range 1..65535.
     * @param transform
     *            an ColorTransform object that will be applied to the button.
     */
    public ButtonColorTransform(final int uid, final ColorTransform transform) {
        setIdentifier(uid);
        setColorTransform(transform);
    }

    /**
     * Creates and initialises a ButtonColorTransform object using the values copied
     * from another ButtonColorTransform object.
     *
     * @param object
     *            a ButtonColorTransform object from which the values will be
     *            copied.
     */
    public ButtonColorTransform(final ButtonColorTransform object) {
        identifier = object.identifier;
        colorTransform = object.colorTransform;
    }

    /**
     * Returns the unique identifier of the button that this object applies to.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Returns the colour transform that will be applied to the button.
     */
    public ColorTransform getColorTransform() {
        return colorTransform;
    }

    /**
     * Sets the identifier of the button that the transform applies to.
     *
     * @param uid
     *            the unique identifier of the button that the colour transform
     *            will be applied to. Must be in the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > 65535)) {
             throw new IllegalArgumentRangeException(1, 65536, uid);
        }
        identifier = uid;
    }

    /**
     * Sets the colour transform that will be applied to the button.
     *
     * @param transform
     *            the colour transform object. Must not be null.
     */
    public void setColorTransform(final ColorTransform transform) {
        if (transform == null) {
            throw new NullPointerException();
        }
        colorTransform = transform;
    }

    /** TODO(method). */
    public ButtonColorTransform copy() {
        return new ButtonColorTransform(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, colorTransform);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        length = 4 + colorTransform.prepareToEncode(coder, context);

        return (length > 62 ? 6 : 2) + length;
    }

    /** {@inheritDoc} */
    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {

        final int start = coder.getPointer();

        if (length >= 63) {
            coder.writeWord((MovieTypes.BUTTON_COLOR_TRANSFORM << 6) | 0x3F, 2);
            coder.writeWord(length, 4);
        } else {
            coder.writeWord((MovieTypes.BUTTON_COLOR_TRANSFORM << 6) | length,
                    2);
        }
        final int end = coder.getPointer() + (length << 3);

        coder.writeWord(identifier, 2);
        colorTransform.encode(coder, context);

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(), start >> 3, length,
                    (coder.getPointer() - end) >> 3);
        }
    }
}