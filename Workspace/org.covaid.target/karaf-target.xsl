<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:pom="http://maven.apache.org/POM/4.0.0"
	exclude-result-prefixes="pom">

	<xsl:output method="xml" indent="yes" encoding="UTF-8" />

	<xsl:variable name="pom"
		select="document('pom.xml')/pom:project" />

	<xsl:variable name="version">
		<xsl:choose>
			<xsl:when test="$pom/pom:version">
				<xsl:value-of select="$pom/pom:version" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$pom/pom:parent/pom:version" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:variable name="artifactId" select="artifactId" />

	<xsl:template match="/target">
		<xsl:message>
			Maven dependencies found:
			<xsl:value-of
				select="count(locations/location[@type='Maven']
                       /dependencies/dependency)" />
		</xsl:message>

		<xsl:message>
			Selected bundles found:
			<xsl:value-of select="count(includeBundles/plugin)" />
		</xsl:message>
		<features
			xmlns="http://karaf.apache.org/xmlns/features/v1.4.0"
			name="indymo-target-karaf">

			<feature name="indymo-target-karaf-feature" version="{$version}">
				<feature prerequisite="true">wrap</feature>
				<xsl:for-each
					select="locations/location[@type='Maven']/dependencies/dependency[
                            not(groupId = 'junit')
                            and not(starts-with(groupId, 'org.junit'))
                            and not(groupId = 'org.opentest4j')]">

					<xsl:variable name="groupId" select="groupId" />
					<xsl:variable name="artifactId" select="artifactId" />
					<xsl:variable name="artifactVersion" select="version" />

					<!-- Evaluate wrapping for the current dependency -->
					<xsl:variable name="needsWrapping"
						select="$artifactId = 'httpclient'
                             or $artifactId = 'httpcore'
                            or $artifactId = 'javax.inject'
                            or $artifactId = 'jta'
                            or $artifactId = 'junit'
                            or $artifactId = 'aopalliance'" />

					<!-- Ignore duplicates already encountered -->
					<xsl:if
						test="not(preceding-sibling::dependency[
                            groupId = $groupId
                            and artifactId = $artifactId
                            and version = $artifactVersion])">

						<bundle>
							<xsl:if test="$needsWrapping">
								<xsl:text>wrap:</xsl:text>
							</xsl:if>

							<xsl:text>mvn:</xsl:text>
							<xsl:value-of select="$groupId" />
							<xsl:text>/</xsl:text>
							<xsl:value-of select="$artifactId" />
							<xsl:text>/</xsl:text>
							<xsl:value-of select="$artifactVersion" />
							
							<xsl:if
                                test="$groupId = 'javax.inject'
                                      and $artifactId = 'javax.inject'">
                                <xsl:text>$Export-Package=javax.inject;version=1.0.0</xsl:text>
                            </xsl:if>
						</bundle>
					</xsl:if>

				</xsl:for-each>
			</feature>
		</features>
	</xsl:template>
</xsl:stylesheet>