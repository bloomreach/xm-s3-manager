##Installation

- In the root pom.xml configure in the properties a version for the plugin and add the bellow dependency both in the dependencyManagement and dependencies section
```xml
    <properties>
        <brxm.s3.manager.version>x.y.z</brxm.s3.manager.version>
    </properties>

    <dependencyManagement>
        <dependencies>
          <!--SNIP-->
          
          <dependency>
            <groupId>com.bloomreach.xm.manager</groupId>
            <artifactId>brxm-s3-manager-common</artifactId>
            <version>${brxm.s3.manager.version}</version>
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
          <version>${brxm.s3.manager.version}</version>
          <scope>provided</scope>
        </dependency>
    
        <!--SNIP-->
    </dependencies>
```

- In the root pom.xml under the profile cargo run and in the 
```xml
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

- In the `src/main/assembly/shared-lib-component.xml` add the below entry
```xml
  <include>com.bloomreach.xm.manager:brxm-s3-manager-common</include>
```

- Add the below dependencies in the `cms-dependencies/pom.xml`

```xml
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-common</artifactId>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-api</artifactId>
      <version>${brxm.s3.manager.version}</version>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-repository</artifactId>
      <version>${brxm.s3.manager.version}</version>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-frontend-app</artifactId>
      <version>${brxm.s3.manager.version}</version>
    </dependency>
```
- Add the below dependencies in the `site/components/pom.xml`
```xml
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-common</artifactId>
    </dependency>
    <dependency>
      <groupId>com.bloomreach.xm.manager</groupId>
      <artifactId>brxm-s3-manager-site</artifactId>
      <version>${brxm.s3.manager.version}</version>
    </dependency>
```

- In the `site/webapp/src/main/webapp/WEB-INF/web.xml` add to the hst-beans-annotated-classes parameter (comma separated value) the value below
```xml
  classpath*:com/bloomreach/xm/manager/**/*.class
```

- In the content bean of the document type that you have enabled the S3 compound add a getter similar to this
```java
@HippoEssentialsGenerated(internalName = "demo:assets", allowModifications = false)
    public S3managerpicker getAssets() {
        return getBean("demo:assets", S3managerpicker.class);
    }
```

###(Optional) Add CKEditor S3 Manager button

If you are editing directly a yaml file, configure the below property on the /cluster.options: node of an RTF field in a document type of your project or on global level `/hippo:namespaces/system/Html/editor:templates/_default_`
```yaml
ckeditor.config.overlayed.json: '{   extraPlugins: ''iframedialog,s3manager''                 }'
```
If configuring the document via Console or CMS document editor
```yaml
ckeditor.config.overlayed.json: {   extraPlugins: 'iframedialog,s3manager'                 }
```

- Add custom content rewriter for RTF

    In your project add the below line in `site/webapp/src/main/webapp/WEB-INF/hst-config.properties`
```properties
    default.hst.contentrewriter.class = com.bloomreach.xm.manager.rewriter.S3AssetsLinkRewriter
```