/*
 * ScreenVideoPacket.java
 * Transform
 * 
 * Copyright (c) 2001-2008 Flagstone Software Ltd. All rights reserved.
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
package com.flagstone.transform.video;

import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.LittleEndianDecoder;
import com.flagstone.transform.coder.LittleEndianEncoder;
import com.flagstone.transform.movie.image.ImageBlock;

/**
 * The ScreenVideoPacket class is used to encode or decode a frame of video
 * data using Macromedia's ScreenVideo format.
 * 
 */
public final class ScreenPacket implements Cloneable
{
	protected boolean keyFrame;
	protected int blockWidth;
	protected int blockHeight;
	protected int imageWidth;
	protected int imageHeight;
	protected List<ImageBlock> imageBlocks;

	protected ScreenPacket()
	{
		imageBlocks = new ArrayList<ImageBlock>();
	}

	/**
	 * Creates a ScreenVideoPacket.
	 * 
	 * @param key indicates whether the packet contains a key frame.
	 * @param imageWidth the width of the frame.
	 * @param imageHeight the height of the frame.
	 * @param blockWidth the width of the blocks that make up the frame.
	 * @param blockHeight the height of the blocks that make up the frame.
	 * @param blocks the array of ImageBlocks that make up the frame.
	 */
	public ScreenPacket(boolean key, int imageWidth, int imageHeight, 
	                         int blockWidth, int blockHeight, List<ImageBlock> blocks)
	{
		setKeyFrame(key);
		setImageWidth(imageWidth);
		setImageHeight(imageHeight);
		setBlockWidth(blockWidth);
		setBlockHeight(blockHeight);
		setImageBlocks(blocks);
	}
	
	public ScreenPacket(ScreenPacket object)
	{
		keyFrame = object.keyFrame;
		blockWidth = object.blockWidth;
		blockHeight = object.blockHeight;
		imageWidth = object.imageWidth;
		imageHeight = object.imageHeight;
		
		imageBlocks = new ArrayList<ImageBlock>(object.imageBlocks.size());
		
		for (ImageBlock block : object.imageBlocks) {
			imageBlocks.add(block.copy());
		}
	}

	
	/**
	 * Add an image block to the array that make up the frame.
	 * 
	 * @param block an ImageBlock. Must not be null.
	 */
	public ScreenPacket add(ImageBlock block)
	{
		imageBlocks.add(block);
		return this;
	}

	/**
	 * Returns true if the packet contains a key frame
	 */
	public boolean isKeyFrame()
	{
		return keyFrame;
	}

	/**
	 * Sets whether the frame is a key frame (true) or normal one (false).
	 * 
	 * @param key a boolean value indicating whether the frame is key (true) or
	 * normal (false.
	 */
	public void setKeyFrame(boolean key)
	{
		keyFrame = key;
	}

	/**
	 * Returns the width of the frame in pixels.
	 */
	public int getImageWidth()
	{
		return imageWidth;
	}

	/**
	 * Sets the width of the frame.
	 * 
	 * @param width the width of the frame in pixels.
	 */
	public void setImageWidth(int width)
	{
		imageWidth = width;
	}

	/**
	 * Returns the height of the frame in pixels.
	 */
	public int getImageHeight()
	{
		return imageHeight;
	}

	public void setImageHeight(int height)
	{
		imageHeight = height;
	}

	/**
	 * Returns the width of the blocks in pixels.
	 */
	public int getBlockWidth()
	{
		return blockWidth;
	}

	/**
	 * Sets the width of the image blocks.
	 * 
	 * @param width the width of the blocks in pixels.
	 */
	public void setBlockWidth(int width)
	{
		blockWidth = width;
	}

	/**
	 * Returns the height of the blocks in pixels.
	 */
	public int getBlockHeight()
	{
		return blockHeight;
	}

	/**
	 * Sets the height of the image blocks.
	 * 
	 * @param height the height of the blocks in pixels.
	 */
	public void setBlockHeight(int height)
	{
		blockHeight = height;
	}

	/**
	 * Returns the image blocks that have changed in this frame,
	 */
	public List<ImageBlock> getImageBlocks()
	{
		return imageBlocks;
	}

	/**
	 * Set the image blocks that have changed in this frame. If this is a key
	 * frame then all image blocks are displayed.
	 * 
	 * @param blocks the array of image blocks. Must not be null.
	 */
	public void setImageBlocks(List<ImageBlock> blocks)
	{
		imageBlocks = new ArrayList<ImageBlock>(blocks);
	}

	/**
	 * Creates and returns a deep copy of this object.
	 */
	public ScreenPacket copy()
	{
		return new ScreenPacket(this);
	}


	private int length()
	{
		int length = 5;

		for (ImageBlock block : imageBlocks)
		{
			length += 2;

			if (!block.isEmpty()) {
				length += block.getBlock().length;
			}
		}
		return length;
	}

	public byte[] encode()
	{
		LittleEndianEncoder coder = new LittleEndianEncoder(length());

		coder.writeBits(keyFrame ? 1 : 2, 4);
		coder.writeBits(3, 4);

		coder.writeBits((blockWidth / 16) - 1, 4);
		coder.writeBits(imageWidth, 12);
		coder.writeBits((blockHeight / 16) - 1, 4);
		coder.writeBits(imageHeight, 12);

		byte[] blockData;

		for (ImageBlock block : imageBlocks)
		{
			if (block.isEmpty())
			{
				coder.writeWord(0, 2);
			} 
			else
			{
				blockData = block.getBlock();
				coder.writeBits(blockData.length, 16);
				coder.writeBytes(blockData);
			}
		}

		return coder.getData();
	}

	public void decode(byte[] data)
	{
		LittleEndianDecoder coder = new LittleEndianDecoder(data);

		keyFrame = coder.readBits(4, false) == 1;
		coder.readBits(4, false); // codec = screen_video

		blockWidth = (coder.readBits(4, false) + 1) * 16;
		imageWidth = coder.readBits(12, false);
		blockHeight = (coder.readBits(4, false) + 1) * 16;
		imageHeight = coder.readBits(12, false);

		int columns = imageWidth / blockWidth + ((imageWidth % blockWidth > 0) ? 1 : 0); // NOPMD
		int rows = imageHeight / blockHeight + ((imageHeight % blockHeight > 0) ? 1 : 0); // NOPMD

		int height = imageHeight; // NOPMD
		int width = imageWidth; // NOPMD

		imageBlocks.clear();
		ImageBlock block;

		for (int i = 0; i < rows; i++, height -= blockHeight)
		{
			for (int j = 0; j < columns; j++, width -= blockWidth)
			{
				int length = coder.readBits(16, false);

				if (length == 0)
				{
					block = new ImageBlock(0, 0, null);
				} 
				else
				{
					int dataHeight = (height < blockHeight) ? height : blockHeight;
					int dataWidth = (width < blockWidth) ? width : blockWidth;

					block = new ImageBlock(dataHeight, dataWidth, coder.readBytes(new byte[length]));
				}

				imageBlocks.add(block);
			}
		}
	}
}