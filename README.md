Installation steps

Add the below dependencies in the cms-dependencies pom.xml

```$xslt
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
      <artifactId>brxm-s3-manager-site</artifactId>
      <version>${project.version}</version>
    </dependency>
```

(Optional) Add CKEditor S3 Manager button

Configure the below property on the /cluster.options: node of an RTF field in a document type of your project or on global level /hippo:namespaces/system/Html/editor:templates/_default_
```
ckeditor.config.overlayed.json: '{   extraPlugins: ''iframedialog,s3manager''                 }'
```

Add custom content rewriter for RTF

In your project add the below line in /site/webapp/src/main/webapp/WEB-INF/hst-config.properties
```
default.hst.contentrewriter.class = com.bloomreach.xm.manager.rewriter.S3AssetsLinkRewriter
```