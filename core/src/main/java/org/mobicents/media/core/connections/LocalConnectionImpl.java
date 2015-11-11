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

package org.mobicents.media.core.connections;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.mobicents.media.server.component.audio.AudioComponent;
import org.mobicents.media.server.component.oob.OOBComponent;
import org.mobicents.media.server.impl.rtp.ChannelsManager;
import org.mobicents.media.server.impl.rtp.LocalDataChannel;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionType;
import org.mobicents.media.server.spi.ModeNotSupportedException;
import org.mobicents.media.server.utils.Text;

/**
 *
 * @author yulian oifa
 */
public class LocalConnectionImpl extends BaseConnection {
    
    private static final Logger logger = Logger.getLogger(LocalConnectionImpl.class);

    private LocalDataChannel localAudioChannel;
    
    public LocalConnectionImpl(int id,ChannelsManager channelsManager) {
        super(id,channelsManager.getScheduler());
        this.localAudioChannel=channelsManager.getLocalChannel();
    }
    
    @Override
    public ConnectionType getType() {
        return ConnectionType.LOCAL;
    }
    
    @Override
    public void generateCname() {
    	throw new UnsupportedOperationException("Not supported!");
    }
    
    @Override
    public String getCname() {
    	throw new UnsupportedOperationException("Not supported!");
    }
    
    @Override
    public AudioComponent getAudioComponent() {
    	return this.localAudioChannel.getAudioComponent();
    }
    
    public OOBComponent getOOBComponent() {
    	return this.localAudioChannel.getOOBComponent();
    }
    
    @Override
    public void generateOffer(boolean webrtc) throws IOException {
    	throw new UnsupportedOperationException("Not supported yet!");
    }
    
    @Override
    public void setOtherParty(Connection other) throws IOException {
        if (!(other instanceof LocalConnectionImpl)) {
            throw new IOException("Not compatible");
        }
        
        this.localAudioChannel.join(((LocalConnectionImpl)other).localAudioChannel);

        try {
            join();           
            ((LocalConnectionImpl)other).join();
        } catch (Exception e) {
        	throw new IOException(e);
        }
    }

    @Override
    public void setOtherParty(Text descriptor) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOtherParty(byte[] descriptor) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public long getPacketsReceived() {
        return 0L;
    }

    @Override
    public long getBytesReceived() {
        return 0L;
    }

    @Override
    public long getPacketsTransmitted() {
        return 0L;
    }

    @Override
    public long getBytesTransmitted() {
        return 0L;
    }

    @Override
    public double getJitter() {
        return 0.0;
    }

    @Override
    protected void onFailed() {
    	try {
            setMode(ConnectionMode.INACTIVE);
        } catch (ModeNotSupportedException e) {
            logger.error(toString() + " - Cannot set local connection state to INACTIVE", e);
        }
        
        this.localAudioChannel.unjoin();
        
        // Warn the MGCP stack that this connection failed
        // so it can delete the parent endpoint
        if (this.connectionFailureListener != null) {
            this.connectionFailureListener.onFailure();
        }     
    }

    @Override
    public void setMode(ConnectionMode mode) throws ModeNotSupportedException  {    	
    	localAudioChannel.updateMode(mode);    	
    	super.setMode(mode);
    }

    @Override
    protected void onClosed() {
        try {
            setMode(ConnectionMode.INACTIVE);
        } catch (ModeNotSupportedException e) {
        }
        
        this.localAudioChannel.unjoin();
    }

	public boolean isAvailable() {
		return true;
	}
	
    @Override
    public String toString() {
        return "Local Connection " + getId();
    }

}
