/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * File Name     : CreateConnectionHandle.java
 *
 * The JAIN MGCP API implementaion.
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */

package org.mobicents.protocols.mgcp.stack;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.ModifyConnection;
import jain.protocol.ip.mgcp.message.ModifyConnectionResponse;
import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.NotificationRequestParms;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import jain.protocol.ip.mgcp.message.parms.RequestIdentifier;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;

import org.apache.log4j.Logger;
import org.mobicents.protocols.mgcp.parser.MgcpContentHandler;
import org.mobicents.protocols.mgcp.parser.MgcpMessageParser;
import org.mobicents.protocols.mgcp.parser.Utils;

/**
 * 
 * @author Oleg Kulikov
 * @author Pavel Mitrenko
 * @author amit bhayani
 * @author yulian oifa
 */
public class ModifyConnectionHandler extends TransactionHandler {
	private ModifyConnection command = null;
	private ModifyConnectionResponse response = null;

	private static final Logger logger = Logger.getLogger(ModifyConnectionHandler.class);

	/** Creates a new instance of ModifyConnectionHandler */
	public ModifyConnectionHandler(JainMgcpStackImpl stack) {
		super(stack);
	}

	public ModifyConnectionHandler(JainMgcpStackImpl stack, InetAddress address, int port) {
		super(stack, address, port);
	}

	public JainMgcpCommandEvent decodeCommand(String message) throws ParseException {
		Utils utils = utilsFactory.allocate();
		MgcpMessageParser parser = new MgcpMessageParser(new CommandContentHandle(utils));
		try {
			parser.parse(message);
		} catch (IOException e) {
			logger.error("Decoding of MDCX command failed", e);
		} finally {
			utilsFactory.deallocate(utils);
		}

		NotifiedEntity notifiedEntity = command.getNotifiedEntity();
		if (command.getNotifiedEntity() != null) {
			this.stack.provider.setNotifiedEntity(notifiedEntity);
		}
		return command;
	}

	public JainMgcpResponseEvent decodeResponse(String message) throws ParseException {
		Utils utils = utilsFactory.allocate();
		MgcpMessageParser parser = new MgcpMessageParser(new ResponseContentHandle(utils));
		try {
			parser.parse(message);
		} catch (IOException e) {
			logger.error("Decoding of MDCX Response failed", e);
		} finally {
			utilsFactory.deallocate(utils);
		}

		return response;
	}

	public String encode(JainMgcpCommandEvent event) {

		// encode message header
		Utils utils = utilsFactory.allocate();
		ModifyConnection evt = (ModifyConnection) event;
		StringBuffer s = new StringBuffer();
		s.append("MDCX ").append(evt.getTransactionHandle()).append(SINGLE_CHAR_SPACE).append(
				evt.getEndpointIdentifier()).append(SINGLE_CHAR_SPACE).append(MGCP_VERSION).append(NEW_LINE);

		// encode mandatory parameters

		s.append("C:").append(evt.getCallIdentifier()).append(NEW_LINE);
		s.append("I:").append(evt.getConnectionIdentifier()).append(NEW_LINE);

		// encode optional parameters

		if (evt.getBearerInformation() != null) {
			s.append("B:e:").append(evt.getBearerInformation()).append(NEW_LINE);
		}

		if (evt.getLocalConnectionOptions() != null) {
			s.append("L:").append(utils.encodeLocalOptionValueList(evt.getLocalConnectionOptions())).append(NEW_LINE);
		}

		if (evt.getMode() != null) {
			s.append("M:").append(evt.getMode()).append(NEW_LINE);
		}

		if (evt.getNotificationRequestParms() != null) {
			s.append(utils.encodeNotificationRequestParms(evt.getNotificationRequestParms())).append(NEW_LINE);
		}

		if (evt.getNotifiedEntity() != null) {
			s.append("N:").append(evt.getNotifiedEntity()).append(NEW_LINE);
		}

		if (evt.getRemoteConnectionDescriptor() != null) {
			s.append(NEW_LINE).append(evt.getRemoteConnectionDescriptor());
		}
		utilsFactory.deallocate(utils);
		return s.toString();
	}

	public String encode(JainMgcpResponseEvent event) {
		ModifyConnectionResponse response = (ModifyConnectionResponse) event;
		ReturnCode returnCode = response.getReturnCode();
		StringBuffer s = new StringBuffer();
		s.append(returnCode.getValue()).append(SINGLE_CHAR_SPACE).append(response.getTransactionHandle()).append(
				SINGLE_CHAR_SPACE).append(returnCode.getComment()).append(NEW_LINE);

		if (response.getLocalConnectionDescriptor() != null) {
			s.append(NEW_LINE).append(response.getLocalConnectionDescriptor());
		}

		return s.toString();
	}

