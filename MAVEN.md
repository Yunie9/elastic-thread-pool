# Maven installation and publishing

## JitPack

After the public `1.0.0` tag is published, add the repository and dependency below to your project's `pom.xml`. JitPack builds on the first request and requires no GitHub token for public repositories. [JitPack documentation](https://docs.jitpack.io/)

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

Use the same dependency with the repository below. Downloads require authentication even for public Maven packages. In `~/.m2/settings.xml`, configure a server with ID `github`, your GitHub username, and a personal access token (classic) with the `read:packages` scope as the password. A GitHub Actions workflow can use its `GITHUB_TOKEN` if its repository has read access to the package and the workflow grants `packages: read`. [GitHub Packages Maven authentication](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages)

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/yunie9/elastic-thread-pool</url>
    </repository>
</repositories>
```

## Maven Central

Publishing to Maven Central requires a Sonatype Central Portal account and a verified namespace. To retain the current coordinates, the publisher would need a namespace covering `com.github.Yunie9`. Central also requires complete POM metadata, sources and Javadoc JARs, GPG signatures, and checksums. [Namespace registration](https://central.sonatype.org/register/namespace/) · [Publishing requirements](https://central.sonatype.org/publish/requirements/)
