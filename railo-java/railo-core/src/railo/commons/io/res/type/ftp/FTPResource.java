package railo.commons.io.res.type.ftp;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import railo.commons.io.IOUtil;
import railo.commons.io.ModeUtil;
import railo.commons.io.res.Resource;
import railo.commons.io.res.ResourceProvider;
import railo.commons.io.res.util.ResourceSupport;
import railo.commons.io.res.util.ResourceUtil;
import railo.commons.lang.StringUtil;
import railo.runtime.PageContext;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.op.Caster;

public final class FTPResource extends ResourceSupport {


	private final FTPResourceProvider provider;
	private final String path;
	private final String name;
	private final FTPConnectionData data;
	

	
	/**
	 * Constructor of the class
	 * @param factory
	 * @param data 
	 * @param path
	 */
	 FTPResource(FTPResourceProvider provider, FTPConnectionData data, String path) {
		this.provider=provider;
		this.data=data;
		
		String[] pathName=ResourceUtil.translatePathName(path);
		this.path=pathName[0];
		this.name=pathName[1];
	}

	 /**
	 * Constructor of the class
	 * @param factory
	 * @param data 
	 * @param path
	 */
	 private FTPResource(FTPResourceProvider provider, FTPConnectionData data, String path,String name) {
		this.provider=provider;
		this.data=data;
		this.path=path;
		this.name=name;
	}

	/**
	 * @see railo.commons.io.res.Resource#isReadable()
	 */
	public boolean isReadable() {
		Boolean rtn = hasPermission(FTPFile.READ_PERMISSION);
		if(rtn==null) return false;
		return rtn.booleanValue();
	}

	public boolean isWriteable() {
		Boolean rtn = hasPermission(FTPFile.WRITE_PERMISSION);
		if(rtn==null) return false;
		return rtn.booleanValue();
	}

	private Boolean hasPermission(int permission) {
		FTPResourceClient client=null;
		try {
			provider.read(this);
			client=provider.getClient(data);
			FTPFile file=client.getFTPFile(this);
			if(file==null) return null;
			return	Caster.toBoolean(file.hasPermission(FTPFile.USER_ACCESS,permission) || 
					file.hasPermission(FTPFile.GROUP_ACCESS,permission) || 
					file.hasPermission(FTPFile.WORLD_ACCESS,permission));
		} 
		catch (IOException e) {
			return Boolean.FALSE;
		}
		finally {
			provider.returnClient(client);
		}
	}
	
	/**
	 * @see railo.commons.io.res.Resource#remove(boolean)
	 */
	public void remove(boolean alsoRemoveChildren) throws IOException {
		if(isRoot()) throw new FTPResoucreException("can't delete root of ftp server");
		
		if(alsoRemoveChildren)ResourceUtil.removeChildren(this);
		FTPResourceClient client=null;
		try {
			provider.lock(this);
			client = provider.getClient(data);
			boolean result = client.deleteFile(getInnerPath());
			if(!result) throw new IOException("can't delete file ["+getPath()+"]");
		}
		finally {
			provider.returnClient(client);
			provider.unlock(this);
		}
	}

	/**
	 * @see railo.commons.io.res.util.ResourceSupport#delete()
	 */
	public boolean delete() {
		if(isRoot()) return false;
		FTPResourceClient client = null;
		try {
			provider.lock(this);
			client = provider.getClient(data);
			return client.deleteFile(getInnerPath());
		} 
		catch (IOException e) {
			return false;
		}
		finally {
			provider.returnClient(client);
			provider.unlock(this);
		}
	}

	/**
	 * @see res.Resource#exists()
	 */
	public boolean exists() {
		try {
			provider.read(this);
		} catch (IOException e) {
			return true;
		}
		FTPResourceClient client = null;
		InputStream is=null;
		try {
//			 getClient muss zuerst sein so wird verbindung geprﾟft
			client = provider.getClient(data);
			if(isRoot()) return true;

			FTPFile file = client.getFTPFile(this);
			if(file!=null) {
				return !file.isUnknown();
			}
			
			//String pathname = getInnerPath();
			String p = getInnerPath();
			if(!StringUtil.endsWith(p, '/'))p+="/";
			if(client.listNames(p)!=null) return true;
			return false;
		} 
		catch (IOException e) {
			return false;
		}
		finally {
			IOUtil.closeEL(is);
			provider.returnClient(client);
		}
	}

