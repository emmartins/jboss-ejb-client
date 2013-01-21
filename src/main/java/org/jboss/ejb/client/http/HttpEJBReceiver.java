package org.jboss.ejb.client.http;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.ejb.client.Affinity;
import org.jboss.ejb.client.AttachmentKeys;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.jboss.ejb.client.EJBReceiver;
import org.jboss.ejb.client.EJBReceiverContext;
import org.jboss.ejb.client.EJBReceiverInvocationContext;
import org.jboss.ejb.client.StatefulEJBLocator;
import org.jboss.ejb.client.remoting.MethodInvocationMessageWriter;
import org.jboss.ejb.client.remoting.ProtocolV1ClassTable;
import org.jboss.ejb.client.remoting.ProtocolV1ObjectTable;
import org.jboss.marshalling.AbstractClassResolver;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;
import org.jboss.marshalling.reflect.SunReflectiveCreator;

public class HttpEJBReceiver extends EJBReceiver {

 // TODO: The version and the marshalling strategy shouldn't be hardcoded here
    private final byte clientProtocolVersion = 0x01;
    private final String clientMarshallingStrategy = "river";
    private final MarshallerFactory marshallerFactory;

    public HttpEJBReceiver(String nodeName) {
        super(nodeName);
        this.marshallerFactory = Marshalling.getProvidedMarshallerFactory(this.clientMarshallingStrategy);
        if (this.marshallerFactory == null) {
            throw new RuntimeException("Could not find a marshaller factory for " + this.clientMarshallingStrategy + " marshalling strategy");
        }
    }

    @Override
    protected void associate(EJBReceiverContext context) {
        // TODO Auto-generated method stub

    }

    public boolean registerModule2(String appName, String moduleName, String distinctName) {
        return registerModule(appName, moduleName, distinctName);
    }

    public boolean deregisterModule2(String appName, String moduleName, String distinctName) {
        return deregisterModule(appName, moduleName, distinctName);
    }

    @Override
    protected void processInvocation(EJBClientInvocationContext clientInvocationContext,
            EJBReceiverInvocationContext receiverContext) throws Exception {

        final MethodInvocationMessageWriter messageWriter = new MethodInvocationMessageWriter(this.clientProtocolVersion, this.marshallerFactory);
        /*
        ByteArrayOutputStream baos = null;
        DataOutputStream output = null;
        try {
            baos = new ByteArrayOutputStream();
            output = new DataOutputStream(baos);
            output.writeByte(clientProtocolVersion);
            output.writeUTF(clientMarshallingStrategy);
            messageWriter.writeMessage(output, (short)1, clientInvocationContext);
        } finally {
             if (output != null)
                 output.close();
             if (baos != null)
                 baos.close();
        }
        ByteArrayEntity entity = new ByteArrayEntity(baos.toByteArray());

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(getNodeName());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        DataInputStream input = null;
        try {
            input = new DataInputStream(response.getEntity().getContent());
            // read the invocation id
            final short invocationId = input.readShort();
            // create a ResultProducer which can unmarshall and return the result, later
            final EJBReceiverInvocationContext.ResultProducer resultProducer = new MethodInvocationResultProducer(clientInvocationContext, input);
            receiverContext.resultReady(resultProducer);
            input = null;
        } finally {
            if (input != null)
                input.close();
       }*/
       URLConnection connection = new URL(getNodeName()).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type","application/octet-stream");
        DataOutputStream output = null;
        try {
             output = new DataOutputStream(connection.getOutputStream());
             output.writeByte(clientProtocolVersion);
             output.writeUTF(clientMarshallingStrategy);
             messageWriter.writeMessage(output, (short)1, clientInvocationContext);
        } finally {
             if (output != null)
                 output.close();
        }
        DataInputStream input = null;
        try {
            input = new DataInputStream(connection.getInputStream());
            final int header = input.read();
            // read the invocation id
            final short invocationId = input.readShort();
            // create a ResultProducer which can unmarshall and return the result, later
            final EJBReceiverInvocationContext.ResultProducer resultProducer = new MethodInvocationResultProducer(clientInvocationContext, input);
            receiverContext.resultReady(resultProducer);
            input = null;
        } finally {
            if (input != null)
                input.close();
       }
    }

    /**
     * A result producer which parses a input stream and returns a method invocation response as a result
     */
    private class MethodInvocationResultProducer implements EJBReceiverInvocationContext.ResultProducer {

        private final DataInputStream input;
        private final EJBClientInvocationContext clientInvocationContext;

        MethodInvocationResultProducer(final EJBClientInvocationContext clientInvocationContext, final DataInputStream input) {
            this.input = input;
            this.clientInvocationContext = clientInvocationContext;
        }

