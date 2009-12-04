package railo.runtime.tag;

import java.util.Iterator;

import railo.commons.io.SystemUtil;
import railo.commons.lang.StringUtil;
import railo.runtime.Page;
import railo.runtime.PageContext;
import railo.runtime.PageContextImpl;
import railo.runtime.config.ConfigImpl;
import railo.runtime.exp.ApplicationException;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.PageException;
import railo.runtime.exp.SecurityException;
import railo.runtime.ext.tag.BodyTagImpl;
import railo.runtime.ext.tag.DynamicAttributes;
import railo.runtime.op.Caster;
import railo.runtime.spooler.ExecutionPlan;
import railo.runtime.spooler.ExecutionPlanImpl;
import railo.runtime.thread.ChildSpoolerTask;
import railo.runtime.thread.ChildThread;
import railo.runtime.thread.ChildThreadImpl;
import railo.runtime.thread.ThreadUtil;
import railo.runtime.thread.ThreadsImpl;
import railo.runtime.type.Array;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.List;
import railo.runtime.type.Struct;
import railo.runtime.type.StructImpl;
import railo.runtime.type.Collection.Key;
import railo.runtime.type.scope.Threads;

// MUST change behavor of mltiple headers now is a array, it das so?

/**
* Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard 
*   GET operations and create a query object from a text file. POST operations lets you upload MIME file 
*   types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a specified server.
*
*
*
* 
**/
public final class ThreadTag extends BodyTagImpl implements DynamicAttributes {
	
	private static final int ACTION_JOIN = 0;
	private static final int ACTION_RUN = 1;
	private static final int ACTION_SLEEP = 2;
	private static final int ACTION_TERMINATE = 3;
	
	private static final int TYPE_DEAMON = 0;
	private static final int TYPE_TASK = 1;
	private static final ExecutionPlan[] EXECUTION_PLAN = new ExecutionPlan[0];
	
	private static final Key DURATION = KeyImpl.getInstance("duration");
	private static final Key NAME = KeyImpl.getInstance("name");
	
	
	
	
	private int action=ACTION_RUN;
	private long duration=-1;
	private String name;
	private String lcName;
	private int priority=Thread.NORM_PRIORITY;
	private long timeout=0;
	private PageContext pc;
	private int type=TYPE_DEAMON;
	private ExecutionPlan[] plans=EXECUTION_PLAN;
	private Struct attrs;
	

	/**
	* @see javax.servlet.jsp.tagext.Tag#release()
	*/
	public void release()	{
		if(ACTION_RUN==action) return;
		_release();
		
	}
	private void _release()	{
		super.release();
		action=ACTION_RUN;
		duration=-1;
		name=null;
		lcName=null;
		priority=Thread.NORM_PRIORITY;
		type=TYPE_DEAMON;
		plans=EXECUTION_PLAN;
		timeout=0;
		attrs=null;
	}
	
	/**
	 * @param action the action to set
	 */
	public void setAction(String strAction) throws ApplicationException {
		String lcAction = strAction.trim().toLowerCase();
		
		if("join".equals(lcAction)) 			this.action=ACTION_JOIN;
		else if("run".equals(lcAction)) 		this.action=ACTION_RUN;
		else if("sleep".equals(lcAction)) 	this.action=ACTION_SLEEP;
		else if("terminate".equals(lcAction)) this.action=ACTION_TERMINATE;
		else 
			throw new ApplicationException("invalid value ["+strAction+"] for attribute action","values for attribute action are:join,run,sleep,terminate");
	}


	/**
	 * @param duration the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = (long) duration;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name.trim();
		this.lcName=this.name.toLowerCase();
	}


	/**
	 * @param strPriority the priority to set
	 */
	public void setPriority(String strPriority) throws ApplicationException {
		int p = ThreadUtil.toIntPriority(strPriority);
		if(p==-1) {
			throw new ApplicationException("invalid value ["+strPriority+"] for attribute priority","values for attribute priority are:low,high,normal");
		}
		priority=p;
	}
	

	/**
	 * @param strType the type to set
	 * @throws ApplicationException 
	 * @throws SecurityException 
	 */
	public void setType(String strType) throws ApplicationException, SecurityException {
		strType=strType.trim().toLowerCase();

		if("task".equals(strType))	{
			// SNSN
			/*SerialNumber sn = pageContext.getConfig().getSerialNumber();
		    if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		         throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+" version of railo");
		    */
			
			
			//throw new ApplicationException("invalid value ["+strType+"] for attribute type","task is not supported at the moment");
			type=TYPE_TASK;
		}
		else if("deamon".equals(strType))	type=TYPE_DEAMON;
		else throw new ApplicationException("invalid value ["+strType+"] for attribute type","values for attribute type are:task,deamon (default)");
		
	}
	
	public void setRetryintervall(Object obj) throws PageException {
		setRetryinterval(obj);
	}
	
	public void setRetryinterval(Object obj) throws PageException {
		if(StringUtil.isEmpty(obj))return;
		Array arr = Caster.toArray(obj,null);
		if(arr==null){
			plans=new ExecutionPlan[]{toExecutionPlan(obj,1)};
		}
		else {
			Iterator it = arr.iterator();
			plans=new ExecutionPlan[arr.size()];
			int index=0;
			while(it.hasNext()) {
				plans[index++]=toExecutionPlan(it.next(),index==1?1:0);
			}
		}
		
	}
	
	
	


