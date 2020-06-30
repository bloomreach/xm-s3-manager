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

Modify web.xml of the CMS application

```$xslt
    <servlet>
        <servlet-name>OpenUIResourceServlet</servlet-name>
        <servlet-class>org.onehippo.cms7.utilities.servlet.SecureCmsResourceServlet</servlet-class>
        <init-param>
          <param-name>jarPathPrefix</param-name>
          <param-value>/openui</param-value>
        </init-param>
        <init-param>
          <param-name>allowedResourcePaths</param-name>
          <param-value>
            ^/.*\..*
          </param-value>
        </init-param>
    </servlet>
    
    <!--SNIP-->

    <servlet-mapping>
        <servlet-name>OpenUIResourceServlet</servlet-name>
        <url-pattern>/openui/*</url-pattern>
    </servlet-mapping>
```