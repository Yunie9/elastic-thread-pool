# Maven 安装与发布

## JitPack

公开的 `1.0.0` 标签发布后，将以下仓库和依赖加入项目的 `pom.xml`。JitPack 会在首次请求时构建，解析公开仓库的依赖无需 GitHub 令牌。 [JitPack 文档](https://docs.jitpack.io/)

```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Yunie9</groupId>
        <artifactId>elastic-thread-pool</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## GitHub Packages

使用相同的依赖坐标和下方的仓库。即使 Maven 包是公开的，下载也需要认证。在 `~/.m2/settings.xml` 中配置 ID 为 `github` 的服务器，用户名填写 GitHub 用户名，密码使用带有 `read:packages` 权限的经典个人访问令牌。若工作流所在仓库拥有该包的读取权限，且工作流已授予 `packages: read`，则可使用 Actions 的 `GITHUB_TOKEN`。 [GitHub Packages Maven 认证文档](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages)

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/yunie9/elastic-thread-pool</url>
    </repository>
</repositories>
```

## Maven Central

发布到 Maven Central 需要 Sonatype Central Portal 账户和已验证的命名空间。若沿用当前坐标，发布者需获得覆盖 `com.github.Yunie9` 的命名空间。Central 还要求完整的 POM 元数据、源码和 Javadoc JAR、GPG 签名及校验和。 [命名空间注册](https://central.sonatype.org/register/namespace/) · [发布要求](https://central.sonatype.org/publish/requirements/)
