/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2009, 2010, 2011, 2012, 2013, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public-taglib.
 *
 * aocode-public-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.taglib;

import com.aoindustries.encoding.MediaEncoder;
import com.aoindustries.encoding.MediaException;
import com.aoindustries.encoding.MediaType;
import com.aoindustries.encoding.MediaValidator;
import com.aoindustries.encoding.MediaWriter;
import com.aoindustries.encoding.ValidMediaInput;
import com.aoindustries.encoding.servlet.HttpServletResponseEncodingContext;
import com.aoindustries.io.TempFileList;
import com.aoindustries.io.buffer.AutoTempFileWriter;
import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.io.buffer.BufferWriter;
import com.aoindustries.io.buffer.LoggingWriter;
import com.aoindustries.io.buffer.SegmentedWriter;
import com.aoindustries.servlet.filter.TempFileContext;
import com.aoindustries.util.WrappedException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * <p>
 * The exhibits all of the behavior of <code>AutoEncodingFilteredTag</code> with
 * the only exception being that it buffers its content instead of using filters.
 * This allows the tag to capture its body.  Character validation is performed
 * as the data goes into the buffer to ensure the captured data is correct for
 * its content type.
 * </p>
 * <p>
 * The tag also has the addition of a separate output type.  Thus, we have three
 * types involved:
 * <ol>
 * <li>contentType - The characters are validated to this type as they go into the buffer.</li>
 * <li>outputType - Our output characters are validated to this type as they are written.</li>
 * <li>containerContentType - Our output characters are encoded to this type as they are written.</li>
 * </ol>
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public abstract class AutoEncodingBufferedTag extends SimpleTagSupport {

	/**
	 * Enables logging of all buffer calls.
	 */
	private static final boolean ENABLE_BUFFER_LOGGING = false; // Must be false for production releases

	/**
	 * Shared log writer.
	 */
	private static final Writer log;
	static {
		if(ENABLE_BUFFER_LOGGING) {
			try {
				File tempFile = File.createTempFile("AutoEncodingBufferedTag.log", null);
				tempFile.deleteOnExit();
				log = new BufferedWriter(
					new OutputStreamWriter(
						new FileOutputStream(
							tempFile
						)
					)
				);
			} catch(IOException e) {
				throw new WrappedException(e);
			}
		} else {
			log = null;
		}
	}

    /**
     * Gets the type of data that is contained by this tag.
     */
    public abstract MediaType getContentType();

    /**
     * Gets the output type of this tag.  This is used to determine the correct
     * encoder.  If the tag never has any output this should return <code>null</code>.
     * When <code>null</code> is returned, any output will result in an error.
     */
    public abstract MediaType getOutputType();

    /**
     * The validator is stored to allow nested tags to check if their output
     * is already being filtered on this tags input.  When this occurs they
     * skip the validation of their own output.
     */
    /*
    @Override
    public boolean isValidatingMediaInputType(MediaType inputType) {
        return inputValidator!=null && inputValidator.isValidatingMediaInputType(inputType);
    }*/

    /**
     * Gets the initial buffer size.  Defaults to 32 characters (the same
     * default as <code>CharArrayBuffer</code>.
     */
    public int getInitialBufferSize() {
        return 32;
    }

    /**
     * Gets the number of characters that may be buffered before switching to the
     * use of a temp file.
	 *
	 * @return the threshold or <code>Long.MAX_VALUE</code> to never use temp files.
	 *
	 * @see  AutoTempFileWriter#DEFAULT_TEMP_FILE_THRESHOLD
     */
    public long getTempFileThreshold() {
        return AutoTempFileWriter.DEFAULT_TEMP_FILE_THRESHOLD;
    }

    @Override
    public void doTag() throws JspException, IOException {
        try {
			final PageContext pageContext = (PageContext)getJspContext();
            final MediaType parentContentType = ThreadEncodingContext.contentType.get();
            final ValidMediaInput parentValidMediaInput = ThreadEncodingContext.validMediaInput.get();

            // Capture the body output while validating
            // BufferWriter bufferWriter = new CharArrayBufferWriter(128, getTempFileThreshold());
            BufferWriter bufferWriter = new SegmentedWriter();
			try {
				// Enable temp files if temp file context active and threshold not Long.MAX_VALUE
				final long tempFileThreshold = getTempFileThreshold();
				if(tempFileThreshold != Long.MAX_VALUE) {
					bufferWriter = TempFileContext.wrapTempFileList(
						bufferWriter,
						pageContext.getRequest(),
						new TempFileContext.Wrapper<BufferWriter>() {
							@Override
							public BufferWriter call(BufferWriter original, TempFileList tempFileList) {
								return new AutoTempFileWriter(
									original,
									tempFileList,
									tempFileThreshold
								);
							}
						}
					);
				}
				if(ENABLE_BUFFER_LOGGING) bufferWriter = new LoggingWriter(bufferWriter, log);
				JspFragment body = getJspBody();
				if(body!=null) {
					final MediaType myContentType = getContentType();
					MediaValidator captureValidator = MediaValidator.getMediaValidator(myContentType, bufferWriter);
					ThreadEncodingContext.contentType.set(myContentType);
					ThreadEncodingContext.validMediaInput.set(captureValidator);
					try {
						invoke(body, captureValidator);
						captureValidator.flush();
					} finally {
						// Restore previous encoding context that is used for our output
						ThreadEncodingContext.contentType.set(parentContentType);
						ThreadEncodingContext.validMediaInput.set(parentValidMediaInput);
					}
				}
			} finally {
				bufferWriter.close();
			}
			final BufferResult capturedBody = bufferWriter.getResult();
			bufferWriter = null; // Done with object, don't need to hold long-term reference

			MediaType myOutputType = getOutputType();
			if(myOutputType==null) {
				// No output, error if anything written.
				doTag(capturedBody, FailOnWriteWriter.getInstance());
			} else {
				final HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
				final JspWriter out = pageContext.getOut();

				// Determine the container's content type
				MediaType containerContentType;
				if(parentContentType!=null) {
					// Use the output type of the parent
					containerContentType = parentContentType;
				} else {
					// Use the content type of the response
					containerContentType = MediaType.getMediaTypeForContentType(response.getContentType());
				}
				// Find the encoder
				MediaEncoder mediaEncoder = MediaEncoder.getInstance(new HttpServletResponseEncodingContext(response), myOutputType, containerContentType);
				if(mediaEncoder!=null) {
					setMediaEncoderOptions(mediaEncoder);
					// Encode our output.  The encoder guarantees valid output for our parent.
					MediaWriter mediaWriter = new MediaWriter(mediaEncoder, out);
					mediaWriter.writePrefix();
					try {
						ThreadEncodingContext.contentType.set(myOutputType);
						ThreadEncodingContext.validMediaInput.set(mediaWriter);
						try {
							doTag(capturedBody, mediaWriter);
						} finally {
							// Restore previous encoding context that is used for our output
							ThreadEncodingContext.contentType.set(parentContentType);
							ThreadEncodingContext.validMediaInput.set(parentValidMediaInput);
						}
					} finally {
						mediaWriter.writeSuffix();
					}
				} else {
					// If parentValidMediaInput exists and is validating our output type, no additional validation is required
					if(
						parentValidMediaInput!=null
						&& parentValidMediaInput.isValidatingMediaInputType(myOutputType)
					) {
						ThreadEncodingContext.contentType.set(myOutputType);
						try {
							doTag(capturedBody, out);
						} finally {
							ThreadEncodingContext.contentType.set(parentContentType);
						}
					} else {
						// Not using an encoder and parent doesn't validate our output, validate our own output.
						MediaValidator validator = MediaValidator.getMediaValidator(myOutputType, out);
						ThreadEncodingContext.contentType.set(myOutputType);
						ThreadEncodingContext.validMediaInput.set(validator);
						try {
							doTag(capturedBody, validator);
						} finally {
							ThreadEncodingContext.contentType.set(parentContentType);
							ThreadEncodingContext.validMediaInput.set(parentValidMediaInput);
						}
					}
				}
			}
        } catch(MediaException err) {
            throw new JspTagException(err);
        }
    }

    /**
     * Sets the media encoder options.  This is how subclass tag attributes
     * can effect the encoding.
     */
    protected void setMediaEncoderOptions(MediaEncoder mediaEncoder) {
    }

	/**
	 * Invokes the body.  This is only called when a body exists.  Subclasses may override this to perform
	 * actions before and/or after invoking the body.  Any overriding implementation should call
	 * super.invoke(JspFragment,MediaValidator) to invoke the body.
	 */
	protected void invoke(JspFragment body, MediaValidator captureValidator) throws JspException, IOException {
		body.invoke(captureValidator);
	}

	/**
     * Once the data is captured, this is called.
     * type, this version of invoke is called.
     */
    abstract protected void doTag(BufferResult capturedBody, Writer out) throws JspException, IOException;
}