        @Override
        public Object getResult() throws Exception {
            try {
                // prepare the unmarshaller
                final Unmarshaller unmarshaller = HttpEJBReceiver.this.prepareForUnMarshalling(HttpEJBReceiver.this.marshallerFactory, this.input);
                // read the result
                final Object result = unmarshaller.readObject();
                // read the attachments
                final Map<String, Object> attachments = HttpEJBReceiver.this.readAttachments(unmarshaller);

                // finish unmarshalling
                unmarshaller.finish();
                // see if there's a weak affinity passed as an attachment. If yes, then attach it to the client invocation
                // context
                if (this.clientInvocationContext != null && attachments != null && attachments.containsKey(Affinity.WEAK_AFFINITY_CONTEXT_KEY)) {
                    final Affinity weakAffinity = (Affinity) attachments.get(Affinity.WEAK_AFFINITY_CONTEXT_KEY);
                    this.clientInvocationContext.putAttachment(AttachmentKeys.WEAK_AFFINITY, weakAffinity);
                }
                // return the result
                return result;
            } finally {
                this.input.close();
            }
        }

        @Override
        public void discardResult() {
        }
    }

    protected Map<String, Object> readAttachments(final ObjectInput input) throws IOException, ClassNotFoundException {
        final int numAttachments = input.readByte();
        if (numAttachments == 0) {
            return null;
        }
        final Map<String, Object> attachments = new HashMap<String, Object>(numAttachments);
        for (int i = 0; i < numAttachments; i++) {
            // read the key
            final String key = (String) input.readObject();
            // read the attachment value
            final Object val = input.readObject();
            attachments.put(key, val);
        }
        return attachments;
    }

    /**
     * Creates and returns a {@link org.jboss.marshalling.Unmarshaller} which is ready to be used for unmarshalling. The {@link org.jboss.marshalling.Unmarshaller#start(org.jboss.marshalling.ByteInput)}
     * will be invoked by this method, to use the passed {@link DataInput dataInput}, before returning the unmarshaller.
     *
     * @param marshallerFactory The marshaller factory
     * @param dataInput         The data input from which to unmarshall
     * @return
     * @throws IOException
     */
    protected Unmarshaller prepareForUnMarshalling(final MarshallerFactory marshallerFactory, final DataInputStream dataInput) throws IOException {
        final Unmarshaller unmarshaller = this.getUnMarshaller(marshallerFactory);
        final InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                try {

                    final int b = dataInput.readByte();
                    return b & 0xff;
                } catch (EOFException eof) {
                    return -1;
                }
            }

            @Override
            public int read(final byte[] b) throws IOException {
                return dataInput.read(b);
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                return dataInput.read(b, off, len);
            }
        };
        final ByteInput byteInput = Marshalling.createByteInput(is);
        // start the unmarshaller
        unmarshaller.start(byteInput);

        return unmarshaller;
    }

    /**
     * Creates and returns a {@link Unmarshaller}
     *
     * @param marshallerFactory The marshaller factory
     * @return
     * @throws IOException
     */
    private Unmarshaller getUnMarshaller(final MarshallerFactory marshallerFactory) throws IOException {
        final MarshallingConfiguration marshallingConfiguration = new MarshallingConfiguration();
        marshallingConfiguration.setVersion(2);
        marshallingConfiguration.setClassTable(ProtocolV1ClassTable.INSTANCE);
        marshallingConfiguration.setObjectTable(ProtocolV1ObjectTable.INSTANCE);
        marshallingConfiguration.setClassResolver(TCCLClassResolver.INSTANCE);
        marshallingConfiguration.setSerializedCreator(new SunReflectiveCreator());
        return marshallerFactory.createUnmarshaller(marshallingConfiguration);
    }

    /**
     * A {@link org.jboss.marshalling.ClassResolver} which returns the context classloader associated
     * with the thread, when the {@link #getClassLoader()} is invoked
     */
    private static final class TCCLClassResolver extends AbstractClassResolver {
        static TCCLClassResolver INSTANCE = new TCCLClassResolver();

        private TCCLClassResolver() {
        }

        @Override
        protected ClassLoader getClassLoader() {
            return SecurityActions.getContextClassLoader();
        }
    }
    @Override
    protected <T> StatefulEJBLocator<T> openSession(EJBReceiverContext context, Class<T> viewType, String appName,
            String moduleName, String distinctName, String beanName) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean exists(String appName, String moduleName, String distinctName, String beanName) {
        // TODO Auto-generated method stub
        return true;
    }

}
