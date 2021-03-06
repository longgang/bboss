/**
 * 
 */
package org.frameworkset.spi.remote.http;

import org.apache.http.Consts;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.*;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;
import org.frameworkset.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This example demonstrates how to customize and configure the most common aspects
 * of HTTP request execution and connection management.
 */
/**
 * @author yinbp
 *
 * @Date:2016-11-20 11:50:36
 */
public class ClientConfiguration implements InitializingBean,BeanNameAware{
	private static Logger logger = LoggerFactory.getLogger(ClientConfiguration.class);
	private final static int TIMEOUT_CONNECTION = 20000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int RETRY_TIME = 3;
	private  CloseableHttpClient httpclient;
	private static RequestConfig defaultRequestConfig ;
	private RequestConfig requestConfig;
	private  static HttpClient defaultHttpclient;
	private int timeoutConnection = TIMEOUT_CONNECTION;
	private int timeoutSocket = TIMEOUT_SOCKET;
	private int connectionRequestTimeout = TIMEOUT_SOCKET;
	private int retryTime = RETRY_TIME;
	private int maxLineLength = 2000;
	private int maxHeaderCount = 200;
	private int maxTotal = 200; 
	private int defaultMaxPerRoute = 10;
	private long retryInterval = -1;
	private Boolean soKeepAlive = false;
	private Boolean soReuseAddress = false;
	private int validateAfterInactivity = -1;
	private int timeToLive = 3600000;

	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * 默认保活1小时

	 */
	private long keepAlive = 1000l*60l*60l;

	public Boolean getSoKeepAlive() {
		return soKeepAlive;
	}

	public void setSoKeepAlive(Boolean soKeepAlive) {
		this.soKeepAlive = soKeepAlive;
	}

	public Boolean getSoReuseAddress() {
		return soReuseAddress;
	}

	public void setSoReuseAddress(Boolean soReuseAddress) {
		this.soReuseAddress = soReuseAddress;
	}

	public int getValidateAfterInactivity() {
		return validateAfterInactivity;
	}

	public void setValidateAfterInactivity(int validateAfterInactivity) {
		this.validateAfterInactivity = validateAfterInactivity;
	}

	public long getRetryInterval() {
		return retryInterval;
	}


	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}


	private String beanName;
	private static Map<String,ClientConfiguration> clientConfigs = new HashMap<String,ClientConfiguration>();
	private static BaseApplicationContext context;
	private static boolean emptyContext;
	private static void loadClientConfiguration(){
		if(context == null) {
			context = DefaultApplicationContext.getApplicationContext("conf/httpclient.xml");
			emptyContext = context.isEmptyContext();
		}
		
	}


	public static final ContentType TEXT_PLAIN_UTF_8 = ContentType.create(
            "text/plain", Consts.UTF_8);
	/**
	 * 
	 */
	public ClientConfiguration() {
		// TODO Auto-generated constructor stub
	}
	
	
	public  static RequestConfig getDefaultRequestConfig() {
		return defaultRequestConfig;
	}

	public int getTimeoutConnection() {
		return timeoutConnection;
	}

	public void setTimeoutConnection(int timeoutConnection) {
		this.timeoutConnection = timeoutConnection;
	}

	public int getTimeoutSocket() {
		return timeoutSocket;
	}

	public void setTimeoutSocket(int timeoutSocket) {
		this.timeoutSocket = timeoutSocket;
	}

	public int getRetryTime() {
		return retryTime;
	}

	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
	}
	public final CloseableHttpClient getHttpClient()  throws Exception {
		if(httpclient != null)
			return httpclient;
 
//	    HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
//	
//	        @Override
//	        public HttpMessageParser<HttpResponse> create(
//	            SessionInputBuffer buffer, MessageConstraints constraints) {
//	            LineParser lineParser = new BasicLineParser() {
//	
//	                @Override
//	                public Header parseHeader(final CharArrayBuffer buffer) {
//	                    try {
//	                        return super.parseHeader(buffer);
//	                    } catch (ParseException ex) {
//	                        return new BasicHeader(buffer.toString(), null);
//	                    }
//	                }
//	
//	            };
//	            return new DefaultHttpResponseParser(
//	                buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {
//	
//	                @Override
//	                protected boolean reject(final CharArrayBuffer line, int count) {
//	                    // try to ignore all garbage preceding a status line infinitely
//	                    return false;
//	                }
//	
//	            };
//	        }
//	
//	    };
//	    HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();
//	
//	    // Use a custom connection factory to customize the process of
//	    // initialization of outgoing HTTP connections. Beside standard connection
//	    // configuration parameters HTTP connection factory can define message
//	    // parser / writer routines to be employed by individual connections.
//	    HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
//	            requestWriterFactory, responseParserFactory);
	
	    // Client HTTP connection objects when fully initialized can be bound to
	    // an arbitrary network socket. The process of network socket initialization,
	    // its connection to a remote address and binding to a local one is controlled
	    // by a connection socket factory.
	
	    // SSL context for secure connections can be created either based on
	    // system or application specific properties.
	    SSLContext sslcontext = SSLContexts.createSystemDefault();
	
	    // Create a registry of custom connection socket factories for supported
	    // protocol schemes.
	    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
	        .register("http", PlainConnectionSocketFactory.INSTANCE)
	        .register("https", new SSLConnectionSocketFactory(sslcontext))
	        .build();
	
	    // Use custom DNS resolver to override the system DNS resolution.
	    DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
	
	        @Override
	        public InetAddress[] resolve(final String host) throws UnknownHostException {
	            if (host.equalsIgnoreCase("localhost")) {
	                return new InetAddress[] { InetAddress.getByAddress(new byte[] {127, 0, 0, 1}) };
	            } else {
	                return super.resolve(host);
	            }
	        }
	
	    };
	
	    // Create a connection manager with custom configuration.