	/**
	 * @see res.Resource#getFullName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see res.Resource#getParent()
	 */
	public String getParent() {
		if(isRoot()) return null;
		return provider.getScheme().concat("://").concat(data.key()).concat(path.substring(0,path.length()-1));
	} 
	
	public String getInnerParent() {
		return path;
	}

	/**
	 * @see res.Resource#getParentResource()
	 */
	public Resource getParentResource() {
		if(isRoot()) return null;
		return new FTPResource(provider,data,path);
	}

	/**
	 * @see railo.commons.io.res.Resource#getRealResource(java.lang.String)
	 */
	public Resource getRealResource(String realpath) {
		realpath=ResourceUtil.merge(getInnerPath(), realpath);
		if(realpath.startsWith("../"))return null;
		return new FTPResource(provider,data,realpath);
	}

	/**
	 * @see res.Resource#getPath()
	 */
	public String getPath() {	
		return provider.getScheme().concat("://").concat(data.key()).concat(path).concat(name);
	}
	/**
	 * @return returns path starting from ftp root
	 */
	String getInnerPath() {	
		return path.concat(name);
	}

	/**
	 * @see railo.commons.io.res.Resource#isAbsolute()
	 */
	public boolean isAbsolute() {
		// TODO impl isAbolute
		return true;
	}

	/**
	 * @see railo.commons.io.res.Resource#isDirectory()
	 */
	public boolean isDirectory() {
		try {
			provider.read(this);
		} 
		catch (IOException e1) {
			return false;
		}
		FTPResourceClient client=null;
		try {
			// getClient muss zuerst sein so wird verbindung geprﾟft
			client = provider.getClient(data);
			if(isRoot())return true;

			FTPFile file = client.getFTPFile(this);
			if(file!=null) {
				return file.isDirectory();
			}
			//if(file==null) return false;
			//return file.isDirectory();
			
			String p = getInnerPath();
			if(!StringUtil.endsWith(p, '/'))p+="/";
			return client.listNames(p)!=null;
			
		} 
		catch (IOException e) {
			return false;
		}
		finally {
			provider.returnClient(client);
		}
	}

	/**
	 * @see res.Resource#isFile()
	 */
	public boolean isFile() {
		if(isRoot()) return false;
		try {
			provider.read(this);
		} 
		catch (IOException e1) {
			return false;
		}
		FTPResourceClient client=null;
		InputStream is=null;
		try {
			client = provider.getClient(data);
			FTPFile file = client.getFTPFile(this);
			if(file!=null) {
				return file.isFile();
			}
			return false;
			//String pathname = getInnerPath();
			//return (is=client.retrieveFileStream(pathname))!=null;
		} 
			
			

		catch (IOException e) {
			return false;
		}
		finally {
			IOUtil.closeEL(is);
			provider.returnClient(client);
		}
	}

	/**
	 * @see railo.commons.io.res.Resource#lastModified()
	 */
	public long lastModified() {
		//if(isRoot()) return 0;
		
		FTPResourceClient client=null;
		try {
			provider.read(this);
			client=provider.getClient(data);
			FTPFile file = client.getFTPFile(this);
			if(file==null) return 0;
			return file.getTimestamp().getTimeInMillis();
		}
		catch (IOException e) {
			return 0;
		}
		finally {
			provider.returnClient(client);
		}
	}

	/**
	 * @see railo.commons.io.res.Resource#length()
	 */
	public long length() {
		if(isRoot()) return 0;
		FTPResourceClient client=null;
		try {
			provider.read(this);
			client = provider.getClient(data);
			FTPFile file = client.getFTPFile(this);
			if(file==null) return 0;
			return file.getSize();
		} 
		catch (IOException e) {
			return 0;
		}
		finally {
			provider.returnClient(client);
		}
	}

