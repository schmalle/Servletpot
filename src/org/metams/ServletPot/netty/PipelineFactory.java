package org.metams.ServletPot.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http2.HttpContentCompressor;
import org.jboss.netty.handler.codec.http2.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http2.HttpResponseEncoder;

import static org.jboss.netty.channel.Channels.pipeline;


/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http2.HttpContentCompressor;
import org.jboss.netty.handler.codec.http2.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http2.HttpResponseEncoder;
import org.metams.ServletPot.EntryNetty;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://openr66.free.fr/">Frederic Bregier</a>
 *
 * @version $Rev: 612 $, $Date: 2010-11-11 19:35:43 +0100 (jeu., 11 nov. 2010) $
 */
public class PipelineFactory implements ChannelPipelineFactory
{

	private EntryNetty m_netty = null;



	/**
	 *
	 * @param x
	 */
	public void setEntryNetty(EntryNetty x)
	{
		m_netty = x;
	}



    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
		RequestHandler rh = new RequestHandler();
		rh.setEntryNetty(m_netty);


        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //pipeline.addLast("ssl", new SslHandler(engine));

        pipeline.addLast("decoder", new HttpRequestDecoder());

        pipeline.addLast("encoder", new HttpResponseEncoder());

        // Remove the following line if you don't want automatic content compression.
        pipeline.addLast("deflater", new HttpContentCompressor());

        pipeline.addLast("handler", rh);
        return pipeline;
    }
}