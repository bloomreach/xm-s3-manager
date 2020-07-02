Installation steps

In the root pom.xml add the bellow dependency both in the dependencyManagement and dependencies section
```$xslt
    <dependencyManagement>
        <dependencies>
          <!--SNIP-->
          
          <dependency>
            <groupId>com.bloomreach.xm.manager</groupId>
            <artifactId>brxm-s3-manager-common</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <scope>provided</scope>
          </dependency>
          
          <!--SNIP-->
        </dependencies>
    </dependencyManagement>
    
    <dependencies>
        <!--SNIP-->

        <dependency>
          <groupId>com.bloomreach.xm.manager</groupId>
          <artifactId>brxm-s3-manager-common</artifactId>
          <version>0.1.0-SNAPSHOT</version>
          <scope>provided</scope>
        </dependency>

        <!--SNIP-->
    </dependencies>
```

In the root pom.xml under the profile cargo run and in the 
```$xslt
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.cargo</groupId>
                <artifactId>cargo-maven2-plugin</artifactId>
                <!--SNIP-->
                <configuration>
                    <container>
                        <!--SNIP-->
                        <dependencies>
                          <!--SNIP-->
                    
                          <dependency>
                            <groupId>com.bloomreach.xm.manager</groupId>
                            <artifactId>brxm-s3-manager-common</artifactId>
                            <classpath>shared</classpath>
                          </dependency>
                    
                          <!--SNIP-->
                        </dependencies>
                    </container>
                </configuration
            </plugin>
        </plugins>
    </build
```

In the src/main/assembly/shared-lib-component.xml add the below entry
```$xslt
<include>com.bloomreach.xm.manager:brxm-s3-manager-common</include>
```

Add the below dependencies in the cms-dependencies pom.xml

```$xslt
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-common</artifactId>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-api</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-repository</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-frontend-app</artifactId>
      <version>${project.version}</version>
    </dependency>
```
Add the below dependency in the site/components pom.xml
```$xslt
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-common</artifactId>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-site</artifactId>
      <version>${project.version}</version>
    </dependency>
```

In the site/webapp/src/main/webapp/WEB-INF/web.xml add to the hst-beans-annotated-classes parameter comma separated value the value below
```$xslt
classpath*:com/bloomreach/xm/manager/**/*.class
```
(Optional) Add CKEditor S3 Manager button

(YAML) Configure the below property on the /cluster.options: node of an RTF field in a document type of your project or on global level /hippo:namespaces/system/Html/editor:templates/_default_
```
ckeditor.config.overlayed.json: '{   extraPlugins: ''iframedialog,s3manager''                 }'
```
Console or CMS document editor
```
ckeditor.config.overlayed.json: {   extraPlugins: 'iframedialog,s3manager'                 }
```

Add custom content rewriter for RTF

In your project add the below line in /site/webapp/src/main/webapp/WEB-INF/hst-config.properties
```
default.hst.contentrewriter.class = com.bloomreach.xm.manager.rewriter.S3AssetsLinkRewriter
```