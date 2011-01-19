<!--- 
Defaults --->
<cfset error.message="">
<cfset error.detail="">
<cfparam name="form.mainAction" default="none">

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
			<cfset data.label=toArrayFromForm("label")>
			<cfset data.hash=toArrayFromForm("hash")>
            
			<cfloop index="idx" from="1" to="#arrayLen(data.label)#">
				<cfif len(trim(data.label[idx]))>
                	<cfadmin 
                    action="updateLabel"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"
                    
                    label="#data.label[idx]#"
                    hash="#data.hash[idx]#">
                 </cfif>
            </cfloop>
		</cfcase>
	</cfswitch>
	<cfcatch>
	
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction NEQ "none">
	<cflocation url="#request.self#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>


<cfoutput>
<div style="width:740px">
#stText.Overview.introdesc[request.adminType]#
</div>
<br />
  

<table class="tbl" width="740">
<tr>
	<td colspan="2"><h2>#stText.Overview.Info#</h2></td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.Version#</td>
	<td class="tblContent">Railo #server.railo.version# #server.railo.state#</td>
</tr>
<cfif StructKeyExists(server.railo,'versionName')>
<tr>
	<td class="tblHead" width="150">#stText.Overview.VersionName#</td>
	<td class="tblContent"><a href="#server.railo.versionNameExplanation#" target="_blank">#server.railo.versionName#</a></td>
</tr>
</cfif>
<tr>
	<td class="tblHead" width="150">#stText.Overview.ReleaseDate#</td>
	<td class="tblContent">#lsDateFormat(server.railo['release-date'])#</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.CFCompatibility#</td>
	<td class="tblContent">#replace(server.ColdFusion.ProductVersion,',','.','all')#</td>
</tr>

<cfif request.adminType EQ "web">
<cfadmin 
	action="getTLDs"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="tlds">
<cfadmin 
	action="getFLDs"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="flds">

<cfif isQuery(tlds)>
	<cfset tlds=listToArray(valueList(tlds.displayname))>
</cfif>
<cfif isQuery(flds)>
	<cfset flds=listToArray(valueList(flds.displayname))>
</cfif>


<tr>
	<td class="tblHead" width="150">#stText.Overview.OS#</td>
	<td class="tblContent">#server.OS.Name# (#server.OS.Version#)</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.remote_addr#</td>
	<td class="tblContent">#cgi.remote_addr#</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.server_name#</td>
	<td class="tblContent">#cgi.server_name#</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.overview.servletContainer#</td>
	<td class="tblContent">#server.servlet.name#</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.overview.railoID#</td>
	<td class="tblContent">#getRailoId().server.id#</td>
</tr>

<tr>
	<td class="tblHead" width="150">#stText.Overview.InstalledTLs#</td>
	<td class="tblContent">
		<cfloop index="idx" from="1" to="#arrayLen(tlds)#">
			- #tlds[idx]# <!--- ( #iif(tlds[idx].type EQ "cfml",de('railo'),de('jsp'))# ) ---><br>
		</cfloop>
	</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.InstalledFLs#</td>
	<td class="tblContent">
		<cfloop index="idx" from="1" to="#arrayLen(flds)#">
			- #flds[idx]#<br>
		</cfloop>
	</td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.DateTime#</td>
	<td class="tblContent">
		#lsdateFormat(now())#
		#lstimeFormat(now())#
	</td> 
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.ServerTime#</td>
	<td class="tblContent">
		
		#lsdateFormat(nowServer())#
		#lstimeFormat(nowServer())#
	</td> 
</tr>
<tr>
	<td class="tblHead" width="150">Java</td>
	<td class="tblContent">
		<!--- <cfset serverNow=createObject('java','java.util.Date')> --->
		#server.java.version# (#server.java.vendor#)
	</td> 
</tr>
<tr>
	<td class="tblHead" width="150">Memory</td>
	<td class="tblContent">
		#round((server.java.maxMemory)/1024/1024)#mb
	</td> 
</tr>
<tr>
	<td class="tblHead" width="150">Classpath</td>
	<td class="tblContent">
    	
	<div class="tblContent" style="font-family:Courier New;font-size : 7pt;overflow:auto;width:100%;height:100px;border-style:solid;border-width:1px;padding:0px">
    <cfset arr=getClasspath()>
    <cfloop from="1" to="#arrayLen(arr)#" index="line">
    <span style="background-color:###line mod 2?'d2e0ee':'ffffff'#;display:block;padding:1px 5px 1px 5px ;">#arr[line]#</span>
    </cfloop>
   </div>
	</td> 
</tr>
</cfif>
</table>
<br><br>


<cfif request.admintype EQ "server">
<cfadmin 
	action="getContexts"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="rst">

