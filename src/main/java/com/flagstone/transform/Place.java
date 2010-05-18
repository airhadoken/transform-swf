/*
 * PlaceObject.java
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
package com.flagstone.transform;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTag;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.ColorTransform;
import com.flagstone.transform.datatype.CoordTransform;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * PlaceObject is used to add an object (shape, button, etc.) to the Flash
 * Player's display list.
 *
 * <p>
 * When adding an object to the display list a coordinate transform can be
 * applied to the object. This is principally used to specify the location of
 * the object when it is drawn on the screen however more complex coordinate
 * transforms can also be specified such as rotating or scaling the object
 * without changing the original definition.
 * </p>
 *
 * <p>
 * Similarly the color transform allows the color of the object to be changed
 * when it is displayed without changing the original definition. The
 * PlaceObject class only supports opaque colours so although the ColorTransform
 * supports transparent colours this information is ignored by the Flash Player.
 * The colour transform is optional and may be set to null, reducing the size of
 * the PlaceObject instruction when it is encoded.
 * </p>
 *
 * @see Place2
 * @see Remove
 * @see Remove2
 */
public final class Place implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Place: { layer=%d; identifier=%d;"
            + " transform=%s; colorTransform=%s }";

    /** The unique identifier of the object that will be displayed. */
    private int identifier;
    private int layer;
    private CoordTransform transform;
    private ColorTransform colorTransform;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a Place object using values encoded
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
    public Place(final SWFDecoder coder, final Context context)
            throws CoderException {
        final int start = coder.getPointer();
        length = coder.readHeader();
        final int end = coder.getPointer() + (length << Coder.BYTES_TO_BITS);

        identifier = coder.readUI16();
        layer = coder.readUI16();
        transform = new CoordTransform(coder);

        if (coder.getPointer() < end) {
            colorTransform = new ColorTransform(coder, context);
        }

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(),
                    start >> Coder.BITS_TO_BYTES, length,
                    (coder.getPointer() - end) >> Coder.BITS_TO_BYTES);
        }
    }

    /**
     * Creates a PlaceObject object that places the the object with the
     * identifier at the specified layer and at the position specified by the
     * coordinate transform.
     *
     * @param uid
     *            the unique identifier for the object to the placed on the
     *            display list. Must be in the range 1..65535.
     * @param level
     *            the layer in the display list where the object will be placed.
     * @param position
     *            an CoordTransform object that defines the orientation, size
     *            and location of the object when it is drawn. Must not be null.
     */
    public Place(final int uid, final int level,
            final CoordTransform position) {
        setIdentifier(uid);
        setLayer(level);
        setTransform(position);
    }

    /**
     * Creates a PlaceObject object that places the the object with the
     * identifier at the specified layer, coordinate transform and colour
     * transform.
     *
     * @param uid
     *            the unique identifier for the object to the placed on the
     *            display list. Must be in the range 1..65535.
     * @param level
     *            the layer in the display list where the object will be placed.
     * @param position
     *            an CoordTransform object that defines the orientation, size
     *            and location of the object when it is drawn. Must not be null.
     * @param color
     *            an ColorTransform object that defines the colour of the object
     *            when it is drawn.
     */
    public Place(final int uid, final int level,
            final CoordTransform position,
            final ColorTransform color) {
        setIdentifier(uid);
        setLayer(level);
        setTransform(position);
        setColorTransform(color);
    }

    /**
     * Creates and initialises a Place object using the values copied
     * from another Place object.
     *
     * @param object
     *            a Place object from which the values will be
     *            copied.
     */
    public Place(final Place object) {
        identifier = object.identifier;
        layer = object.layer;
        transform = object.transform;
        colorTransform = object.colorTransform;
    }

    /**
     * Returns the identifier of the object to add to the display list.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the object that will be added to the display list.
     *
     * @param uid
     *            the unique identifier for the object to the placed on the
     *            display list. Must be in the range 1..65535.
     */
    public Place setIdentifier(final int uid) {
        if ((uid < SWF.MIN_IDENTIFIER) || (uid > SWF.MAX_IDENTIFIER)) {
            throw new IllegalArgumentRangeException(
                    SWF.MIN_IDENTIFIER, SWF.MAX_IDENTIFIER, uid);
        }
        identifier = uid;
        return this;
    }

    /**
     * Returns the layer that defines the order in which objects are displayed.
     */
    public int getLayer() {
        return layer;
    }

    /**
     * Sets the layer that defines the order in which objects are displayed.
     *
     * @param aNumber
     *            the layer in the display list where the object will be placed.
     *            Must be in the range 1..65535.
     */
    public Place setLayer(final int aNumber) {
        if ((aNumber < 1) || (aNumber > SWF.MAX_LAYER)) {
            throw new IllegalArgumentRangeException(1, SWF.MAX_LAYER, aNumber);
        }
        layer = aNumber;
        return this;
    }

    /**
     * Returns the transform that defines the position where the object is
     * displayed.
     */
    public CoordTransform getTransform() {
        return transform;
    }

    /**
     * Sets the transform that defines the position where the object is
     * displayed.
     *
     * @param aTransform
     *            an CoordTransform object that defines the orientation, size
     *            and location of the object when it is drawn. Must not be null.
     */
    public Place setTransform(final CoordTransform aTransform) {
        if (aTransform == null) {
            throw new IllegalArgumentException();
        }
        transform = aTransform;
        return this;
    }

    /**
     * Returns the colour transform that defines any colour effects applied when
     * the object is displayed. May be null if no transform is defined.
     */
    public ColorTransform getColorTransform() {
        return colorTransform;
    }

    /**
     * Sets the location where the object will be displayed.
     *
     * @param xCoord
     *            the x-coordinate of the object's origin.
     * @param yCoord
     *            the x-coordinate of the object's origin.
     * @return this object.
     */
    public Place setLocation(final int xCoord, final int yCoord) {
        transform = CoordTransform.translate(xCoord, yCoord);
        return this;
    }

    /**
     * Sets the colour transform that defines any colour effects applied when
     * the object is displayed.
     *
     * @param aColorTransform
     *            an ColorTransform object that defines the colour of the object
     *            when it is drawn. May be set to null.
     */
    public Place setColorTransform(final ColorTransform aColorTransform) {
        colorTransform = aColorTransform;
        return this;
    }

    /** {@inheritDoc} */
    public Place copy() {
        return new Place(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, layer, transform,
                colorTransform);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        length = 4;
        length += transform.prepareToEncode(context);
        // TODO(optimise) replace with if statement ?
        length += colorTransform == null ? 0 : colorTransform.prepareToEncode(
                context);

        return 2 + length;
        // CHECKSTYLE:ON
   }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        final int start = coder.getPointer();
        coder.writeHeader(MovieTypes.PLACE, length);
        final int end = coder.getPointer() + (length << Coder.BYTES_TO_BITS);
        coder.writeI16(identifier);
        coder.writeI16(layer);
        transform.encode(coder, context);

        if (colorTransform != null) {
            colorTransform.encode(coder, context);
        }

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(),
                    start >> Coder.BITS_TO_BYTES, length,
                    (coder.getPointer() - end) >> Coder.BITS_TO_BYTES);
        }
    }
}
