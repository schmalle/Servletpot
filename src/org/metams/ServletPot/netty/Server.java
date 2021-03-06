package org.metams.ServletPot.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.metams.ServletPot.EntryNetty;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://openr66.free.fr/">Frederic Bregier</a>
 *
 * @version $Rev: 612 $, $Date: 2010-11-11 19:35:43 +0100 (jeu., 11 nov. 2010) $
 */
public class Server
{
    public static void main(String[] args)
	{


		String config =  "/Users/flake/IdeaProjects/ServletPot/web/WEB-INF/config.txt";
		String index =   "/Users/flake/IdeaProjects/ServletPot/web/WEB-INF/index.html";

		System.out.println("Size of arguments: " + args.length);

		if (args.length == 2)
		{
			config = args[0];
			index = args[1];
		}

		System.out.println("Using "+ config + " as path to config file");
		System.out.println("Using "+ index + " as path to html data file file");

		EntryNetty entry = new EntryNetty();
		entry.init(config, index);
		PipelineFactory pf = new PipelineFactory();
		pf.setEntryNetty(entry);


        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                		Executors.newCachedThreadPool(),
                		Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(pf);

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(80));
    }
}
