package com.flagstone.transform.filter;

import java.util.Arrays;

import com.flagstone.transform.Strings;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Filter;
import com.flagstone.transform.coder.FilterTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Bounds;

public final class ColorMatrixFilter implements Filter {

	private static final String FORMAT = "ColorMatrix: { matrix=%s }";
	
	private final float[] matrix;

	public ColorMatrixFilter(final SWFDecoder coder) throws CoderException {
		coder.readByte();
		matrix = new float[20];
		for (int i=0; i<20; i++) {
			matrix[i] = coder.readFloat();
		}
	}
	
	public ColorMatrixFilter(final float[] matrix) {
		if (matrix == null || matrix.length != 20) {
			throw new IllegalArgumentException(Strings.VALUE_NOT_SET);
		}
		this.matrix = Arrays.copyOf(matrix, matrix.length);
	}
	
	public ColorMatrixFilter(final ColorMatrixFilter object) {
		matrix = object.matrix;
	}
	
	public float[] getMatrix() {
		return Arrays.copyOf(matrix, matrix.length);
	}

	public ColorMatrixFilter copy() {
		return new ColorMatrixFilter(this);
	}
	
	@Override
	public String toString() {
		return String.format(FORMAT, matrix);
	}

	@Override
	public boolean equals(final Object object) {
		boolean result;
		ColorMatrixFilter filter;

		if (object == null) {
			result = false;
		} else if (object == this) {
			result = true;
		} else if (object instanceof ColorMatrixFilter) {
			filter = (ColorMatrixFilter) object;
			result = Arrays.equals(matrix, filter.matrix);
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(matrix);
	}

	public int prepareToEncode(final SWFEncoder coder, final Context context) {
		return 81;
	}

	public void encode(final SWFEncoder coder, final Context context) throws CoderException {
		coder.writeByte(FilterTypes.COLOR_MATRIX);
		for (float value : matrix) {
			coder.writeFloat(value);
		}
	}
}