<table class="tbl" width="740">

<tr>
	<td colspan="4"><h2>#stText.Overview.contexts.title#</h2></td>
</tr>
<tr>
	<td class="tblHead" width="100">#stText.Overview.contexts.label#</td>
	<td class="tblHead" width="150">#stText.Overview.contexts.url#</td>
	<td class="tblHead" width="220">#stText.Overview.contexts.webroot#</td>
	<td class="tblHead" width="220">#stText.Overview.contexts.config_file#</td>
</tr>
<cfform action="#request.self#" method="post">
<cfloop query="rst">
<input type="hidden" name="hash_#rst.currentrow#" value="#rst.hash#"/>
<tr>
	<td class="tblContent" width="100"><input type="text" style="width:100px" name="label_#rst.currentrow#" value="#rst.label#"/></td>
	<td class="tblContent" width="150"><cfif len(rst.url)><a target="_blank" href="#rst.url#/railo-context/admin/web.cfm">#rst.url#</a></cfif></td>
	<td class="tblContent"><input type="text" style="width:220px" name="path_#rst.currentrow#" value="#rst.path#" readonly="readonly"/></td>
	<td class="tblContent"><input type="text" style="width:220px" name="cf_#rst.currentrow#" value="#rst.config_file#" readonly="readonly"/></td>
</tr>
</cfloop>

<tr>
	<td colspan="4">
		<input class="submit" type="submit" class="submit" name="mainAction" value="#stText.Buttons.Update#">
		<input class="submit" type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
	</td>
</tr>


</cfform>
</table><br /><br />
</cfif>
 


<table class="tbl">
<tr>
	<td colspan="2"><h2>#stText.Overview.Support#</h2></td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.Professional#</td>
	<td class="tblContent"><a href="http://www.getrailo.com/index.cfm/services/support/" target="_blank">Support by Railo Technologies</a></td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.Mailinglist_en#</td>
	<td class="tblContent"><a href="http://groups.google.com/group/railo" target="_blank">groups.google.com.railo</a></td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.Mailinglist_de#</td>
	<td class="tblContent"><a href="http://de.groups.yahoo.com/group/railo/" target="_blank">de.groups.yahoo.com.railo</a></td>
</tr>
<tr>
	<td class="tblHead" width="150">Linked in</td>
	<td class="tblContent"><a href="http://www.linkedin.com/e/gis/71368/0CF7D323BBC1" target="_blank">Linked in</a></td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.issueTracker#</td>
	<td class="tblContent"><a href="https://jira.jboss.org/jira/browse/RAILO" target="_blank">jira.jboss.org.railo</a></td>
</tr>
<tr>
	<td class="tblHead" width="150">#stText.Overview.blog#</td>
	<td class="tblContent"><a href="http://www.railo-technologies.com/blog/" target="_blank">railo-technologies.com.blog</a></td>
</tr>

</table>
<!---
<cfif request.admintype EQ "server">
	<h2>#stText.Overview.LanguageSupport#</h2>
	<cfinclude template="overview.uploadNewLangFile.cfm">
	<table class="tbl">
		<tr>
			<td class="tblHead" width="150">#stText.Overview.ShortLabel#</td>
			<td class="tblHead" width="400">#stText.Overview.LanguageName#</td>
		</tr>
		<cfset stLangs = readLanguages()>
		<cfset aLangs = structKeyArray(stLangs)>
		<cfset arraySort(aLangs, "text")>
		<cfloop array="#aLangs#" index="sKey">
			<tr>
				<td class="tblContent" width="150">#sKey#</td>
				<td class="tblContent" width="400">#stLangs[sKey]#</td>
			</tr>
		</cfloop>
		<tr>
			<td class="tblHead">#stText.Overview.AddNewLanguage#</td>
			<form action="#cgi.script_name#?#cgi.query_string#" method="post" enctype="multipart/form-data">
			<td class="tblContent"><input type="File" name="newLangFile"><br>
			<input type="submit" value="#stText.Overview.Submit#"></td>
			</form>
		</tr>
	</table>
</cfif>
--->
</cfoutput>

<cffunction name="readLanguages" output="No" returntype="struct">
	<cfdirectory name="local.getLangs" directory="resources/language/" action="list" mode="listnames" filter="*.xml">
	<cfset var stRet = {}>
	<cfloop query="getLangs">
		<cffile action="read" file="resources/language/#getLangs.name#" variable="local.sContent">
		<cfset local.sXML = XMLParse(sContent)>
		<cfset stRet[sXML.language.XMLAttributes.Key] = sXML.language.XMLAttributes.label>
	</cfloop>
	<cfreturn stRet>
</cffunction>