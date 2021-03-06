/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.mobicents.media.server.io.sdp.attributes;

/**
 * a=sendrecv
 * <p>
 * This specifies that the tools should be started in send and receive mode.<br>
 * This is necessary for interactive conferences with tools that default to
 * receive-only mode. It can be either a session or media-level attribute, and
 * it is not dependent on charset.
 * </p>
 * <p>
 * If none of the attributes "sendonly", "recvonly", "inactive", and "sendrecv"
 * is present, "sendrecv" SHOULD be assumed as the default for sessions that are
 * not of the conference type "broadcast" or "H332".
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class SendRecvAttribute extends AbstractConnectionModeAttribute {

	public static final String ATTRIBUTE_TYPE = "sendrecv";
	private static final String FULL = "a=sendrecv";

	public SendRecvAttribute() {
		super(ATTRIBUTE_TYPE);
	}

	@Override
	public String toString() {
		return FULL;
	}

}