	private ExecutionPlan toExecutionPlan(Object obj,int plus) throws PageException {

		if(obj instanceof Struct){
			Struct sct=(Struct)obj;
			// GERT
			
			// tries
			Object oTries=sct.get("tries",null);
			if(oTries==null)throw new ExpressionException("missing key tries inside struct");
			int tries=Caster.toIntValue(oTries);
			if(tries<0)throw new ExpressionException("tries must contain a none negative value");
			
			// interval
			Object oInterval=sct.get("interval",null);
			if(oInterval==null)oInterval=sct.get("intervall",null);
			
			if(oInterval==null)throw new ExpressionException("missing key interval inside struct");
			int interval=toSeconds(oInterval);
			if(interval<0)throw new ExpressionException("interval should contain a positive value or 0");
			
			
			return new ExecutionPlanImpl(tries+plus,interval);
		}
		return new ExecutionPlanImpl(1+plus,toSeconds(obj));
	}
	
	private int toSeconds(Object obj) throws PageException {
		return (int)Caster.toTimespan(obj).getSeconds();
	}
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(double timeout) {
		this.timeout = (long)timeout;
	}

	public void setDynamicAttribute(String uri, String name, Object value) {
		if(attrs==null)attrs=new StructImpl();
		Key key = KeyImpl.init(name=StringUtil.trim(name,""));
		
		/*if(key.equals(NAME))	setName(name);
		else if(key.equals(DURATION)){
			try {
				setDuration(Caster.toDoubleValue(name));
			} catch (PageException pe) {
				throw new PageRuntimeException(pe);
			}
		}
		else*/ 
			attrs.setEL(key,value);
	}

	/**
	 * @throws PageException 
	 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
	 */
	public int doStartTag() throws PageException	{
		switch(action) {
			case ACTION_JOIN:	
				required("thread", "join", "name", name);	
				doJoin();
			break;
			case ACTION_SLEEP:	
				required("thread", "sleep", "duration", duration,-1);	
				doSleep();
			break;
			case ACTION_TERMINATE:	
				required("thread", "terminate", "name", name);
				doTerminate();
			break;
			case ACTION_RUN:		
				required("thread", "run", "name", name);
				return EVAL_BODY_INCLUDE;
			
		}
		return SKIP_BODY;
	}

	/**
	 * @throws PageException
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws PageException {
		this.pc=pageContext;
		//if(ACTION_RUN==action) doRun();
		return EVAL_PAGE;
	}
	
	public void register(Page currentPage, int threadIndex) throws PageException	{
		if(ACTION_RUN!=action) return;
		
		if(((PageContextImpl)pc).getParentPageContext()!=null)
			throw new ApplicationException("could not create a thread within a child thread");
		
		try {
			Threads ts = pc.getThreadScope(lcName);
			
			if(type==TYPE_DEAMON){
				if(ts!=null)
					throw new ApplicationException("could not create a thread with the name ["+name+"]. name must be unique within a request");
				ChildThreadImpl ct = new ChildThreadImpl((PageContextImpl) pc,currentPage,name,threadIndex,attrs,false);
				pc.setThreadScope(name,new ThreadsImpl(ct));
				ct.setPriority(priority);
				ct.setDaemon(false);
				ct.start();
			}
			else {
				ChildThreadImpl ct = new ChildThreadImpl((PageContextImpl) pc,currentPage,name,threadIndex,attrs,true);
				ct.setPriority(priority);
				((ConfigImpl)pc.getConfig()).getSpoolerEngine().add(new ChildSpoolerTask(ct,plans));
			}
			
		} 
		catch (Throwable t) {
			throw Caster.toPageException(t);
		}
		finally {
			_release();
		}
	}
	
	private void doSleep() throws ExpressionException {
		if(duration>=0) {
			SystemUtil.sleep(duration);
		}
		else throw new ExpressionException("The attribute duration must be greater or equal than 0, now ["+duration+"]");
		
	}

    private void doJoin() throws ApplicationException {
    	String[] names=List.listToStringArray(name, ',');
    	String lcName;
    	
    	ChildThread ct;
    	Threads ts;
    	for(int i=0;i<names.length;i++) {
    		if(StringUtil.isEmpty(names[i],true))continue;
    		ts = pc.getThreadScope(names[i]);
    		if(ts==null)
    			throw new ApplicationException("there is no thread running with the name ["+name+"]");
    		ct=ts.getChildThread();
    		
    		if(ct.isAlive()) {
    			try {
					if(timeout>0)ct.join(timeout);
					else ct.join();
				} 
    			catch (InterruptedException e) {}
    		}
    	}
    	
    }

	private void doTerminate() throws ApplicationException {
		
		Threads ts = pc.getThreadScope(lcName);
		
		if(ts==null)
			throw new ApplicationException("there is no thread running with the name ["+name+"]");
		ChildThread ct = ts.getChildThread();
		
		if(ct.isAlive()){
			ct.terminated();
			ct.stop();
		}
		
	}

	/**
	* @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
	*/
	public void doInitBody()	{
		
	}

	/**
	* @see javax.servlet.jsp.tagext.BodyTag#doAfterBody()
	*/
	public int doAfterBody()	{
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
	    
	}
}