	/**
	 * @see res.Resource#listResources()
	 */
	public Resource[] listResources() {
		if(isFile()) return null;//new Resource[0];
		
		FTPResourceClient client=null;
		try {
			client = provider.getClient(data);
			FTPFile[] files=null;
			String p = getInnerPath();
			if(!StringUtil.endsWith(p, '/'))p+="/";
			files=client.listFiles(p);
			if(files==null) return new Resource[0];
			
			List list=new ArrayList();
			String parent=path.concat(name).concat("/");
			String name;
			FTPResource res;
		    for(int i=0;i<files.length;i++) {
		    	name=files[i].getName();
		    	if(!".".equals(name) && !"..".equals(name)) {
			    	res=new FTPResource(provider,data,parent,name);
		    		client.registerFTPFile(res, files[i]);
		    		list.add(res);
		    	}
		    }
			return (Resource[]) list.toArray(new FTPResource[list.size()]);
		}
		catch(IOException ioe) {
			return null;
		}
		finally {
			provider.returnClient(client);
		}
	}

	/**
	 * @see railo.commons.io.res.Resource#setLastModified(long)
	 */
	public boolean setLastModified(long time) {
		//if(isRoot()) return false;
		
		FTPResourceClient client=null;
		try {
			provider.lock(this);
			client=provider.getClient(data);
			
			PageContext pc = ThreadLocalPageContext.get();
			Calendar c;
			if(pc==null) c=Calendar.getInstance();
			else c=Calendar.getInstance(pc.getTimeZone());
			c.setTimeInMillis(time);
			FTPFile file = client.getFTPFile(this);
			if(file==null) return false;
			file.setTimestamp(c);
			client.unregisterFTPFile(this);
			return true;
		}
		catch (IOException e) {}
		finally {
			provider.returnClient(client);
			provider.unlock(this);
		}

		return false;
	}

	public boolean setReadOnly() {
		try {
			setMode(ModeUtil.setWritable(getMode(), false));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * @see res.Resource#createFile(boolean)
	 */
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		//client.unregisterFTPFile(this);
		IOUtil.copy(new ByteArrayInputStream(new byte[0]), getOutputStream(), true, true);
	}
	
	/**
	 * @see res.Resource#moveTo(res.Resource)
	 */
	public void moveTo(Resource dest) throws IOException {
		FTPResourceClient client=null;
		ResourceUtil.checkMoveToOK(this, dest);
		try {
			provider.lock(this);
			client = provider.getClient(data);
			
			client.unregisterFTPFile(this);
			
			if(dest instanceof FTPResource) moveTo(client,(FTPResource)dest);
			else super.moveTo(dest);
			
		}
		finally {
			provider.returnClient(client);
			provider.unlock(this);
		}
	}

	private void moveTo(FTPResourceClient client, FTPResource dest) throws IOException {
		if(!dest.data.equals(data)) {
			super.moveTo(dest);
			return;
		}
		if(dest.exists())dest.delete();
		
		client.unregisterFTPFile(dest);
		boolean ok = client.rename(getInnerPath(), dest.getInnerPath());
		if(!ok) throw new IOException("can't create file "+this);
		
	}

	/**
	 * @see res.Resource#createDirectory(boolean)
	 */
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		
		FTPResourceClient client=null;
		try {
			provider.lock(this);
			client = provider.getClient(data);
			client.unregisterFTPFile(this);
			boolean ok = client.makeDirectory(getInnerPath());
			if(!ok) throw new IOException("can't create file "+this);
			
		}
		finally {
			provider.returnClient(client);
			provider.unlock(this);
		}
	}

