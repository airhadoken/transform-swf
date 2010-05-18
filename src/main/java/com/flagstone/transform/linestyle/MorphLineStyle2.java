/*
 * MorphLineStyle2.java
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

package com.flagstone.transform.linestyle;


import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.fillstyle.FillStyle;

/** TODO(class). */
public final class MorphLineStyle2 implements SWFEncodeable,
        Copyable<MorphLineStyle2> {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MorphLineStyle2: {"
            + " startWidth=%d; endWidth=%d; startColor=%s; endColor=%s;"
            + " fillStyle=%s; startCap=%s; endCap=%s; joinStyle=%s;"
            + " scaledHorizontally=%b; scaledVertically=%b;"
            + " pixelAligned=%b; lineClosed=%b; miterLimit=%d }";

    private int startWidth;
    private int endWidth;
    private Color startColor;
    private Color endColor;

    private int startCap;
    private int endCap;
    private int joinStyle;
    private FillStyle fillStyle;

    private boolean horizontal;
    private boolean vertical;
    private boolean pixelAligned;
    private boolean lineClosed;

    private int miterLimit;

    private transient boolean hasFillStyle;
    private transient boolean hasMiter;

    /**
     * Creates and initialises a MorphLineStyle2 object using values encoded
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
    public MorphLineStyle2(final SWFDecoder coder, final Context context)
            throws CoderException {
        startWidth = coder.readUI16();
        endWidth = coder.readUI16();
        unpack(coder.readB16());

        if (hasMiter) {
            coder.readUI16();
        }

        if (hasFillStyle) {
            final SWFFactory<FillStyle> decoder = context.getRegistry()
                    .getMorphFillStyleDecoder();
            fillStyle = decoder.getObject(coder, context);
        } else {
            startColor = new Color(coder, context);
            endColor = new Color(coder, context);
        }
    }


    public MorphLineStyle2(final int initialWidth, final int finalWidth,
            final Color initialColor, final Color finalColor) {
        super();

        setStartWidth(initialWidth);
        setEndWidth(finalWidth);
        setStartColor(initialColor);
        setEndColor(finalColor);

        vertical = true;
        vertical = true;
        lineClosed = true;
    }

    /**
     * Creates and initialises a MorphLineStyle2 object using the values copied
     * from another MorphLineStyle2 object.
     *
     * @param object
     *            a MorphLineStyle2 object from which the values will be
     *            copied.
     */
    public MorphLineStyle2(final MorphLineStyle2 object) {
        startWidth = object.startWidth;
        endWidth = object.endWidth;

        startColor = object.startColor;
        endColor = object.endColor;

        if (object.fillStyle != null) {
            fillStyle = object.fillStyle.copy();
        }

        startCap = object.startCap;
        endCap = object.endCap;
        joinStyle = object.joinStyle;

        horizontal = object.horizontal;
        vertical = object.vertical;
        pixelAligned = object.pixelAligned;
        lineClosed = object.lineClosed;
        miterLimit = object.miterLimit;
    }

    /**
     * Returns the width of the line at the start of the morphing process.
     */
    public int getStartWidth() {
        return startWidth;
    }

    /**
     * Returns the width of the line at the end of the morphing process.
     */
    public int getEndWidth() {
        return endWidth;
    }

    /**
     * Returns the colour of the line at the start of the morphing process.
     */
    public Color getStartColor() {
        return startColor;
    }

    /**
     * Returns the colour of the line at the end of the morphing process.
     */
    public Color getEndColor() {
        return endColor;
    }

    /**
     * Sets the width of the line at the start of the morphing process.
     *
     * @param aNumber
     *            the starting width of the line. Must be in the range 0..65535.
     */
    public void setStartWidth(final int aNumber) {
        if ((aNumber < 0) || (aNumber > 65535)) {
            throw new IllegalArgumentRangeException(0, 65535, aNumber);
        }
        startWidth = aNumber;
    }

    /**
     * Sets the width of the line at the end of the morphing process.
     *
     * @param aNumber
     *            the ending width of the line. Must be in the range 0..65535.
     */
    public void setEndWidth(final int aNumber) {
        if ((aNumber < 0) || (aNumber > 65535)) {
            throw new IllegalArgumentRangeException(0, 65535, aNumber);
        }
        endWidth = aNumber;
    }

    /**
     * Returns the colour of the line at the start of the morphing process.
     *
     * @param aColor
     *            the starting colour of the line. Must not be null.
     */
    public void setStartColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        startColor = aColor;
    }

    /**
     * Sets the colour of the line at the end of the morphing process.
     *
     * @param aColor
     *            the ending colour of the line. Must not be null.
     */
    public void setEndColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        endColor = aColor;
    }


    public CapStyle getStartCap() {
        CapStyle style;
        if (startCap == 1) {
            style = CapStyle.NONE;
        } else if (startCap == 2) {
            style = CapStyle.SQUARE;
        } else {
            style = CapStyle.ROUND;
        }
        return style;
    }


    public void setStartCap(final CapStyle capStyle) {
        switch (capStyle) {
        case NONE:
            startCap = 1;
            break;
        case SQUARE:
            startCap = 2;
            break;
        default:
            startCap = 0;
            break;
        }
    }


    public CapStyle getEndCap() {
        CapStyle style;
        if (endCap == 1) {
            style = CapStyle.NONE;
        } else if (endCap == 2) {
            style = CapStyle.SQUARE;
        } else {
            style = CapStyle.ROUND;
        }
        return style;
    }


    public void setEndCap(final CapStyle capStyle) {
        switch (capStyle) {
        case NONE:
            endCap = 1;
            break;
        case SQUARE:
            endCap = 2;
            break;
        default:
            endCap = 0;
            break;
        }
    }


    public JoinStyle getJoinStyle() {
        JoinStyle style;
        if (endCap == 1) {
            style = JoinStyle.BEVEL;
        } else if (endCap == 2) {
            style = JoinStyle.MITER;
        } else {
            style = JoinStyle.ROUND;
        }
        return style;
    }


    public void setJoinStyle(final JoinStyle style) {
        switch (style) {
        case BEVEL:
            joinStyle = 1;
            break;
        case MITER:
            joinStyle = 2;
            break;
        default:
            joinStyle = 0;
            break;
        }
    }


    public boolean isHorizontal() {
        return horizontal;
    }


    public void setHorizontal(final boolean scaled) {
        horizontal = scaled;
    }


    public boolean isVertical() {
        return vertical;
    }


    public void setVertical(final boolean scaled) {
        vertical = scaled;
    }


    public boolean isPixelAligned() {
        return pixelAligned;
    }


    public void setPixelAligned(final boolean aligned) {
        pixelAligned = aligned;
    }


    public boolean isLineClosed() {
        return lineClosed;
    }


    public void setLineClosed(final boolean closed) {
        lineClosed = closed;
    }


    public int getMiterLimit() {
        return miterLimit;
    }


    public void setMiterLimit(final int limit) {
        if ((limit < 0) || (limit > 65535)) {
            throw new IllegalArgumentRangeException(0, 65535, limit);
        }
        miterLimit = limit;
    }


    public FillStyle getFillStyle() {
        return fillStyle;
    }


    public void setFillStyle(final FillStyle style) {
        fillStyle = style;
    }

    /** {@inheritDoc} */
    public MorphLineStyle2 copy() {
        return new MorphLineStyle2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, startWidth, endWidth, startColor,
                endColor, fillStyle, startCap, endCap, joinStyle,
                horizontal, vertical, pixelAligned, lineClosed,
                miterLimit);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {

        hasFillStyle = fillStyle != null;
        hasMiter = joinStyle == 2;

        int length = 6;

        if (hasMiter) {
            length += 2;
        }

        if (hasFillStyle) {
            length += fillStyle.prepareToEncode(context);
        } else {
            length += 4;
            length += 4;
        }

        if (horizontal || vertical) {
            context.getVariables().put(Context.SCALING_STROKE, 1);
        }

        return length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        coder.writeI16(startWidth);
        coder.writeI16(endWidth);
        coder.writeB16(pack());

        if (hasMiter) {
            coder.writeI16(miterLimit);
        }

        if (hasFillStyle) {
            fillStyle.encode(coder, context);
        } else {
            startColor.encode(coder, context);
            endColor.encode(coder, context);
        }
    }

    //TODO Fix this to remove the magic numbers
    private int pack() {
// CHECKSTYLE:OFF
        int value = 0;

        if (startCap == 1) {
            value |= 0x00004000;
        } else if (startCap == 2) {
            value |= 0x00008000;
        }

        if (joinStyle == 1) {
            value |= 0x00001000;
        } else if (joinStyle == 2) {
            value |= 0x00002000;
        }

        value |= fillStyle == null ? 0 : 0x00000800;
        value |= horizontal ? 0 : 0x00000400;
        value |= vertical ? 0 : 0x00000200;
        value |= pixelAligned ? 0x00000100 : 0;
        value |= lineClosed ? 0 : 0x00000004;
        value |= endCap;

        return value;
// CHECKSTYLE:ON
    }

    //TODO Fix this to remove the magic numbers
    private void unpack(final int value) {
// CHECKSTYLE:OFF
        if ((value & 0x00004000) > 0) {
            startCap = 1;
        } else if ((value & 0x00008000) > 0) {
            startCap = 2;
        } else {
            startCap = 0;
        }

        if ((value & 0x00001000) > 0) {
            joinStyle = 1;
            hasMiter = false;
        } else if ((value & 0x00002000) > 0) {
            joinStyle = 2;
            hasMiter = true;
        } else {
            joinStyle = 0;
            hasMiter = false;
        }

        hasFillStyle = (value & 0x00000800) != 0;
        horizontal = (value & 0x00000400) == 0;
        vertical = (value & 0x00000200) == 0;
        pixelAligned = (value & 0x00000100) != 0;
        lineClosed = (value & 0x00000004) == 0;
        endCap = value & 0x00000003;
// CHECKSTYLE:ON
    }
}
