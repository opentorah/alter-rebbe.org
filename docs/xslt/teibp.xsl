<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0" 
  xmlns:eg="http://www.tei-c.org/ns/Examples"
  xmlns:tei="http://www.tei-c.org/ns/1.0" 
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:exsl="http://exslt.org/common"
  xmlns:msxsl="urn:schemas-microsoft-com:xslt"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  extension-element-prefixes="exsl msxsl"
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:html="http://www.w3.org/1999/xhtml" 
  exclude-result-prefixes="xsl tei xd eg fn #default">
  
  <!-- import teibp-orig.xsl, which allows templates,
         variables, and parameters from teibp-orig.xsl
         to be overridden here. -->
  <xsl:import href="teibp-orig.xsl"/>


  <!-- Disable toolbox and Google analytics -->
  <xsl:param name="includeToolbox" select="false()"/>
  <xsl:param name="includeAnalytics" select="false()"/>

  <!-- I use nested layout; common files are two levels up, not one :) -->
  <xsl:param name="filePrefix" select="''"/>
  

  <!-- Use proper CSS file -->
  <xsl:param name="teibpCSS" select="'/assets/teibp.css'"/>

  <!-- Suppress "target" attribute on the links so that they open in-place -->
<!--  <xsl:template match="@target"/> -->


  <!-- Facsimile viewer prefix -->
  <xsl:param name="pbNote"><text>страница: </text></xsl:param>
  

  <!-- turn TEI elements with 'ref' attributes (e.g., 'name') into links. -->
  <xsl:template match="tei:*[@ref]" priority="99">
    <a href="{@ref}">
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="rendition"/>
      <xsl:apply-templates select="node()"/>
    </a>
  </xsl:template>

  <!-- Add archive license to the footer -->
  <xsl:variable name="htmlFooter">
    <footer>
      <div>
        Powered by <a href="{$teibpHome}">TEI Boilerplate</a>.
        TEI Boilerplate is licensed under a <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a>.
        <a href="http://creativecommons.org/licenses/by/3.0/">
          <img alt="Creative Commons License" style="border-width:0;" src="http://i.creativecommons.org/l/by/3.0/80x15.png"/>
        </a>
      </div>

      <div>
        Content is licensed under a <a href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
        <a href="http://creativecommons.org/licenses/by/4.0/">
          <img alt="Creative Commons License" style="border-width:0;" src="https://i.creativecommons.org/l/by/4.0/80x15.png"/>
        </a>
      </div>
    </footer>
  </xsl:variable>
</xsl:stylesheet>
