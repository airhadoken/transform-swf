/*
 * GetUrl2.java
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

package com.flagstone.transform.action;

import java.util.LinkedHashMap;
import java.util.Map;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The GetUrl2 action is used to either load a web page or movie clip or load or
 * submit variable values to/from a server.
 *
 * <p>
 * It extends the functionality provided by the GetUrl action by allowing the
 * variables defined in a movie to be submitted as form values to a server.
 * Variables defined in a movie can also be initialised by loading a file
 * containing variable name / value assignments.
 * </p>
 *
 * <p>
 * GetUrl2 gets the URL and the target from the Flash Player stack. The
 * <i>url</i> is the first argument popped from the stack and is a fully
 * qualified uniform resource location where the movie clip or web page will be
 * retrieved from. The second argument <i>target</i> - is either the name of a
 * specific movie clip, e.g. _root.movieClip or the name of a level in the main
 * movie into which a movie clip has been loaded, e.g. _level1.
 * </p>
 *
 * <p>
 * The <i>target</i> can either be the name of the frame can be one of the
 * following reserved words:
 * </p>
 *
 * <table class="datasheet">
 * <tr>
 * <td valign="top"><code>"name"</code></td>
 * <td>opens the new page in the frame with the name defined in the HTML
 * &lt;frame&gt; tag.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_blank</code></td>
 * <td>opens the new page in a new window.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_self</code></td>
 * <td>opens the new page in the current window.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_top</code></td>
 * <td>opens the new page in the top level frame of the current window.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_parent</code></td>
 * <td>opens the new page in the parent frame of the frame where the Flash
 * Player id displayed.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>""</code></td>
 * <td>(blank string) opens the new page in the current frame or window.</td>
 * </tr>
 * </table>
 *
 * <p>
 * Levels are virtual layers (analogous to the layers in the Display List).
 * Higher levels are displayed in front of lower levels. The background of each
 * level is transparent allowing movie clips on lower levels to be visible in
 * areas not filled by the movie clip on a given level. The main movie is loaded
 * into level 0. Movie clips are loaded into any level above this (1, 2, 124,
 * etc.). If a movie clip is loaded into a level that already contains a movie
 * clip then the existing clip is replaced by the new one. The level follows the
 * general form: "_level<i>n</i>" loads a movie clip into the current movie at
 * level <i>n</i>.
 * </p>
 *
 */
public final class GetUrl2 implements Action {
    
    private static final String FORMAT = "GetUrl2: { requestType=%s }";

    /** 
     * Request defines the different types of request that can be submitted
     * to a server using a GetUrl action.
      */
    public enum Request {
        /** Load a movie without submitting the movie variables. */
        MOVIE_TO_LEVEL,
        /** Load a movie submitting the movie variables using HTTP GET. */
        MOVIE_TO_LEVEL_WITH_GET,
        /** Load a movie submitting the movie variables using HTTP POST. */
        MOVIE_TO_LEVEL_WITH_POST,
        /** Load a movie or web page without submitting the movie variables. */
        MOVIE_TO_TARGET,
        /** Load a movie or web page sending variables using HTTP GET. */
        MOVIE_TO_TARGET_WITH_GET,
        /** Load a movie or web page sending the variables using HTTP POST. */
        MOVIE_TO_TARGET_WITH_POST,
        /** Load variables without submitting the movie variables. */
        VARIABLES_TO_LEVEL,
        /** Load variables submitting the movie variables using HTTP GET. */
        VARIABLES_TO_LEVEL_WITH_GET,
        /** Load variables submitting the movie variables using HTTP POST. */
        VARIABLES_TO_LEVEL_WITH_POST,
        /** Load variables without submitting the movie variables. */
        VARIABLES_TO_TARGET,
        /** Load variables submitting the movie variables using HTTP GET. */
        VARIABLES_TO_TARGET_WITH_GET,
        /** Load variables submitting the movie variables using HTTP POST. */
        VARIABLES_TO_TARGET_WITH_POST;
    }

    private int request;

    /**
     * Creates and initialises a GetUrl2 action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws CoderException
     *             if an error occurs while decoding the data.
     */
    public GetUrl2(final SWFDecoder coder) throws CoderException {
        coder.readByte();
        coder.readWord(2, false);
        request = coder.readByte();
    }

    /**
     * Creates a GetUrl2 using the specified request type.
     *
     * @param request
     *            the type of request to be performed.
     */
    public GetUrl2(final Request request) {
        setRequest(request);
    }

    /**
     * Creates and initialises a GetUrl2 action using the values
     * copied from another GetUrl2 action.
     *
     * @param object
     *            a GetUrl2 action from which the values will be
     *            copied.
     */
    public GetUrl2(final GetUrl2 object) {
        request = object.request;
    }

    /**
     * Returns the request type.
     */
    public Request getRequest() {
        Request value;
        switch(request) {
        case 0:
            value = Request.MOVIE_TO_LEVEL;
            break;
        case 1:
            value = Request.MOVIE_TO_LEVEL_WITH_GET;
            break;
        case 2:
            value = Request.MOVIE_TO_LEVEL_WITH_POST;
            break;
        case 64:
            value = Request.MOVIE_TO_TARGET;
            break;
        case 65:
            value = Request.MOVIE_TO_TARGET_WITH_POST;
            break;
        case 66:
            value = Request.MOVIE_TO_TARGET_WITH_POST;
            break;
        case 128:
            value = Request.MOVIE_TO_LEVEL_WITH_POST;
            break;
        case 129:
            value = Request.MOVIE_TO_LEVEL_WITH_POST;
            break;
        case 130:
            value = Request.MOVIE_TO_LEVEL_WITH_POST;
            break;
        case 192:
            value = Request.VARIABLES_TO_LEVEL_WITH_POST;
            break;
        case 193:
            value = Request.VARIABLES_TO_LEVEL_WITH_POST;
            break;
        case 194:
            value = Request.VARIABLES_TO_LEVEL_WITH_POST;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /** TODO(method).
     *
     * @param type
     *            the type of request to be performed.
     */
    public void setRequest(final Request type) {
        switch (type) {
        case MOVIE_TO_LEVEL:
            request = 0;
            break;
        case MOVIE_TO_LEVEL_WITH_GET:
            request = 1;
            break;
        case MOVIE_TO_LEVEL_WITH_POST:
            request = 2;
            break;
        case MOVIE_TO_TARGET:
            request = 64;
            break;
        case MOVIE_TO_TARGET_WITH_GET:
            request = 65;
            break;
        case MOVIE_TO_TARGET_WITH_POST:
            request = 66;
            break;
        case VARIABLES_TO_LEVEL:
            request = 128;
            break;
        case VARIABLES_TO_LEVEL_WITH_GET:
            request = 129;
            break;
        case VARIABLES_TO_LEVEL_WITH_POST:
            request = 130;
            break;
        case VARIABLES_TO_TARGET:
            request = 192;
            break;
        case VARIABLES_TO_TARGET_WITH_GET:
            request = 193;
            break;
        case VARIABLES_TO_TARGET_WITH_POST:
            request = 194;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /** {@inheritDoc} */
    public GetUrl2 copy() {
        return new GetUrl2(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, request);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        return 4;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        coder.writeByte(ActionTypes.GET_URL_2);
        coder.writeWord(1, 2);
        coder.writeWord(request, 1);
    }
}