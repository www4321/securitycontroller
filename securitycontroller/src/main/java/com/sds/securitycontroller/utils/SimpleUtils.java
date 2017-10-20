/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;



public class SimpleUtils {
	
	 public static int getUnsignedValue (Object data){
		 if(data instanceof Byte)
			 return (byte)data&0x0FF;
		 else if(data instanceof Short)
	         return (short)data&0x0FFFF;
		 else
			 return 1000;
      }
	 
     public static <T extends Serializable> byte[] serialize(  
            T obj, boolean compressed) throws IOException {  
        ByteArrayOutputStream buf = new ByteArrayOutputStream();  
        ObjectOutputStream oos = null;  
        try {  
            if (compressed)  
                oos = new ObjectOutputStream(new GZIPOutputStream(buf));  
            else  
                oos = new ObjectOutputStream(buf);  
            oos.writeObject(obj);  
            oos.flush();  
        } finally {  
            if (oos != null)  
                oos.close();  
        }  
        return buf.toByteArray();  
     }  
     
     @SuppressWarnings("unchecked")
     public static <T extends Serializable> T deserialize(byte[] data,  
	        boolean compressed) throws IOException, ClassNotFoundException {  
	    ByteArrayInputStream in = new ByteArrayInputStream(data);  
	    ObjectInputStream ois = null;  
	    try {  
	        if (compressed)  
	            ois = new ObjectInputStream(new GZIPInputStream(in));  
	        else  
	            ois = new ObjectInputStream(in);  
	        return (T) ois.readObject();  
	    } finally {  
	    	ois.close();  
	    }  
     } 
	
	
     public static <T extends Serializable> String serializeToString(
	        T obj, boolean compressed) throws IOException {  
		byte[] rawObj = serialize(obj, compressed);
		return new String(Base64.encodeBase64(rawObj));
     }
		
	
     public static <T extends Serializable> T deserializeFromString(String encodedData,  
	        boolean compressed) throws IOException, ClassNotFoundException {  
		byte[] data = Base64.decodeBase64(encodedData.getBytes());
		return deserialize(data, compressed);
     }
	 

}
