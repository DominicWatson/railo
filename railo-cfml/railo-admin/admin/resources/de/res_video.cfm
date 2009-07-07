<cfset stText.video.provider="Provider">
<cfset stText.video.providerDesc="URL eines Provider der die ben�tigten Komponenten (FFMpeg Binaries) anbietet.">
<cfset stText.video.providerMissing="Fehlende Provider Definition">
<cfset stText.video.upload="Upload">
<cfset stText.video.uploadDesc="Video Komponenten (ffmpeg.zip)">
<cfset stText.video.uploadMissing="Fehlende Upload Definition">
<cfset stText.video.server.installedNotDesc="Da f�r das Tag cfvideo/cfvideoplayer betriebssystemspezifische Video Komponenten ben�tigt werden, sind diese nicht mit Railo gebundelt. Dies w�rde den Umfang der Software unn�tig vergr�ssern. Zudem d�rfen gewisse enthaltene Codecs nicht vertrieben werden. Die Verwendung derer ist jedoch uneingeschr�nkt, weshalb sie diese nachladen k�nnen. Sie k�nnen diese Komponenten direkt vom Provider laden oder hier per Formular hochladen.">
<cfset stText.video.server.installedNotURLTitle="Video Komponenten �ber URL">
<cfset stText.video.server.installedNotURLDesc="Video Komponenten werden direkt vom Remote Server geladen und in Railo kopiert (keine Installation).">
<cfset stText.video.server.installedNotUploadTitle="Video Komponenten �ber Upload">
<cfset stText.video.server.installedNotUploadDesc="Video Komponenten (ffmpeg.zip) werden direkt �ber Formular hochgeladen und in Railo kopiert (keine Installation). Als Quelle dient z.B. {provider}">
<cfset stText.video.server.installed="Die ben�tigten Video Komponenten sind auf Ihrem System installiert.">
<cfset stText.video.server.manTitle="Manuelle Installation">
<cfset stText.video.server.manDesc="Eine Manuelle Installation wird wie folgt vorgenommen: Navigieren Sie zur Adresse {provider} und laden sie dort die ffmpeg.zip f�r Ihr Betriebssystem ({OS-Name}) herunter. Kopieren Sie diese Datei (nicht entpacken) in das Verzeichnis {directory}. Falls Sie f�r Ihr Betriebssystem keinen Download finden, kontaktieren Sie uns.">
<cfset stText.video.server.installedBut1="Video Komponenten sind zwar installiert, jedoch k�nnen sie nicht korrekt ausgef�hrt werden:">
<cfset stText.video.server.installedBut2="Wir empfehlen Ihnen eine manuelle Installation vorzunehmen.">
<cfset stText.video.web.installedNot="Video Komponenten sind nicht auf Ihrem System installiert. Um diese zu installieren wechseln sie in den Railo Server Administrator.">
<cfset stText.video.web.installed="Video Komponenten sind auf Ihrem System installiert.">
<cfset stText.video.web.installedBut="Video Komponenten sind zwar installiert, jedoch k�nnen sie nicht korrekt ausgef�hrt werden. Wechseln Sie in den Railo Server Administrator, um dies zu reparieren:">