	/**
	 * @throws IOException 
	 * @see res.Resource#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		ResourceUtil.checkGetInputStreamOK(this);
		provider.lock(this);
		FTPResourceClient client=provider.getClient(data);
		client.setFileType(FTP.BINARY_FILE_TYPE);
		try {
			return IOUtil.toBufferedInputStream(new FTPResourceInputStream(client,this,client.retrieveFileStream(getInnerPath())));
		} 
		catch (IOException e) {
			provider.returnClient(client);
			provider.unlock(this);
			throw e;
		}
	}

	/**
	 * @see railo.commons.io.res.Resource#getOutputStream(boolean)
	 */
	public OutputStream getOutputStream(boolean append) throws IOException {
		ResourceUtil.checkGetOutputStreamOK(this);
		FTPResourceClient client=null;
		try {
			provider.lock(this);
			client=provider.getClient(data);
			client.unregisterFTPFile(this);
			client.setFileType(FTP.BINARY_FILE_TYPE);
			OutputStream os = append?client.appendFileStream(getInnerPath()):client.storeFileStream(getInnerPath());
			if(os==null)throw new IOException("can not open stream to file ["+this+"]");
			
			return IOUtil.toBufferedOutputStream(new FTPResourceOutputStream(client,this,os));
		}
		catch (IOException e) {
			provider.returnClient(client);
			provider.unlock(this);
			throw e;
		}
	}


	/**
	 * @see res.util.ResourceSupport#list()
	 */
	public String[] list() {
		if(isFile()) return new String[0];
		
		FTPResourceClient client=null;
		try {
			client = provider.getClient(data);
			String[] files=null;
			
			String p = getInnerPath();
			if(!StringUtil.endsWith(p, '/'))p+="/";
			files=client.listNames(p);
			if(files==null) return new String[0];
			for(int i=0;i<files.length;i++) {
				files[i]=cutName(files[i]);
		    }
			return files;
		}
		catch(IOException ioe) {
			return null;
		}
		finally {
			provider.returnClient(client);
		}
	}

	private String cutName(String path) {
		int index=path.lastIndexOf('/');
		if(index==-1) return path;
		return path.substring(index+1);
	}

	/**
	 * @see res.Resource#getResourceProvider()
	 */
	public ResourceProvider getResourceProvider() {
		return provider;
	}
	
	public FTPResourceProvider getFTPResourceProvider() {
		return provider;
	}

	
	boolean isRoot() {
		return StringUtil.isEmpty(name);
	}

	public int getMode() {
		//if(isRoot()) return 0;
		
		FTPResourceClient client=null;
		try {
			provider.read(this);
			client=provider.getClient(data);
			
			FTPFile file = client.getFTPFile(this);
			int mode=0;
			if(file==null)return 0;
			
			// World
			if(file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION))	mode+=01;
			if(file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION))		mode+=02;
			if(file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION))		mode+=04;

			// Group
			if(file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION))	mode+=010;
			if(file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION))		mode+=020;
			if(file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION))		mode+=040;

			// Owner
			if(file.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION))		mode+=0100;
			if(file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION))		mode+=0200;
			if(file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION))		mode+=0400;
			
			return mode;
			
		}
		catch (IOException e) {}
		finally {
			provider.returnClient(client);
		}

		return 0;
	}

	public void setMode(int mode) throws IOException {
		//if(isRoot()) throw new IOException("can't change mode of root");
	
		FTPResourceClient client=null;
		try {
			provider.lock(this);
			client=provider.getClient(data);
			
			FTPFile file = client.getFTPFile(this);
			if(file!=null) {
				// World
				file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION,(mode&01)>0);
				file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION,(mode&02)>0);
				file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION,(mode&04)>0);
	
				// Group
				file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION,(mode&010)>0);
				file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION,(mode&020)>0);
				file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION,(mode&040)>0);
	
				// Owner
				file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION,(mode&0100)>0);
				file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION,(mode&0200)>0);
				file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION,(mode&0400)>0);
	
				client.unregisterFTPFile(this);
			}
		}
		catch (IOException e) {}
		finally {
			provider.returnClient(client);
			provider.unlock(this);
		}

	}

	public boolean setReadable(boolean value) {
		try {
			setMode(ModeUtil.setReadable(getMode(), value));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean setWritable(boolean value) {
		try {
			setMode(ModeUtil.setWritable(getMode(), value));
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}