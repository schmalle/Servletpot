package org.metams.ServletPot.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http2.*;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;



/*
* Copyright 2009 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0 (the
* "License"); you may not use this file except in compliance with the License.
* You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http2.Attribute;
import org.jboss.netty.handler.codec.http2.Cookie;
import org.jboss.netty.handler.codec.http2.CookieDecoder;
import org.jboss.netty.handler.codec.http2.CookieEncoder;
import org.jboss.netty.handler.codec.http2.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http2.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http2.DiskAttribute;
import org.jboss.netty.handler.codec.http2.DiskFileUpload;
import org.jboss.netty.handler.codec.http2.HttpChunk;
import org.jboss.netty.handler.codec.http2.HttpDataFactory;
import org.jboss.netty.handler.codec.http2.HttpHeaders;
import org.jboss.netty.handler.codec.http2.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http2.HttpRequest;
import org.jboss.netty.handler.codec.http2.HttpResponse;
import org.jboss.netty.handler.codec.http2.HttpResponseStatus;
import org.jboss.netty.handler.codec.http2.HttpVersion;
import org.jboss.netty.handler.codec.http2.InterfaceHttpData;
import org.jboss.netty.handler.codec.http2.QueryStringDecoder;
import org.jboss.netty.handler.codec.http2.HttpPostRequestDecoder.EndOfDataDecoderException;
import org.jboss.netty.handler.codec.http2.HttpPostRequestDecoder.ErrorDataDecoderException;
import org.jboss.netty.handler.codec.http2.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import org.jboss.netty.handler.codec.http2.HttpPostRequestDecoder.NotEnoughDataDecoderException;
import org.jboss.netty.handler.codec.http2.InterfaceHttpData.HttpDataType;
import org.jboss.netty.util.CharsetUtil;
import org.metams.ServletPot.ConfigHandler;
import org.metams.ServletPot.EntryNetty;
import org.metams.ServletPot.tools.Utils;

import javax.servlet.ServletConfig;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://openr66.free.fr/">Frederic Bregier</a>
 * @version $Rev: 645 $, $Date: 2009-10-25 01:26:23 +0200 (dim., 25 oct. 2009)
 *          $
 */
public class RequestHandler extends SimpleChannelUpstreamHandler
{

	private Utils m_utils = new Utils(null);
	private EntryNetty m_entry = null;
	private Hashtable m_URIStore = new Hashtable();

    private volatile HttpRequest request;

    private volatile boolean readingChunks = false;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(
            DefaultHttpDataFactory.MINSIZE); // Disk if size exceed MINSIZE

    private HttpPostRequestDecoder decoder = null;

