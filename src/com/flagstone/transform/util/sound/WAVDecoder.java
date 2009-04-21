/*
 *  SoundConstructor.java
 *  Transform Utilities
 *
 * Copyright (c) 2001-2009 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.util.sound;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import com.flagstone.transform.MovieTag;
import com.flagstone.transform.SoundFormat;
import com.flagstone.transform.Strings;
import com.flagstone.transform.coder.LittleEndianDecoder;
import com.flagstone.transform.sound.DefineSound;
import com.flagstone.transform.sound.SoundStreamBlock;
import com.flagstone.transform.sound.SoundStreamHead2;
import com.flagstone.transform.util.image.ImageInfo;

/**
 * Decoder for WAV sounds so they can be added to a flash file.
 */
public final class WAVDecoder implements SoundProvider, SoundDecoder
{
    protected static final int[] riffSignature = { 82, 73, 70, 70 };
    protected static final int[] wavSignature = { 87, 65, 86, 69 };

    protected static final int FMT = 0x20746d66;
    protected static final int DATA = 0x61746164;
    
    private SoundFormat format;
    private int numberOfChannels;
    private int samplesPerChannel;
    private int sampleRate;
    private int sampleSize;
    private byte[] sound = null;

    public SoundDecoder newDecoder() {
    	return new WAVDecoder();
    }
    
    public void read(String path) throws FileNotFoundException, IOException, DataFormatException
    {
    	read(new File(path));
    }
    
    public void read(File file) throws FileNotFoundException, IOException, DataFormatException
    {
		decode(loadFile(file));
    }

    public void read(URL url) throws FileNotFoundException, IOException, DataFormatException
    {
	    URLConnection connection = url.openConnection();

	    int fileSize = connection.getContentLength();
            
	    if (fileSize<0) {
              throw new FileNotFoundException(url.getFile());
	    }
	    
	    byte[] bytes = new byte[fileSize];

	    InputStream stream = url.openStream();
	    BufferedInputStream buffer = new BufferedInputStream(stream);

	    buffer.read(bytes);
	    buffer.close();

		decode(bytes);
    }

	/**
	 * Create a definition for an event sound using the sound in the specified file.
	 * 
	 * @param identifier the unique identifier that will be used to refer to the 
	 * sound in the Flash file.
	 * 
	 * @param file the File containing the abstract path to the sound.
	 * 
	 * @return a sound definition that can be added to a Movie.
	 * 
	 * @throws FileNotFoundException if the file cannot be found or opened.
	 * 
	 * @throws IOException if there is an error reading the file.
	 * 
	 * @throws DataFormatException if there is a problem decoding the image, 
	 * either it is in an unsupported format or an error occurred while decoding
	 * the image.
	 */
    public DefineSound defineSound(int identifier)
    {
        return new DefineSound(identifier, format, sampleRate, numberOfChannels, sampleSize, samplesPerChannel, sound);
    }

    private byte[] loadFile(final File file) throws FileNotFoundException, IOException {
		byte[] data = new byte[(int) file.length()];

		FileInputStream stream = null; //TODO(code) fix

		try {
			stream = new FileInputStream(file);
			int bytesRead = stream.read(data);

			if (bytesRead != data.length) {
				throw new IOException(file.getAbsolutePath());
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return data;
	}
    /** 
     * Generates all the objects required to generate a streaming sound from 
     * a URL reference. 
     * 
     * @param frameRate the rate at which the movie is played. Sound are streamed
     * with one block of sound data per frame.
     * 
 	 * @param url the Uniform Resource Locator referencing the file containing
 	 * the sound.
     * 
     * @return an array where the first object is the SoundStreamHead2 object 
     * that defines the streaming sound, followed by SoundStreamBlock objects 
     * containing the sound samples that will be played in each frame.
	 * 
	 * @throws FileNotFoundException if the file cannot be found or opened.
	 * 
	 * @throws IOException if there is an error reading the file.
	 * 
	 * @throws DataFormatException if there is a problem decoding the sound, 
	 * either it is in an unsupported format or an error occurred while decoding
	 * the sound data.
     */
    public List<MovieTag> streamSound(int frameRate)
    {
     	ArrayList<MovieTag>array = new ArrayList<MovieTag>();
  
        int firstSample = 0;
        int firstSampleOffset = 0;
        int bytesPerBlock = 0;
        int bytesRemaining = 0;
        int numberOfBytes = 0;
   	    byte[] bytes = null;
	    
    	int samplesPerBlock = sampleRate/frameRate;
	 	int numberOfBlocks = samplesPerChannel/samplesPerBlock;

	    array.add(new SoundStreamHead2(format, sampleRate, numberOfChannels, sampleSize, sampleRate, numberOfChannels, sampleSize, samplesPerBlock));

	    for (int i=0; i<numberOfBlocks; i++)
	    {
            firstSample = i*samplesPerBlock;
            firstSampleOffset = firstSample * sampleSize * numberOfChannels;
            bytesPerBlock = samplesPerBlock * sampleSize * numberOfChannels;
            bytesRemaining = sound.length - firstSampleOffset;
            
            numberOfBytes = (bytesRemaining < bytesPerBlock) ? bytesRemaining : bytesPerBlock;
        
            bytes = new byte[numberOfBytes];
            System.arraycopy(sound, firstSampleOffset, bytes, 0, numberOfBytes);
            
            array.add(new SoundStreamBlock(bytes));
	    }
    	return array;
     }

	protected void decode(byte[] data) throws DataFormatException
    {
    	LittleEndianDecoder coder = new LittleEndianDecoder(data);
        
        for (int i=0; i<4; i++)
        {
            if (coder.readByte() != riffSignature[i]) {
                throw new DataFormatException(Strings.UNSUPPORTED_FILE_FORMAT);
            }
        }
        
        coder.readWord(4, false);
        
        for (int i=0; i<4; i++)
        {
            if (coder.readByte() != wavSignature[i]) {
                throw new DataFormatException(Strings.UNSUPPORTED_FILE_FORMAT);
            }
        }
       
        int chunkType;
        int length;
        boolean moreChunks;

        do {
            chunkType = coder.readWord(4, false);
            length = coder.readWord(4, false);
            
            int blockStart = coder.getPointer();
            
            switch (chunkType)
            {
                case FMT: 
                	decodeFMT(coder); 
                	break;
                case DATA: 
                	decodeDATA(coder, length); 
                	break;
                default: 
                	coder.adjustPointer(length << 3); 
                	break;
            }

            int nextBlock = blockStart + (length << 3);
            coder.setPointer(nextBlock);
            moreChunks = !coder.eof();
        } while (moreChunks);
    }

    private void decodeFMT(LittleEndianDecoder coder) throws DataFormatException
    {
        format = SoundFormat.PCM;
        
        if (coder.readWord(2, false) != 1) {
            throw new DataFormatException(Strings.UNSUPPORTED_FILE_FORMAT);
        }
        
        numberOfChannels = coder.readWord(2, false);
        sampleRate = coder.readWord(4, false);
        coder.readWord(4, false); // total data length
        coder.readWord(2, false); // total bytes per sample
        sampleSize = coder.readWord(2, false) / 8;
    }
    
    private void decodeDATA(LittleEndianDecoder coder, int length)
    {
        samplesPerChannel = length / (sampleSize*numberOfChannels);

        sound = coder.readBytes(new byte[length]);
    }
}