package org.mobicents.media.server.io.sdp.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mobicents.media.server.io.sdp.fields.AttributeField;

/**
 * a=ssrc:[ssrc-id] [attribute]<br>
 * a=ssrc:[ssrc-id] [attribute]:[value]
 * 
 * <p>
 * The SDP media attribute "ssrc" indicates a property (known as a
 * "source-level attribute") of a media source (RTP stream) within an RTP
 * session.
 * </p>
 * <p>
 * <b>ssrc-id</b> is the synchronization source (SSRC) ID of the source being
 * described, interpreted as a 32-bit unsigned integer in network byte order and
 * represented in decimal.
 * </p>
 * <p>
 * <b>attribute</b> or <b>attribute:value</b> represents the source-level
 * attribute specific to the given media source.<br>
 * The source-level attribute follows the syntax of the SDP "a=" line. It thus
 * consists of either a single attribute name (a flag) or an attribute name and
 * value, e.g., "cname:user@example.com". No attributes of the former type are
 * defined by this document.
 * </p>
 * <p>
 * Within a media stream, "ssrc" attributes with the same value of <ssrc-id>
 * describe different attributes of the same media sources. Across media
 * streams, <ssrc-id> values are not correlated (unless correlation is indicated
 * by media-stream grouping or some other mechanism) and MAY be repeated.
 * </p>
 * <p>
 * Each "ssrc" media attribute specifies a single source-level attribute for the
 * given <ssrc-id>. For each source mentioned in SDP, the source-level attribute
 * "cname" MUST be provided. Any number of other source-level attributes for the
 * source MAY also be provided.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5576">RFC5576</a>
 */
public class SsrcAttribute extends AttributeField {

	public static final String ATTRIBUTE_TYPE = "ssrc";
	public static final String BEGIN = "a=ssrc:";
	public static final String NEWLINE = "\n";
	
	private String ssrcId;
	private final Map<String, String> attributes;

	public SsrcAttribute(String ssrcId) {
		super(ATTRIBUTE_TYPE);
		this.ssrcId = ssrcId;
		this.attributes = new HashMap<String, String>(3);
	}

	public void setSsrcId(String ssrcId) {
		this.ssrcId = ssrcId;
	}
	
	public String getSsrcId() {
		return ssrcId;
	}
	
	public String getAttributeValue(String attribute) {
		return this.attributes.get(attribute);
	}
	
	public void addAttribute(String attribute, String value) {
		this.attributes.put(attribute, value);
	}
	
	public void reset() {
		this.ssrcId = null;
		this.attributes.clear();
	}
	
	@Override
	public String toString() {
		super.builder.setLength(0);		
		for (Entry<String, String> value : this.attributes.entrySet()) {
			super.builder.append(BEGIN).append(this.ssrcId).append(" ").append(value.getKey());
			if(value.getValue() != null && !value.getValue().isEmpty()) {
				super.builder.append(ATTRIBUTE_SEPARATOR).append(value.getValue()).append(NEWLINE);
			}
		}
		super.builder.deleteCharAt(builder.length() - 1);
		return super.builder.toString();
	}
}