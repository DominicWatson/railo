/**
 * Implements the Cold Fusion Function createobject
 * FUTURE neue attr unterst�tzen
 */
package railo.runtime.functions.other;

import railo.commons.io.res.Resource;
import railo.commons.io.res.util.ResourceClassLoader;
import railo.commons.io.res.util.ResourceUtil;
import railo.commons.lang.ClassUtil;
import railo.commons.lang.StringUtil;
import railo.runtime.Component;
import railo.runtime.PageContext;
import railo.runtime.com.COMObject;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.FunctionNotSupported;
import railo.runtime.exp.PageException;
import railo.runtime.exp.SecurityException;
import railo.runtime.ext.function.Function;
import railo.runtime.java.JavaObject;
import railo.runtime.net.rpc.client.RPCClient;
import railo.runtime.op.Caster;
import railo.runtime.security.SecurityManager;
import railo.runtime.type.Array;
import railo.runtime.type.List;

public final class CreateObject implements Function {
	public static Object call(PageContext pc , String type, String className) throws PageException {
		return call(pc,type,className,null,null);
	}
	public static Object call(PageContext pc , String type, String className, String context) throws PageException {
		return call(pc,type,className,context,null);
	}
	public static Object call(PageContext pc , String type, String className, String context, String serverName) throws PageException {
		type=StringUtil.toLowerCase(type);
		
		
		// JAVA
			if(type.equals("java")) {
			    checkAccess(pc,type);
				return doJava(pc, className,context,serverName);
			}
		// COM
			if(type.equals("com")) {
				return doCOM(pc,className);
			}
        // Component
            if(type.equals("component")) {
                return doComponent(pc,className);
            }
        // Webservice
            if(type.equals("webservice") || type.equals("wsdl")) {
                return doWebService(pc,className);
            }
        // .net
            if(type.equals(".net") || type.equals("dotnet")) {
                return doDotNet(pc,className);
            }
			throw new ExpressionException("invalid argument for function createObject, first argument (type), " +
					"must be (com, java, webservice or component) other types are not supported");
		
	} 

    private static Object doDotNet(PageContext pc, String className) throws FunctionNotSupported {
    	throw new FunctionNotSupported("CreateObject","type .net");
	}
	private static void checkAccess(PageContext pc, String type) throws SecurityException {
        if(pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT)==SecurityManager.VALUE_NO) 
			throw new SecurityException("can't access function [createObject] with type ["+type+"]","access is prohibited by security manager");
		
    }
    
    public static Object doJava(PageContext pc,String className, String pathes, String delimeter) throws PageException {
        if(pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS)==SecurityManager.VALUE_YES) {
        	ClassLoader parent = pc.getConfig().getClassLoader();
        	
//        	 load classPath
        	if(!StringUtil.isEmpty(pathes, true)) {
        		if(StringUtil.isEmpty(delimeter))delimeter=",";
        		Array arrPathes = List.listToArrayRemoveEmpty(pathes.trim(),delimeter);
        		Resource[] reses=new Resource[arrPathes.size()];
        		for(int i=0;i<reses.length;i++) {
        			reses[i]=ResourceUtil.toResourceExisting(pc,Caster.toString(arrPathes.getE(i+1)));
        		}
        		try {
        			//ClassLoader cl = new ResourceClassLoader(reses,pc.getClass().getClassLoader());
        			ClassLoader cl = new ResourceClassLoader(reses,parent);
        			Class clazz = ClassUtil.loadClass(cl,className);
					return new JavaObject((pc).getVariableUtil(),clazz);
				} 
        		catch (Exception e) {
					throw Caster.toPageException(e);
				}
        	}
        	try	{
        		Class clazz = ClassUtil.loadClass(parent,className);
        		return new JavaObject((pc).getVariableUtil(),clazz);
	        } 
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
        }
        throw new SecurityException("can't create Java Object ["+className+"], direct java access is deinied by security manager");
	} 
    
    public static Object doCOM(PageContext pc,String className) {
		return new COMObject(className);
	} 
    
    public static Component doComponent(PageContext pc,String className) throws PageException {
    	return pc.loadComponent(className);
    } 
    
    public static Object doWebService(PageContext pc,String wsdlUrl) throws PageException {
    	// TODO CF8 impl. alle neuen attribute f�r wsdl
    	return new RPCClient(wsdlUrl);
    } 
}