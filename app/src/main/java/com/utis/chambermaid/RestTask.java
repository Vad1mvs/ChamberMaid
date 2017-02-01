package com.utis.chambermaid;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import javax.net.ssl.SSLSocketFactory;

public class RestTask extends AsyncTask<HttpUriRequest, Void, Object> {
    private static final String TAG = "RestTask";
    private static final int CONNECTION_TIMEOUT = 20*1000;
    public static final int SOCKET_TIMEOUT_MIN = 18*1000;
    public static int SOCKET_TIMEOUT = 18*1000;
    public interface ResponseCallback {
        public void onRequestSuccess(String response);
        public void onRequestError(Exception error);
    }

    private AbstractHttpClient mClient;
    private WeakReference<ResponseCallback> mCallback;

    public RestTask() {
        this(new DefaultHttpClient());
/*
		DefaultHttpClient client = new DefaultHttpClient();
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 8443));
		SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		mClient = new DefaultHttpClient(mgr, client.getParams());
*/

/*
		mClient = new DefaultHttpClient();
		SSLContext ctx = SSLContext.getInstance("TLS");
		X509TrustManager tm = new X509TrustManager() {
		    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException { }

		    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException { }

		    public X509Certificate[] getAcceptedIssuers() {
		        return null;
		    }
		};
		ctx.init(null, new TrustManager[]{tm}, null);
		SSLSocketFactory ssf = new SSLSocketFactory(ctx,  org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		mClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", (SocketFactory) ssf, 443));
*/

    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true; //false;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    public RestTask(AbstractHttpClient client) {
//		mClient = client;
//		mClient = (AbstractHttpClient) createHttpClient();
        mClient = (AbstractHttpClient) sslClient(client);
    }

    public void setResponseCallback(ResponseCallback callback) {
        mCallback = new WeakReference<ResponseCallback>(callback);
    }

    @Override
    protected Object doInBackground(HttpUriRequest... params) {
        try {
//			mClient.getConnectionManager().getSchemeRegistry().register(new Scheme("SSLSocketFactory", SSLSocketFactory.getSocketFactory(), 443));
//			mClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
//			trustEveryone();
            HttpUriRequest request = params[0];
            HttpResponse serverResponse = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String response = handler.handleResponse(serverResponse);
            return response;
        } catch (Exception e) {
            Log.w(TAG, e);
            return e;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (mCallback != null && mCallback.get() != null) {
            if (result instanceof String) {
                mCallback.get().onRequestSuccess((String) result);
            } else if (result instanceof Exception) {
                mCallback.get().onRequestError((Exception) result);
            } else {
                mCallback.get().onRequestError(new IOException("Unknown Error Contacting Host"));
            }
        }
    }


    /**
     * The server has a SSL certificate. This method add SSL certificate to HTTP
     * Request
     */
/*
	private static void addSSLCertificateToHttpRequest() {
	    // Code to use verifier which return true.
	    try {
	        SSLContext sslctx = null;
	        try {
	            sslctx = SSLContext.getInstance("TLS");
	            sslctx.init(null, new TrustManager[] { new X509TrustManager() {
	                public void checkClientTrusted(X509Certificate[] chain, String authType)
	                {
	                }

	                public void checkServerTrusted(X509Certificate[] chain, String authType)
	                {
	                }

	                public X509Certificate[] getAcceptedIssuers() {
	                    return new X509Certificate[] {};
	                }
	            } }, null);
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        HttpsURLConnection.setDefaultSSLSocketFactory(sslctx.getSocketFactory());
	        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        });
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	}
*/

    private HttpClient createHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);

            SchemeRegistry schReg = new SchemeRegistry();
//            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", sf, 443));
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

            return new DefaultHttpClient(conMgr, params);

        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    private HttpClient sslClient(HttpClient client) {
        try {
            CustomX509TrustManager tm = new CustomX509TrustManager();
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));

            BasicHttpParams httpParams = (BasicHttpParams) client.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT); // Connection timeout
            HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT); // Socket timeout

            return new DefaultHttpClient(ccm, httpParams/*client.getParams()*/);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        }
    }


    /**
     * Taken from: http://janis.peisenieks.lv/en/76/english-making-an-ssl-connection-via-android/
     *
     */
    public class CustomSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new CustomX509TrustManager();

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        public CustomSSLSocketFactory(SSLContext context)
                throws KeyManagementException, NoSuchAlgorithmException,
                KeyStoreException, UnrecoverableKeyException {
            super(null);
            sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port,
                    autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public class CustomX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs,
                                       String authType) throws CertificateException {

            // Here you can verify the servers certificate. (e.g. against one which is stored on mobile device)

            // InputStream inStream = null;
            // try {
            // inStream = MeaApplication.loadCertAsInputStream();
            // CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // X509Certificate ca = (X509Certificate)
            // cf.generateCertificate(inStream);
            // inStream.close();
            //
            // for (X509Certificate cert : certs) {
            // // Verifing by public key
            // cert.verify(ca.getPublicKey());
            // }
            // } catch (Exception e) {
            // throw new IllegalArgumentException("Untrusted Certificate!");
            // } finally {
            // try {
            // inStream.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }


} 