//	    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
//	            socketFactoryRegistry, connFactory, dnsResolver);

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, null, null, dnsResolver,
				this.timeToLive, TimeUnit.MILLISECONDS);

	    // Create socket configuration
	    SocketConfig socketConfig = SocketConfig.custom()
	        .setTcpNoDelay(true)
	        .setSoTimeout(timeoutSocket)
			.setSoKeepAlive(this.soKeepAlive)
			.setSoReuseAddress(this.soReuseAddress)
	        .build();
	    // Configure the connection manager to use socket configuration either
	    // by default or for a specific host.
	    connManager.setDefaultSocketConfig(socketConfig);
	//	        connManager.setSocketConfig(new HttpHost("localhost", 80), socketConfig);
	    // Validate connections after 1 sec of inactivity
	    connManager.setValidateAfterInactivity(validateAfterInactivity);

	    
	    // Create message constraints
	    MessageConstraints messageConstraints = MessageConstraints.custom()
	        .setMaxHeaderCount(this.maxHeaderCount)
	        .setMaxLineLength(this.maxLineLength)
	        .build();
	    // Create connection configuration
	    ConnectionConfig connectionConfig = ConnectionConfig.custom()
	        .setMalformedInputAction(CodingErrorAction.IGNORE)
	        .setUnmappableInputAction(CodingErrorAction.IGNORE)
	        .setCharset(Consts.UTF_8)
	        .setMessageConstraints(messageConstraints)
	        .build();
	    // Configure the connection manager to use connection configuration either
	    // by default or for a specific host.
	    connManager.setDefaultConnectionConfig(connectionConfig);
	//	        connManager.setConnectionConfig(new HttpHost("localhost", 80), ConnectionConfig.DEFAULT);
	
	    // Configure total max or per route limits for persistent connections
	    // that can be kept in the pool or leased by the connection manager.
	    connManager.setMaxTotal(maxTotal);
	    connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
	//	        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost", 80)), 20);
	
	    // Use custom cookie store if necessary.
	    CookieStore cookieStore = new BasicCookieStore();
	    
	    // Use custom credentials provider if necessary.
	    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	    // Create global request configuration
	    RequestConfig requestConfig = RequestConfig.custom()
	        .setCookieSpec(CookieSpecs.DEFAULT)
	        .setExpectContinueEnabled(true)
	        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
	        .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
	        .setConnectTimeout(this.timeoutConnection).setConnectionRequestTimeout(connectionRequestTimeout)
	        .build();
	
	    // Create an HttpClient with the given custom dependencies and configuration.
	    if(keepAlive > 0)//设置链接保活策略
	    {
	    	HttpConnectionKeepAliveStrategy httpConnectionKeepAliveStrategy = new HttpConnectionKeepAliveStrategy(this.keepAlive);
	    	httpclient = HttpClients.custom()
	    	        .setConnectionManager(connManager)
	    	        .setDefaultCookieStore(cookieStore)
	    	        .setDefaultCredentialsProvider(credentialsProvider)
	    	        //.setProxy(new HttpHost("myproxy", 8080))
	    	        .setDefaultRequestConfig(requestConfig).setKeepAliveStrategy(httpConnectionKeepAliveStrategy)
	    	        .build();
	    }
	    else
	    {
		    httpclient = HttpClients.custom()
		        .setConnectionManager(connManager)
		        .setDefaultCookieStore(cookieStore)
		        .setDefaultCredentialsProvider(credentialsProvider)
		        //.setProxy(new HttpHost("myproxy", 8080))
		        .setDefaultRequestConfig(requestConfig)
		        .build();
	    }
	    if(this.beanName.equals("default")){
	    	defaultRequestConfig = requestConfig;
	    	defaultHttpclient = httpclient;
	    }
	    clientConfigs.put(beanName, this);
		 
        return httpclient;


    }

	public HttpClient getHttpclient() {
		return httpclient;
	}

	public int getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	public int getDefaultMaxPerRoute() {
		return defaultMaxPerRoute;
	}

	public void setDefaultMaxPerRoute(int defaultMaxPerRoute) {
		this.defaultMaxPerRoute = defaultMaxPerRoute;
	}

	/** (non-Javadoc)
	 * @see org.frameworkset.spi.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.getHttpClient();
		
	}

	/** (non-Javadoc)
	 * @see org.frameworkset.spi.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
		
	}

	public static HttpClient getDefaultHttpclient() {
		loadClientConfiguration();
		return getDefaultClientConfiguration().getHttpclient();
	}
	private static ClientConfiguration defaultClientConfiguration;
	public static ClientConfiguration getDefaultClientConfiguration(){
		loadClientConfiguration();
		if(defaultClientConfiguration != null)
			return defaultClientConfiguration;

		if(defaultClientConfiguration == null){

			try {
				defaultClientConfiguration = makeDefualtClientConfiguration("default");
			} catch (Exception e) {
				throw new HttpRuntimeException("Get DefaultClientConfiguration[default] failed:",e);
			}
		}
		return defaultClientConfiguration;
	}
	private static ClientConfiguration makeDefualtClientConfiguration(String name) throws Exception {

		ClientConfiguration clientConfiguration = clientConfigs.get(name);
		if(clientConfiguration != null){
			return clientConfiguration;
		}
		synchronized (ClientConfiguration.class){
			clientConfiguration = clientConfigs.get(name);
			if(clientConfiguration != null){
				return clientConfiguration;
			}
			if(!emptyContext) {
				try {
					clientConfiguration = context.getTBeanObject(name, ClientConfiguration.class);
				}
				catch (SPIException e){
					logger.warn(new StringBuilder().append("Make Defualt ClientConfiguration [").append(name).append("] failed,an internal http pool will been constructed:").append(e.getMessage()).toString());
				}

			}
			if(clientConfiguration == null) {
				clientConfiguration = new ClientConfiguration();
				/**
				 * f:timeoutConnection = "20000"
				 f:timeoutSocket = "20000"
				 f:retryTime = "1"
				 f:maxLineLength = "2000"
				 f:maxHeaderCount = "200"
				 f:maxTotal = "200"
				 f:defaultMaxPerRoute = "10"
				 */
				clientConfiguration.setTimeoutConnection(40000);
				clientConfiguration.setTimeoutSocket(40000);
				clientConfiguration.setConnectionRequestTimeout(40000);
				clientConfiguration.setRetryTime(-1);
				clientConfiguration.setMaxLineLength(Integer.MAX_VALUE);
				clientConfiguration.setMaxHeaderCount(Integer.MAX_VALUE);
				clientConfiguration.setMaxTotal(1000);
				clientConfiguration.setDefaultMaxPerRoute(200);
				clientConfiguration.setBeanName(name);

				clientConfiguration.afterPropertiesSet();
				clientConfigs.put(name, clientConfiguration);
			}
		}
		return clientConfiguration;

	}
	public static ClientConfiguration getClientConfiguration(String poolname){
		loadClientConfiguration();
		if(poolname == null)
			return getDefaultClientConfiguration();
		try {
			return makeDefualtClientConfiguration(poolname);
		} catch (Exception e) {
			throw new HttpRuntimeException("makeDefualtClientConfiguration ["+poolname+"] failed:",e);
		}
//		ClientConfiguration config = clientConfigs.get(poolname);
//		if(config != null)
//			return config;
//		config = context.getTBeanObject(poolname, ClientConfiguration.class);
//		return config;
	}

	public RequestConfig getRequestConfig() {
		return requestConfig;
	}

	public int getMaxLineLength() {
		return maxLineLength;
	}

	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

	public int getMaxHeaderCount() {
		return maxHeaderCount;
	}

	public void setMaxHeaderCount(int maxHeaderCount) {
		this.maxHeaderCount = maxHeaderCount;
	}


	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}


	public void setConnectionRequestTimeout(int connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
	}


	public long getKeepAlive() {
		return keepAlive;
	}


	public void setKeepAlive(long keepAlive) {
		this.keepAlive = keepAlive;
	}
}