	private class CommandContentHandle implements MgcpContentHandler {
		private Utils utils = null;

		public CommandContentHandle(Utils utils) {
			this.utils = utils;
		}

		/**
		 * Receive notification of the header of a message. Parser will call
		 * this method to report about header reading.
		 * 
		 * @param header
		 *            the header from the message.
		 */
		public void header(String header) throws ParseException {

			command = new ModifyConnection(source != null ? source : stack, new CallIdentifier("00"), endpoint,
					new ConnectionIdentifier("00"));
			command.setTransactionHandle(remoteTID);
		}

		/**
		 * Receive notification of the parameter of a message. Parser will call
		 * this method to report about parameter reading.
		 * 
		 * @param name
		 *            the name of the paremeter
		 * @param value
		 *            the value of the parameter.
		 */
		public void param(String name, String value) throws ParseException {
			if(name.length()==1)
			{
				switch(name.charAt(0))
				{
					case 'b':
					case 'B':
						command.setBearerInformation(utils.decodeBearerInformation(value));		
						break;
					case 'c':
					case 'C':
						command.setCallIdentifier(new CallIdentifier(value));
						break;
					case 'i':
					case 'I':
						command.setConnectionIdentifier(new ConnectionIdentifier(value));
						break;
					case 'm':
					case 'M':
						command.setMode(utils.decodeConnectionMode(value));
						break;
					case 'l':
					case 'L':
						command.setLocalConnectionOptions(utils.decodeLocalOptionValueList(value));		
						break;
					case 'n':
					case 'N':
						command.setNotifiedEntity(utils.decodeNotifiedEntity(value, true));		
						break;
					case 'x':
					case 'X':
						command.setNotificationRequestParms(new NotificationRequestParms(new RequestIdentifier(value)));		
						break;
					case 'r':
					case 'R':
						command.getNotificationRequestParms().setRequestedEvents(utils.decodeRequestedEventList(value));		
						break;
					case 's':
					case 'S':
						command.getNotificationRequestParms().setSignalRequests(utils.decodeEventNames(value));		
						break;
					case 't':
					case 'T':
						command.getNotificationRequestParms().setDetectEvents(utils.decodeEventNames(value));
						break;
					default:
						logger.error("Unknown code while encoding MDCX Command name = " + name + " value = " + value);						
						break;						
				}
			}
			else
				logger.error("Unknown code while encoding MDCX Command name = " + name + " value = " + value);
		}

		/**
		 * Receive notification of the session description. Parser will call
		 * this method to report about session descriptor reading.
		 * 
		 * @param sd
		 *            the session description from message.
		 */
		public void sessionDescription(String sd) throws ParseException {
			command.setRemoteConnectionDescriptor(new ConnectionDescriptor(sd));
		}
	}

	private class ResponseContentHandle implements MgcpContentHandler {
		private Utils utils;

		public ResponseContentHandle(Utils utils) {
			this.utils = utils;
		}

		/**
		 * Receive notification of the header of a message. Parser will call
		 * this method to report about header reading.
		 * 
		 * @param header
		 *            the header from the message.
		 */
		public void header(String header) throws ParseException {
			String[] tokens = utils.splitStringBySpace(header);
			int tid = Integer.parseInt(tokens[1]);

			response = new ModifyConnectionResponse(source != null ? source : stack, utils.decodeReturnCode(Integer
					.parseInt(tokens[0])));
			response.setTransactionHandle(tid);
		}

		/**
		 * Receive notification of the parameter of a message. Parser will call
		 * this method to report about parameter reading.
		 * 
		 * @param name
		 *            the name of the paremeter
		 * @param value
		 *            the value of the parameter.
		 */
		public void param(String name, String value) throws ParseException {
			// hasn't parameters
		}

		/**
		 * Receive notification of the session description. Parser will call
		 * this method to report about session descriptor reading.
		 * 
		 * @param sd
		 *            the session description from message.
		 */
		public void sessionDescription(String sd) throws ParseException {
			response.setLocalConnectionDescriptor(new ConnectionDescriptor(sd));
		}
	}

	@Override
	public JainMgcpResponseEvent getProvisionalResponse() {
		ModifyConnectionResponse provisionalresponse = null;
		if (!sent) {
			provisionalresponse = new ModifyConnectionResponse(source != null ? source : stack,
					ReturnCode.Transaction_Being_Executed);
		}
		return provisionalresponse;
	}

}