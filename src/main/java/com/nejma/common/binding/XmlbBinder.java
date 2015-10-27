package com.nejma.common.binding;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author aminelouati
 */
public class XmlbBinder {

    private static Map<Class, JAXBContext> jaxbContextMap = new ConcurrentHashMap<Class, JAXBContext>();
    private static JAXBException JAXB_EXCEPTION = new JAXBException("JAXB_EXCEPTION");

    private static JAXBContext getJaxbContext(Class clazz) throws JAXBException {
        JAXBContext jaxbContext = jaxbContextMap.get(clazz);

        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(clazz);
            jaxbContextMap.put(clazz, jaxbContext);
        }
        return jaxbContext;
    }

    public static <T> String marshal(T instance) throws JAXBException {
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = getJaxbContext(instance.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(instance, result);
        return result.toString();
    }

    public static <T> void marshal(T instance, File file) throws JAXBException {
        JAXBContext jaxbContext = getJaxbContext(instance.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(instance, file);
    }

    public static <T> void marshal(T instance, OutputStream output) throws JAXBException {
        JAXBContext jaxbContext = getJaxbContext(instance.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(instance, output);
    }

    public static <T> T unmarshal(String xml, Class<T> clazz) throws JAXBException {
        if (xml == null) {
            throw JAXB_EXCEPTION;
        }
        JAXBContext jaxbContext = getJaxbContext(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try {
            InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(xml.getBytes()), "utf-8");
            return (T) unmarshaller.unmarshal(isr);
        } catch (UnsupportedEncodingException ex) {
            throw JAXB_EXCEPTION;
        }
    }

    public static <T> T unmarshal(InputStream stream, Class<T> clazz) throws JAXBException {
        if (stream == null) {
            throw JAXB_EXCEPTION;
        }
        JAXBContext jaxbContext = getJaxbContext(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try {
            InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
            return (T) unmarshaller.unmarshal(isr);
        } catch (UnsupportedEncodingException ex) {
            throw JAXB_EXCEPTION;
        }
    }

    public static <T> T unmarshal(URL url, Class<T> clazz) throws JAXBException {
        if (url == null) {
            throw JAXB_EXCEPTION;
        }
        JAXBContext jaxbContext = getJaxbContext(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(url);
    }

    public static <T> T unmarshal(File file, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = getJaxbContext(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(file);
    }
}
