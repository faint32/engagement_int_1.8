

package com.netease.common.http.Entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

/**
 * Implements a request entity suitable for an HTTP multipart POST method.
 * <p>
 * The HTTP multipart POST method is defined in section 3.3 of
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC1867</a>:
 * <blockquote>
 * The media-type multipart/form-data follows the rules of all multipart
 * MIME data streams as outlined in RFC 1521. The multipart/form-data contains 
 * a series of parts. Each part is expected to contain a content-disposition 
 * header where the value is "form-data" and a name attribute specifies 
 * the field name within the form, e.g., 'content-disposition: form-data; 
 * name="xxxxx"', where xxxxx is the field name corresponding to that field.
 * Field names originally in non-ASCII character sets may be encoded using 
 * the method outlined in RFC 1522.
 * </blockquote>
 * </p>
 * <p>This entity is designed to be used in conjunction with the 
 * {@link org.apache.http.HttpRequest} to provide
 * multipart posts.  Example usage:</p>
 * <pre>
 *  File f = new File("/path/fileToUpload.txt");
 *  HttpRequest request = new HttpRequest("http://host/some_path");
 *  Part[] parts = {
 *      new StringPart("param_name", "value"),
 *      new FilePart(f.getName(), f)
 *  };
 *  filePost.setEntity(
 *      new MultipartRequestEntity(parts, filePost.getParams())
 *      );
 *  HttpClient client = new HttpClient();
 *  int status = client.executeMethod(filePost);
 * </pre>
 * 
 * @since 3.0
 */
public class MultipartEntity extends AbstractHttpEntity {

//    private static final Log log = LogFactory.getLog(MultipartEntity.class);
    
    /** The Content-Type for multipart/form-data. */
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
    
    /**
     * Sets the value to use as the multipart boundary.
     * <p>
     * This parameter expects a value if type {@link String}.
     * </p>
     */
    public static final String MULTIPART_BOUNDARY = "http.method.multipart.boundary";
    
    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private static byte[] MULTIPART_CHARS = EncodingUtils.getAsciiBytes(
        "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    
    /**
     * Generates a random multipart boundary string.
    */
    public static byte[] generateMultipartBoundary() {
        Random rand = new Random();
        byte[] bytes = new byte[rand.nextInt(11) + 30]; // a random size from 30 to 40
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)];
        }
        return bytes;
    }
    
    /** The MIME parts as set by the constructor */
    protected Part[] parts;
    
    private byte[] multipartBoundary;
    
    private HttpParams params;
    
    private boolean contentConsumed = false;
    
    /**
     * Creates a new multipart entity containing the given parts.
     * @param parts The parts to include.
     * @param params The params of the HttpMethod using this entity.
     */
    public MultipartEntity(Part[] parts, HttpParams params) {      
      if (parts == null) {
          throw new IllegalArgumentException("parts cannot be null");
      }
      if (params == null) {
          throw new IllegalArgumentException("params cannot be null");
      }
      this.parts = parts;
      this.params = params;
    }
    
    public MultipartEntity(Part[] parts) {
      setContentType(MULTIPART_FORM_CONTENT_TYPE);
      if (parts == null) {
          throw new IllegalArgumentException("parts cannot be null");
      }
      this.parts = parts;
      this.params = null;
    }
    
    /**
     * Returns the MIME boundary string that is used to demarcate boundaries of
     * this part. The first call to this method will implicitly create a new
     * boundary string. To create a boundary string first the 
     * HttpMethodParams.MULTIPART_BOUNDARY parameter is considered. Otherwise 
     * a random one is generated.
     * 
     * @return The boundary string of this entity in ASCII encoding.
     */
    protected byte[] getMultipartBoundary() {
        if (multipartBoundary == null) {
            String temp = null;
            if (params != null) {
              temp = (String) params.getParameter(MULTIPART_BOUNDARY);
            }
            if (temp != null) {
                multipartBoundary = EncodingUtils.getAsciiBytes(temp);
            } else {
                multipartBoundary = generateMultipartBoundary();
            }
        }
        return multipartBoundary;
    }

    /**
     * Returns <code>true</code> if all parts are repeatable, <code>false</code> otherwise.
     */
    public boolean isRepeatable() {
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isRepeatable()) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     */
    public void writeTo(OutputStream out) throws IOException {
//    	if (out != null) {
//    		android.util.Log.v("multipartEntity", "out != null");
//    	}
        Part.sendParts(out, parts, getMultipartBoundary());
    }
    /* (non-Javadoc)
     * @see org.apache.commons.http.AbstractHttpEntity.#getContentType()
     */
    @Override
    public Header getContentType() {
      StringBuffer buffer = new StringBuffer(MULTIPART_FORM_CONTENT_TYPE);
      buffer.append("; boundary=");
      buffer.append(EncodingUtils.getAsciiString(getMultipartBoundary()));
      return new BasicHeader(HTTP.CONTENT_TYPE, buffer.toString());

    }

    /* (non-Javadoc)
     */
    public long getContentLength() {
        try {
            return Part.getLengthOfParts(parts, getMultipartBoundary());            
        } catch (Exception e) {
 //           log.error("An exception occurred while getting the length of the parts", e);
            return 0;
        }
    }    
 
    public InputStream getContent() throws IOException, IllegalStateException {
          if(!isRepeatable() && this.contentConsumed ) {
              throw new IllegalStateException("Content has been consumed");
          }
          this.contentConsumed = true;
          
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          Part.sendParts(baos, this.parts, this.multipartBoundary);
          ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
          return bais;
    }
  
    public boolean isStreaming() {
        return false;
    }
}