	public void setEntryNetty(EntryNetty x)
	{
		m_entry = x;
	}


    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelClosed(org
     * .jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception
    {
        if (decoder != null)
        {
            decoder.cleanFiles();
        }
    }


    /**
     * @param request
     * @return
     */
    private URI getURI(HttpRequest request)
    {
        URI uri = null;
        try
        {
            uri = new URI(request.getUri());
        } catch (URISyntaxException e2)
        {
            uri = null;
        }

        return uri;
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
    {

        StringBuilder responseContent = new StringBuilder();

        Hashtable getParameter = new Hashtable();
         Hashtable postParameter = new Hashtable();
        HttpResponseStatus status = HttpResponseStatus.OK;
        String uri = null;
		String ip = null;
		String method = null;
		String host = "";



        if (!readingChunks)
        {

            HttpRequest request = this.request = (HttpRequest) e.getMessage();
			method = request.getMethod().getName().toLowerCase();
            uri = getURI(request).toString();
			ip = e.getRemoteAddress().toString();

			int index = ip.indexOf(":");
			if (index != -1)
				ip = ip.substring(1, index);

            responseContent.setLength(0);

            // new method
            List<Map.Entry<String, String>> headers = request.getHeaders();
            for (Map.Entry<String, String> entry : headers)
            {

				if (entry.getKey().toLowerCase().equals("host"))
				{
					host = entry.getValue();
				}

            }





            getParameter = extractGETParameter(getParameter, request);

            try
            {
                if (request.getMethod().getName().toLowerCase().equals("post"))
                    decoder = new HttpPostRequestDecoder(factory, request);
            } catch (Exception e2)
            {
                e2.printStackTrace();
            }


            if (!request.isChunked())
            {
                // Not chunk version
                readHttpDataAllReceive(e.getChannel(), status, uri, getParameter, postParameter, responseContent, ip, method, host);
                writeResponse(e.getChannel(), status, uri, getParameter, postParameter, responseContent, ip, method, host);
            }
        }
        else
        {
            // New chunk is received
            HttpChunk chunk = (HttpChunk) e.getMessage();
            try
            {
                decoder.offer(chunk);
            }
            catch (HttpPostRequestDecoder.ErrorDataDecoderException e1)
            {
                writeResponse(e.getChannel(), status, uri, getParameter, postParameter, responseContent, ip, method, host);
                Channels.close(e.getChannel());
                return;
            }

            postParameter = readHttpDataChunkByChunk(e.getChannel(), postParameter);

            if (chunk.isLast())
            {
                if (null == readHttpDataAllReceive(e.getChannel(), status, uri, getParameter, postParameter, responseContent, ip, method, host))
                    writeResponse(e.getChannel(), status, uri, getParameter, postParameter, responseContent, ip, method, host);

                readingChunks = false;
            }
        }
    }


    /**
     * extracts all get parameters
     * @param hash
     * @param request
     * @return
     */
    public Hashtable extractGETParameter(Hashtable hash, HttpRequest request)
    {
        QueryStringDecoder decoderQuery = new QueryStringDecoder(request
                .getUri());
        Map<String, List<String>> uriAttributes = decoderQuery
                .getParameters();
        for (String key : uriAttributes.keySet())
        {
            for (String valuen : uriAttributes.get(key))
            {

                String val = (String) hash.get(key);
                if (val == null)
                    hash.put(key, valuen);

            }
        }

        return hash;
    }   // extractGETParameter

    /**
     * Example of reading all InterfaceHttpData from finished transfer
     *
     * @param channel
     */
    private Hashtable readHttpDataAllReceive(Channel channel, HttpResponseStatus status,
                                             String uri, Hashtable get, Hashtable post, StringBuilder responseContent,
											 String ip, String method, String host)
    {

		if (!method.equalsIgnoreCase("post"))
			return post;

        List<InterfaceHttpData> datas = null;
        try
        {
          datas = decoder.getBodyHttpDatas();
        } catch (HttpPostRequestDecoder.NotEnoughDataDecoderException e1)
        {
            // Should not be!
            e1.printStackTrace();
            responseContent.append(e1.getMessage());
            writeResponse(channel, status, uri, get, post, responseContent, ip, method, host);

            Channels.close(channel);
            return null;
        }
        for (InterfaceHttpData data : datas)
        {
            post = extractPostValue(data, post);
        }

        return post;
    }

    /**
     * Example of reading request by chunk and getting values from chunk to
     * chunk
     *
     * @param channel
     */
    private Hashtable readHttpDataChunkByChunk(Channel channel, Hashtable hash)
    {

        try
        {
            while (decoder.hasNext())
            {
                InterfaceHttpData data = decoder.next();
                if (data != null)
                {
                    // new value
                    hash = extractPostValue(data, hash);
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1)
        {
        }


        return hash;
    }


    /**
     * @param data
     * @param hash
     * @return
     */
    private Hashtable extractPostValue(InterfaceHttpData data, Hashtable hash)
    {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute)
        {
            Attribute attribute = (Attribute) data;
            String value;
            String name;
            try
            {
                value = attribute.getValue();
                name = attribute.getName();
                hash.put(name, value);
            } catch (IOException e1)
            {
                // Error while reading data from File, only print name and error
                e1.printStackTrace();
            }
        }

        return hash;
    }


    /**
     *
     * @param channel
     * @param status
     * @param uri
     * @param get
     * @param post
     */
    private void writeResponse(Channel channel, HttpResponseStatus status, String uri, Hashtable get, Hashtable post,
							   StringBuilder responseContent, String ip, String method, String host)
    {





		long shortURI = m_utils.getCRC32(uri, false);
		long ipShort = m_utils.getCRC32(ip, false);

		Object knownIP = m_URIStore.get((Object)shortURI);
		if (knownIP == null)
		{
			m_URIStore.put((Object)shortURI, (Object)ipShort);
			m_entry.doGet(get, post, ip, uri, responseContent, method, host);

		}
		else if (ipShort == ((Long)knownIP).longValue())
		{
			responseContent = m_entry.returnDefaultPage(responseContent, null, 0, null, null, null, null);

		}
		else
		{
			m_entry.doGet(get, post, ip, uri, responseContent, method, host);
		}



        // Convert the response content to a ChannelBuffer.
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent
				.toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);

        // Decide whether to close the connection or not.
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
                .getHeader(HttpHeaders.Names.CONNECTION)) ||
                request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) &&
                        !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
                                .getHeader(HttpHeaders.Names.CONNECTION));

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                status);

        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
                "text/html; charset=UTF-8");

        if (!close)
        {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String
                    .valueOf(buf.readableBytes()));
        }

       // Write the response.
        ChannelFuture future = channel.write(response);
        // Close the connection after the write operation is done if necessary.
        if (close)
        {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception
    {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}



